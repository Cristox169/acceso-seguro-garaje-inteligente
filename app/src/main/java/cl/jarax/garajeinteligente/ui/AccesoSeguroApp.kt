package cl.jarax.garajeinteligente.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.jarax.garajeinteligente.data.AppDatabaseHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val OtBackground = Color(0xFF0E1929)
private val OtPanel = Color(0xFF1B293E)
private val OtField = Color(0xFF0B1527)
private val OtLine = Color(0xFF40506A)
private val OtBlue = Color(0xFF18ABE2)
private val OtGreen = Color(0xFF19C58D)
private val OtRed = Color(0xFFFF5362)
private val OtPurple = Color(0xFFD4BBFA)
private val OtOrange = Color(0xFFFFB522)
private val OtText = Color(0xFFF4F5F8)
private val OtMuted = Color(0xFFA7ADBD)
private val OtDisabled = Color(0xFF667083)

private enum class OtSection { MONITOR, ACTUATOR, CONNECTION }
private enum class ServoMode { AUTO, OPEN, CLOSED }
private enum class ConnectionMode { BLUETOOTH, WIFI, MESH }

private data class AuditEvent(
    val timestamp: String,
    val action: String,
    val user: String,
    val detail: String,
)

@Suppress("UNUSED_PARAMETER")
@Composable
fun AccesoSeguroApp(
    initialOnboardingComplete: Boolean,
    onOnboardingComplete: () -> Unit,
    onRestartOnboarding: () -> Unit,
) {
    val context = LocalContext.current
    val database = remember { AppDatabaseHelper(context.applicationContext) }
    var loggedIn by rememberSaveable { mutableStateOf(false) }
    var showAudit by rememberSaveable { mutableStateOf(false) }
    val audit = remember { mutableStateListOf<AuditEvent>() }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = OtBackground) {
            if (!loggedIn) {
                LoginScreen(
                    authenticate = database::authenticate,
                    onLoginSuccess = {
                        loggedIn = true
                        audit.add(0, auditEvent("LOGIN_SUCCESS", "admin", "Acceso concedido a operador autorizado"))
                        audit.add(1, auditEvent("INITIALIZE", "SYSTEM", "Gestor de Seguridad OT ISO 27001 iniciado"))
                    },
                )
            } else {
                ControlCenter(audit = audit, onShowAudit = { showAudit = true })
            }
            if (showAudit) AuditDialog(audit = audit, onDismiss = { showAudit = false })
        }
    }
}

@Composable
private fun LoginScreen(authenticate: (String, String) -> Boolean, onLoginSuccess: () -> Unit) {
    var username by rememberSaveable { mutableStateOf("admin") }
    var password by rememberSaveable { mutableStateOf("Admin123!") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(WindowInsets.safeDrawing.asPaddingValues())
            .verticalScroll(rememberScrollState()).padding(horizontal = 26.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(56.dp))
        OtLogo(size = 96)
        Spacer(Modifier.height(25.dp))
        Text("Sistema Control de Acceso OT", color = OtText, fontSize = 25.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(7.dp))
        Text("Monitoreo y Activación en Zonas Oscuras", color = OtMuted, fontSize = 15.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))

        Surface(
            modifier = Modifier.fillMaxWidth().border(1.dp, OtLine, RoundedCornerShape(18.dp)),
            color = OtPanel,
            shape = RoundedCornerShape(18.dp),
        ) {
            Column(Modifier.padding(horizontal = 24.dp, vertical = 25.dp), verticalArrangement = Arrangement.spacedBy(19.dp)) {
                Text("Ingreso de Credenciales", color = OtBlue, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                OtTextField(value = username, onValueChange = { username = it; error = "" }, label = "Usuario Operador")
                OtTextField(
                    value = password,
                    onValueChange = { password = it; error = "" },
                    label = "Contraseña",
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardType = KeyboardType.Password,
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña", tint = OtMuted)
                        }
                    },
                )
                if (error.isNotEmpty()) Text(error, color = OtRed, fontSize = 12.sp)
                Button(
                    onClick = { if (authenticate(username, password)) onLoginSuccess() else error = "Usuario o contraseña incorrectos" },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(13.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OtBlue, contentColor = Color.White),
                ) { Text("INICIAR SESIÓN", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
                Text("Credenciales de Prueba: admin / Admin123!", color = OtDisabled, fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
        }
        Spacer(Modifier.height(27.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(Icons.Filled.Lock, null, tint = OtOrange, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(7.dp))
            Text("Cumplimiento Estándar ISO/IEC 27001 y Seguridad OT", color = OtGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun OtTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardType: KeyboardType = KeyboardType.Text,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        trailingIcon = trailingIcon,
        shape = RoundedCornerShape(7.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = OtField, unfocusedContainerColor = OtField,
            focusedTextColor = OtText, unfocusedTextColor = OtText, cursorColor = OtBlue,
            focusedBorderColor = OtBlue, unfocusedBorderColor = Color(0xFF050C18),
            focusedLabelColor = OtBlue, unfocusedLabelColor = OtMuted,
        ),
    )
}

@Composable
private fun OtLogo(size: Int) {
    Box(
        modifier = Modifier.size(size.dp).clip(CircleShape).background(OtField).border(3.dp, OtBlue, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Filled.Security, null, tint = Color(0xFFCED7E6), modifier = Modifier.size((size * .58f).dp))
        Box(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = (size * .14f).dp)
                .size((size * .36f).dp).clip(RoundedCornerShape((size * .07f).dp)).background(OtBlue),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Lock, null, tint = OtOrange, modifier = Modifier.size((size * .22f).dp))
        }
    }
}

@Composable
private fun ControlCenter(audit: MutableList<AuditEvent>, onShowAudit: () -> Unit) {
    var section by rememberSaveable { mutableStateOf(OtSection.MONITOR) }
    var distance by rememberSaveable { mutableFloatStateOf(50f) }
    var light by rememberSaveable { mutableFloatStateOf(500f) }
    var distanceThreshold by rememberSaveable { mutableFloatStateOf(20f) }
    var lightThreshold by rememberSaveable { mutableFloatStateOf(400f) }
    var servoMode by rememberSaveable { mutableStateOf(ServoMode.AUTO) }
    var emergency by rememberSaveable { mutableStateOf(false) }
    var connectionMode by rememberSaveable { mutableStateOf(ConnectionMode.BLUETOOTH) }
    var connected by rememberSaveable { mutableStateOf(false) }
    var command by rememberSaveable { mutableStateOf("CMD_OPEN_GATE_SECURE") }
    var packets by rememberSaveable { mutableStateOf(0) }

    val automaticOpen = light < lightThreshold && distance < distanceThreshold
    val gateOpen = !emergency && when (servoMode) {
        ServoMode.AUTO -> automaticOpen
        ServoMode.OPEN -> true
        ServoMode.CLOSED -> false
    }

    Column(Modifier.fillMaxSize().padding(WindowInsets.safeDrawing.asPaddingValues())) {
        Header(onShowAudit)
        SectionTabs(section = section, onSelect = { section = it })
        when (section) {
            OtSection.MONITOR -> MonitorScreen(distance, light, distanceThreshold, lightThreshold, gateOpen, { distance = it }, { light = it })
            OtSection.ACTUATOR -> ActuatorScreen(
                servoMode = servoMode,
                onServoModeChange = { servoMode = it; audit.add(0, auditEvent("SERVO_MODE", "admin", "Modo seleccionado: ${it.name}")) },
                emergency = emergency,
                onEmergencyChange = { emergency = it; audit.add(0, auditEvent("OT_EMERGENCY", "admin", if (it) "Anulación activada" else "Anulación desactivada")) },
                distanceThreshold = distanceThreshold,
                lightThreshold = lightThreshold,
                onDistanceThresholdChange = { distanceThreshold = it },
                onLightThresholdChange = { lightThreshold = it },
                command = command,
                onCommandChange = { command = it },
                onSendCommand = { packets++; audit.add(0, auditEvent("WIRELESS_COMMAND", "admin", command.ifBlank { "Comando vacío" })) },
            )
            OtSection.CONNECTION -> ConnectionScreen(
                mode = connectionMode,
                onModeChange = { connectionMode = it },
                connected = connected,
                packets = packets,
                onConnect = { connected = true; packets += 2; audit.add(0, auditEvent("CONNECT", "admin", "Enlace ${connectionMode.name} establecido")) },
                onDisconnect = { connected = false; audit.add(0, auditEvent("DISCONNECT", "admin", "Conexión inalámbrica finalizada")) },
            )
        }
    }
}

@Composable
private fun Header(onShowAudit: () -> Unit) {
    Surface(
        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp).fillMaxWidth().border(1.dp, OtLine, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp), color = OtPanel,
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            OtLogo(size = 58)
            Column(Modifier.weight(1f)) {
                Text("Sistema Control\nde Acceso OT", color = OtText, fontSize = 20.sp, lineHeight = 23.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(5.dp))
                Text("Operador: admin | ISO\n27001 Activo", color = OtBlue, fontSize = 13.sp, lineHeight = 16.sp)
            }
            OutlinedButton(onClick = onShowAudit, shape = RoundedCornerShape(7.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = OtBlue), modifier = Modifier.height(46.dp)) {
                Text("AUDITORÍA", fontSize = 12.sp, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
private fun SectionTabs(section: OtSection, onSelect: (OtSection) -> Unit) {
    Row(
        modifier = Modifier.padding(horizontal = 18.dp, vertical = 5.dp).fillMaxWidth().background(OtPanel).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        TabButton("Monitoreo", section == OtSection.MONITOR, Modifier.weight(1f)) { onSelect(OtSection.MONITOR) }
        TabButton("Activador", section == OtSection.ACTUATOR, Modifier.weight(1f)) { onSelect(OtSection.ACTUATOR) }
        TabButton("Conexión\nInalámbrica", section == OtSection.CONNECTION, Modifier.weight(1f)) { onSelect(OtSection.CONNECTION) }
    }
}

@Composable
private fun TabButton(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier.height(55.dp).clip(RoundedCornerShape(12.dp)).background(if (selected) OtBlue else OtField).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Text(text, color = OtText, fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center) }
}

@Composable
private fun MonitorScreen(
    distance: Float,
    light: Float,
    distanceThreshold: Float,
    lightThreshold: Float,
    gateOpen: Boolean,
    onDistanceChange: (Float) -> Unit,
    onLightChange: (Float) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp).padding(top = 14.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SectionLabel("ENTRADAS (DATOS RECIBIDOS DE SENSORES)")
        SensorPanel(Icons.Filled.Sensors, "Sensor Ultrasónico (Distancia)", "${distance.toInt()} cm", "Umbral configurado: < ${distanceThreshold.toInt()} cm", distance, 0f..100f, OtBlue, onDistanceChange)
        SensorPanel(Icons.Filled.LightMode, "Fotorresistencia LDR (Luz)", light.toInt().toString(), "Umbral configurado: Oscuridad < ${lightThreshold.toInt()}", light, 0f..1000f, OtOrange, onLightChange)
        GateStatusCard(gateOpen, distance, light)
    }
}

@Composable
private fun SensorPanel(
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String,
    sliderValue: Float,
    range: ClosedFloatingPointRange<Float>,
    accent: Color,
    onValueChange: (Float) -> Unit,
) {
    OtCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = OtMuted, modifier = Modifier.size(19.dp))
            Spacer(Modifier.width(7.dp))
            Text(title, color = OtText, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text(value, color = accent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Text(subtitle, color = OtDisabled, fontSize = 12.sp)
        OtSlider(sliderValue, onValueChange, range)
    }
}

@Composable
private fun GateStatusCard(gateOpen: Boolean, distance: Float, light: Float) {
    val color = if (gateOpen) OtRed else OtGreen
    Surface(
        modifier = Modifier.fillMaxWidth().border(2.dp, color, RoundedCornerShape(18.dp)),
        color = if (gateOpen) Color(0xFF3B0E18) else Color(0xFF003B22), shape = RoundedCornerShape(18.dp),
    ) {
        Column(Modifier.padding(21.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(if (gateOpen) "ALERTA DE ACCESO" else "ESTADO NORMAL (REPOSO)", color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(if (gateOpen) "BARRERA ABIERTA" else "BARRERA CERRADA", color = color, fontSize = 25.sp, fontWeight = FontWeight.Bold)
            Text("Servomotor: ${if (gateOpen) 90 else 0}°", color = OtMuted, fontSize = 16.sp)
            Box(Modifier.fillMaxWidth().height(1.dp).background(OtLine))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                LedStatus("LED Verde", !gateOpen, OtGreen)
                LedStatus("LED Rojo", gateOpen, OtRed)
            }
            Text(
                if (gateOpen) "Condiciones cumplidas: zona oscura y obstáculo detectado."
                else "Condiciones no cumplidas (Distancia: ${distance.toInt()} cm, Luz: ${light.toInt()}).",
                color = OtMuted, fontSize = 13.sp, lineHeight = 18.sp,
            )
        }
    }
}

@Composable
private fun LedStatus(label: String, on: Boolean, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(24.dp).clip(CircleShape).background(if (on) color else OtField))
        Text("$label (${if (on) "ON" else "OFF"})", color = if (on) color else OtDisabled, fontSize = 12.sp)
    }
}

@Composable
private fun ActuatorScreen(
    servoMode: ServoMode,
    onServoModeChange: (ServoMode) -> Unit,
    emergency: Boolean,
    onEmergencyChange: (Boolean) -> Unit,
    distanceThreshold: Float,
    lightThreshold: Float,
    onDistanceThresholdChange: (Float) -> Unit,
    onLightThresholdChange: (Float) -> Unit,
    command: String,
    onCommandChange: (String) -> Unit,
    onSendCommand: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp).padding(top = 14.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SectionLabel("ACTIVADOR Y CONTROL MANUAL DE ACTUADORES")
        OtCard {
            CardTitle("Modo de Operación del Servomotor")
            RadioRow("Automático (Lógica de Sensores)", servoMode == ServoMode.AUTO) { onServoModeChange(ServoMode.AUTO) }
            RadioRow("Manual: Forzar Barrera ABIERTA (90°)", servoMode == ServoMode.OPEN) { onServoModeChange(ServoMode.OPEN) }
            RadioRow("Manual: Forzar Barrera CERRADA (0°)", servoMode == ServoMode.CLOSED) { onServoModeChange(ServoMode.CLOSED) }
        }
        OtCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Anulación de Emergencia (OT Switch)", color = OtText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Inhabilita aperturas automáticas por seguridad", color = OtDisabled, fontSize = 12.sp, lineHeight = 17.sp)
                }
                Switch(
                    checked = emergency, onCheckedChange = onEmergencyChange,
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = OtRed, uncheckedThumbColor = Color.White, uncheckedTrackColor = OtDisabled),
                )
            }
        }
        OtCard {
            CardTitle("Calibración Dinámica de Umbrales")
            Text("Umbral Distancia: ${distanceThreshold.toInt()} cm", color = OtText, fontSize = 14.sp)
            OtSlider(distanceThreshold, onDistanceThresholdChange, 5f..80f)
            Text("Umbral Luz: ${lightThreshold.toInt()} ADC", color = OtText, fontSize = 14.sp)
            OtSlider(lightThreshold, onLightThresholdChange, 0f..1000f)
        }
        OtCard {
            CardTitle("Envío de Comando Inalámbrico (EditText)")
            OutlinedTextField(
                value = command, onValueChange = onCommandChange, modifier = Modifier.fillMaxWidth(), singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = OtText, unfocusedTextColor = OtText, focusedBorderColor = OtPurple, unfocusedBorderColor = OtMuted, cursorColor = OtBlue),
            )
            Button(
                onClick = onSendCommand, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OtBlue),
            ) { Text("ENVIAR COMANDO A OTRO\nDISPOSITIVO", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center) }
        }
    }
}

@Composable
private fun ConnectionScreen(
    mode: ConnectionMode,
    onModeChange: (ConnectionMode) -> Unit,
    connected: Boolean,
    packets: Int,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp).padding(top = 14.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SectionLabel("GESTOR DE CONEXIONES INALÁMBRICAS (BLUETOOTH / WI-FI)")
        OtCard {
            Text("Estado: ${if (connected) "CONECTADO" else "DESCONECTADO"}", color = if (connected) OtGreen else OtRed, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Tecnología: ${mode.description()}", color = OtMuted, fontSize = 13.sp)
            RadioRow("Bluetooth (HC-05 / ESP32)", mode == ConnectionMode.BLUETOOTH, Icons.Filled.Bluetooth) { onModeChange(ConnectionMode.BLUETOOTH) }
            RadioRow("Wi-Fi UDP Socket (192.168.1.100:8080)", mode == ConnectionMode.WIFI, Icons.Filled.Wifi) { onModeChange(ConnectionMode.WIFI) }
            RadioRow("Interconexión Multi-App Móvil (Mesh)", mode == ConnectionMode.MESH, Icons.Filled.Hub) { onModeChange(ConnectionMode.MESH) }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onConnect, enabled = !connected, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OtGreen, disabledContainerColor = OtGreen.copy(alpha = .35f)),
                ) { Text("CONECTAR", color = Color.White, fontWeight = FontWeight.SemiBold) }
                OutlinedButton(
                    onClick = onDisconnect, enabled = connected, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(7.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OtRed, disabledContentColor = OtRed.copy(alpha = .35f)),
                ) { Text("DESCONECTAR", fontWeight = FontWeight.SemiBold) }
            }
        }
        OtCard {
            CardTitle("Prueba de Estabilidad y Eficiencia de Red")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Latencia: ${if (connected) "18" else "--"} ms", color = OtText, fontSize = 13.sp)
                Text("Pérdida: ${if (connected) "0.0" else "--"}%", color = OtText, fontSize = 13.sp)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Paquetes Tx/Rx: $packets/${if (connected) packets else 0}", color = OtText, fontSize = 13.sp)
                Text("RSSI: ${if (connected) "-62" else "--"} dBm", color = OtText, fontSize = 13.sp)
            }
            Box(Modifier.fillMaxWidth().background(OtField).padding(13.dp)) {
                Text(
                    if (connected) "[SISTEMA] Enlace estable. Prueba de interconexión activa."
                    else "[SISTEMA] Listo para iniciar prueba de interconexión inalámbrica.",
                    color = OtGreen, fontSize = 12.sp, lineHeight = 17.sp,
                )
            }
        }
    }
}

private fun ConnectionMode.description(): String = when (this) {
    ConnectionMode.BLUETOOTH -> "Bluetooth RFCOMM (HC-05 / ESP32)"
    ConnectionMode.WIFI -> "Wi-Fi UDP Socket"
    ConnectionMode.MESH -> "Interconexión Multi-App Móvil"
}

@Composable
private fun RadioRow(text: String, selected: Boolean, icon: ImageVector? = null, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable(onClick = onClick).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick, colors = RadioButtonDefaults.colors(selectedColor = OtPurple, unselectedColor = OtMuted))
        if (icon != null) {
            Icon(icon, null, tint = OtBlue, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(7.dp))
        }
        Text(text, color = OtText, fontSize = 14.sp, lineHeight = 19.sp)
    }
}

@Composable
private fun OtSlider(value: Float, onValueChange: (Float) -> Unit, range: ClosedFloatingPointRange<Float>) {
    Slider(
        value = value, onValueChange = onValueChange, valueRange = range,
        colors = SliderDefaults.colors(thumbColor = OtPurple, activeTrackColor = OtPurple, inactiveTrackColor = OtDisabled),
    )
}

@Composable
private fun OtCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, OtLine, RoundedCornerShape(17.dp)), color = OtPanel, shape = RoundedCornerShape(17.dp),
    ) { Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content) }
}

@Composable
private fun CardTitle(text: String) {
    Text(text, color = OtBlue, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, color = OtMuted, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium)
}

@Composable
private fun AuditDialog(audit: List<AuditEvent>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss, containerColor = Color(0xFF414141), titleContentColor = OtText, textContentColor = OtText,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Lock, null, tint = OtOrange, modifier = Modifier.size(26.dp))
                Spacer(Modifier.width(8.dp))
                Text("Log de Auditoría OT (ISO/IEC 27001)", fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(Modifier.height(360.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(17.dp)) {
                audit.forEach { event ->
                    Text("[${event.timestamp}] ${event.action}\nUsuario: ${event.user} | Éxito: true\n${event.detail}", color = OtText, fontSize = 14.sp, lineHeight = 19.sp)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Aceptar", color = OtPurple, fontSize = 15.sp) } },
    )
}

private fun auditEvent(action: String, user: String, detail: String): AuditEvent = AuditEvent(
    timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()), action = action, user = user, detail = detail,
)
