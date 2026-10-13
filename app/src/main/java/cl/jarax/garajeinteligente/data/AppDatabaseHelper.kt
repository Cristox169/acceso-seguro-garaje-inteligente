package cl.jarax.garajeinteligente.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class SensorReading(
    val id: Long,
    val createdAt: Long,
    val lightLux: Int,
    val distanceCm: Int?,
    val gateOpen: Boolean,
    val alertActive: Boolean,
    val source: String,
) {
    fun formattedTime(): String = SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault()).format(Date(createdAt))
}

class AppDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                salt BLOB NOT NULL,
                password_hash BLOB NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE readings (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                created_at INTEGER NOT NULL,
                light_lux INTEGER NOT NULL,
                distance_cm INTEGER,
                gate_open INTEGER NOT NULL,
                alert_active INTEGER NOT NULL,
                source TEXT NOT NULL
            )
            """.trimIndent(),
        )
        insertUser(db, DEMO_USER, DEMO_PASSWORD)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS readings")
        db.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    fun authenticate(username: String, password: String): Boolean {
        readableDatabase.query(
            "users",
            arrayOf("salt", "password_hash"),
            "username = ?",
            arrayOf(username.trim().lowercase(Locale.ROOT)),
            null,
            null,
            null,
            "1",
        ).use { cursor ->
            if (!cursor.moveToFirst()) return false
            val salt = cursor.getBlob(0)
            val storedHash = cursor.getBlob(1)
            return constantTimeEquals(storedHash, hashPassword(password, salt))
        }
    }

    fun addReading(
        lightLux: Int,
        distanceCm: Int?,
        gateOpen: Boolean,
        alertActive: Boolean,
        source: String = "Este dispositivo",
    ) {
        writableDatabase.insert(
            "readings",
            null,
            ContentValues().apply {
                put("created_at", System.currentTimeMillis())
                put("light_lux", lightLux)
                if (distanceCm == null) putNull("distance_cm") else put("distance_cm", distanceCm)
                put("gate_open", if (gateOpen) 1 else 0)
                put("alert_active", if (alertActive) 1 else 0)
                put("source", source)
            },
        )
    }

    fun latestReadings(limit: Int = 30): List<SensorReading> {
        val readings = mutableListOf<SensorReading>()
        readableDatabase.query(
            "readings",
            arrayOf("id", "created_at", "light_lux", "distance_cm", "gate_open", "alert_active", "source"),
            null,
            null,
            null,
            null,
            "created_at DESC",
            limit.coerceIn(1, 100).toString(),
        ).use { cursor ->
            while (cursor.moveToNext()) {
                readings += SensorReading(
                    id = cursor.getLong(0),
                    createdAt = cursor.getLong(1),
                    lightLux = cursor.getInt(2),
                    distanceCm = if (cursor.isNull(3)) null else cursor.getInt(3),
                    gateOpen = cursor.getInt(4) == 1,
                    alertActive = cursor.getInt(5) == 1,
                    source = cursor.getString(6),
                )
            }
        }
        return readings
    }

    fun clearReadings() {
        writableDatabase.delete("readings", null, null)
    }

    private fun insertUser(db: SQLiteDatabase, username: String, password: String) {
        val salt = ByteArray(16).also(SecureRandom()::nextBytes)
        db.insert(
            "users",
            null,
            ContentValues().apply {
                put("username", username)
                put("salt", salt)
                put("password_hash", hashPassword(password, salt))
            },
        )
    }

    private fun hashPassword(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, HASH_BITS)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun constantTimeEquals(expected: ByteArray, actual: ByteArray): Boolean {
        if (expected.size != actual.size) return false
        var difference = 0
        expected.indices.forEach { index -> difference = difference or (expected[index].toInt() xor actual[index].toInt()) }
        return difference == 0
    }

    companion object {
        const val DEMO_USER = "operador"
        const val DEMO_PASSWORD = "Acceso2026!"
        private const val DATABASE_NAME = "acceso_seguro.db"
        private const val DATABASE_VERSION = 1
        private const val PBKDF2_ITERATIONS = 120_000
        private const val HASH_BITS = 256
    }
}
