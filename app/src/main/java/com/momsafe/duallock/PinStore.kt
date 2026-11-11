package com.momsafe.duallock

import android.content.Context
import java.security.MessageDigest
import java.security.SecureRandom

internal class PinStore(context: Context) {
    private val prefs = context.getSharedPreferences("dual_lock_pins", Context.MODE_PRIVATE)

    fun isConfigured(): Boolean =
        prefs.contains("owner_hash") && prefs.contains("mom_hash") && prefs.contains("salt")

    fun save(ownerPin: String, momPin: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        prefs.edit()
            .putString("salt", salt.toHex())
            .putString("owner_hash", hash(ownerPin, salt))
            .putString("mom_hash", hash(momPin, salt))
            .apply()
    }

    fun isOwner(pin: String): Boolean = matches("owner_hash", pin)
    fun isMom(pin: String): Boolean = matches("mom_hash", pin)

    private fun matches(key: String, pin: String): Boolean {
        val salt = prefs.getString("salt", null)?.hexToBytes() ?: return false
        val expected = prefs.getString(key, null) ?: return false
        return MessageDigest.isEqual(
            expected.toByteArray(Charsets.UTF_8),
            hash(pin, salt).toByteArray(Charsets.UTF_8)
        )
    }

    private fun hash(pin: String, salt: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(salt + pin.toByteArray(Charsets.UTF_8)).toHex()
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun String.hexToBytes(): ByteArray =
        chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}
