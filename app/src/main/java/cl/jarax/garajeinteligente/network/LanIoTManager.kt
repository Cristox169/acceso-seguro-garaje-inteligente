package cl.jarax.garajeinteligente.network

import android.os.Handler
import android.os.Looper
import android.util.Base64
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Collections
import java.util.concurrent.atomic.AtomicBoolean
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

enum class LanRole { DISCONNECTED, CONTROLLER, MONITOR }

data class LanStatus(
    val role: LanRole = LanRole.DISCONNECTED,
    val connected: Boolean = false,
    val message: String = "Sin conexión",
    val localAddress: String = localIpv4Address(),
)

class LanIoTManager {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val running = AtomicBoolean(false)
    private var serverSocket: ServerSocket? = null
    private var socket: Socket? = null
    private var writer: BufferedWriter? = null
    private var sessionCode = ""

    var onStatusChanged: (LanStatus) -> Unit = {}
    var onPayloadReceived: (String) -> Unit = {}

    fun startController(code: String) {
        disconnect()
        sessionCode = normalizeCode(code)
        running.set(true)
        emitStatus(LanStatus(LanRole.CONTROLLER, false, "Esperando monitor en el puerto $PORT"))
        Thread {
            try {
                serverSocket = ServerSocket(PORT)
                val accepted = serverSocket?.accept() ?: return@Thread
                if (!running.get()) return@Thread
                socket = accepted
                writer = BufferedWriter(OutputStreamWriter(accepted.getOutputStream()))
                emitStatus(LanStatus(LanRole.CONTROLLER, true, "Monitor conectado de forma cifrada"))
                readLoop(accepted, LanRole.CONTROLLER)
            } catch (error: Exception) {
                if (running.get()) emitStatus(LanStatus(LanRole.CONTROLLER, false, "No se pudo iniciar: ${error.safeMessage()}"))
            }
        }.apply { name = "AccesoSeguro-LAN-Controller" }.start()
    }

    fun connectAsMonitor(host: String, code: String) {
        disconnect()
        sessionCode = normalizeCode(code)
        running.set(true)
        emitStatus(LanStatus(LanRole.MONITOR, false, "Conectando con ${host.trim()}…"))
        Thread {
            try {
                val connectedSocket = Socket(host.trim(), PORT)
                socket = connectedSocket
                writer = BufferedWriter(OutputStreamWriter(connectedSocket.getOutputStream()))
                emitStatus(LanStatus(LanRole.MONITOR, true, "Conectado al controlador con AES-GCM"))
                readLoop(connectedSocket, LanRole.MONITOR)
            } catch (error: Exception) {
                if (running.get()) emitStatus(LanStatus(LanRole.MONITOR, false, "Conexión fallida: ${error.safeMessage()}"))
            }
        }.apply { name = "AccesoSeguro-LAN-Monitor" }.start()
    }

    fun sendPayload(payload: String) {
        val currentWriter = writer ?: return
        val currentCode = sessionCode
        Thread {
            try {
                val encrypted = SecureLanCodec.encrypt(payload, currentCode)
                synchronized(currentWriter) {
                    currentWriter.write(encrypted)
                    currentWriter.newLine()
                    currentWriter.flush()
                }
            } catch (error: Exception) {
                emitStatus(LanStatus(message = "Error al enviar: ${error.safeMessage()}"))
            }
        }.apply { name = "AccesoSeguro-LAN-Send" }.start()
    }

    fun disconnect() {
        running.set(false)
        try { writer?.close() } catch (_: Exception) {}
        try { socket?.close() } catch (_: Exception) {}
        try { serverSocket?.close() } catch (_: Exception) {}
        writer = null
        socket = null
        serverSocket = null
        emitStatus(LanStatus())
    }

    private fun readLoop(activeSocket: Socket, role: LanRole) {
        try {
            BufferedReader(InputStreamReader(activeSocket.getInputStream())).use { reader ->
                while (running.get()) {
                    val line = reader.readLine() ?: break
                    val decoded = SecureLanCodec.decrypt(line, sessionCode)
                    mainHandler.post { onPayloadReceived(decoded) }
                }
            }
        } catch (error: Exception) {
            if (running.get()) emitStatus(LanStatus(role, false, "Conexión interrumpida: ${error.safeMessage()}"))
        } finally {
            if (running.get()) emitStatus(LanStatus(role, false, "Dispositivo desconectado"))
        }
    }

    private fun emitStatus(status: LanStatus) {
        mainHandler.post { onStatusChanged(status) }
    }

    private fun normalizeCode(code: String): String = code.trim().ifBlank { DEFAULT_CODE }

    private fun Exception.safeMessage(): String = message?.take(80) ?: javaClass.simpleName

    companion object {
        const val PORT = 40404
        const val DEFAULT_CODE = "GARAGE-2026"
    }
}

private object SecureLanCodec {
    private val random = SecureRandom()

    fun encrypt(plainText: String, code: String): String {
        val iv = ByteArray(12).also(random::nextBytes)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key(code), GCMParameterSpec(128, iv))
        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(iv + cipherText, Base64.NO_WRAP)
    }

    fun decrypt(encoded: String, code: String): String {
        val packet = Base64.decode(encoded, Base64.NO_WRAP)
        require(packet.size > 12) { "Mensaje inválido" }
        val iv = packet.copyOfRange(0, 12)
        val cipherText = packet.copyOfRange(12, packet.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(code), GCMParameterSpec(128, iv))
        return cipher.doFinal(cipherText).toString(Charsets.UTF_8)
    }

    private fun key(code: String): SecretKeySpec {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest("AccesoSeguro-LAN:$code".toByteArray(Charsets.UTF_8))
        return SecretKeySpec(digest, "AES")
    }
}

private fun localIpv4Address(): String {
    return try {
        Collections.list(NetworkInterface.getNetworkInterfaces())
            .asSequence()
            .filter { it.isUp && !it.isLoopback }
            .flatMap { Collections.list(it.inetAddresses).asSequence() }
            .filterIsInstance<Inet4Address>()
            .firstOrNull { it.isSiteLocalAddress }
            ?.hostAddress
            ?: "IP no disponible"
    } catch (_: Exception) {
        "IP no disponible"
    }
}
