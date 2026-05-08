package com.word.file.manager.pdf.base.helper.ad.util

import android.os.Build
import android.util.Base64
import com.word.file.manager.pdf.app
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object NativeAdPreviewChecker {

    private const val SECRET_KEY = "A6mP9qL2xV7sR4tZ"
    private const val TEST_HEADLINE = "3QKbbTJCG5ztLhnRuOX0ssqoSF7hUK2Pa701tz4IXVkcd40="
    private const val FALLBACK_NAME = "VxJBU31ww48qcO641BcEpgZGLMLuX4AGfJbqN4CB"
    private const val RESOURCE_TYPE = "ahmcBtXoMx0KpFs44/grtFeIujfVJGz0+Hz7PQXEa8e8pw=="

    fun isPreviewUser(headline: String?): Boolean {
        val title = headline?.trim().orEmpty()
        if (title.isEmpty()) return false
        if (isLikelyGoogleDevice()) return true

        val encryptedPrefix = decryptText(TEST_HEADLINE)
        if (title.startsWith(encryptedPrefix, ignoreCase = true)) return true

        val resourcePrefix = readResourcePrefix()
        return resourcePrefix.isNotBlank() && title.startsWith(resourcePrefix, ignoreCase = true)
    }

    private fun readResourcePrefix(): String {
        return runCatching {
            val name = decryptText(FALLBACK_NAME)
            val type = decryptText(RESOURCE_TYPE)
            val id = app.resources.getIdentifier(name, type, app.packageName)
            app.getString(id)
        }.getOrDefault("")
    }

    private fun decryptText(payload: String): String {
        val data = Base64.decode(payload, Base64.NO_WRAP)
        val iv = data.copyOfRange(0, 12)
        val encrypted = data.copyOfRange(12, data.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.DECRYPT_MODE,
            SecretKeySpec(SECRET_KEY.toByteArray(Charsets.UTF_8), "AES"),
            GCMParameterSpec(128, iv),
        )
        return cipher.doFinal(encrypted).toString(Charsets.UTF_8)
    }

    private fun isLikelyGoogleDevice(): Boolean {
        val brand = Build.BRAND.asDeviceValue()
        val device = Build.DEVICE.asDeviceValue()
        if (brand.startsWith("generic") && device.startsWith("generic")) return true

        val fingerprint = Build.FINGERPRINT.asDeviceValue()
        val hardware = Build.HARDWARE.asDeviceValue()
        val model = Build.MODEL.asDeviceValue()
        val manufacturer = Build.MANUFACTURER.asDeviceValue()
        val host = Build.HOST.asDeviceValue()
        val product = Build.PRODUCT.asDeviceValue()

        return fingerprint.hasAny("generic", "unknown") ||
                hardware.hasAny("goldfish", "ranchu") ||
                model.hasAny("google_sdk", "emulator", "android sdk built for x86") ||
                manufacturer.hasAny("genymotion") ||
                host.startsWith("build") ||
                product.hasAny("sdk_google", "google_sdk", "sdk", "sdk_x86", "vbox86p", "emulator", "simulator")
    }

    private fun String?.asDeviceValue(): String = orEmpty().lowercase(Locale.US)

    private fun String.hasAny(vararg parts: String): Boolean {
        return parts.any { contains(it) }
    }
}
