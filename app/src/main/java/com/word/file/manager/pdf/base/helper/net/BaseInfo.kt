package com.word.file.manager.pdf.base.helper.net

import android.os.Build
import com.word.file.manager.pdf.BuildConfig
import com.word.file.manager.pdf.app
import com.word.file.manager.pdf.base.helper.LocalPrefs
import com.word.file.manager.pdf.isDebug
import org.json.JSONObject
import java.util.Locale
import java.util.UUID

object BaseInfo {

    val baseUrl by lazy { if (isDebug) "https://test-organic.agilepdfview.com/ceramic/regis/exegesis" else "https://organic.agilepdfview.com/james/passive/quest" }
    val cloakUrl by lazy { "https://smallpox.agilepdfview.com/march/mecum/label" }
    val firstDeviceCountry by lazy { buildFirstCountryCode() }
    val deviceId by lazy { buildDeviceId() }

    fun buildObj(): JSONObject {
        return JSONObject().apply {
            put("mist", app.packageName)
            put("penguin", "vacate")
            put("nitride", BuildConfig.VERSION_NAME)
            put("cold", deviceId)
            put("betide", UUID.randomUUID().toString())
            put("nebular", System.currentTimeMillis())
            put("weaken", Build.MANUFACTURER ?: "")
            put("stub", Build.BRAND ?: "")
            put("whipple", Build.MODEL ?: "")
            put("shako", Build.VERSION.RELEASE ?: "")
            put("longhand", "")
            put("cattle", Locale.getDefault().toString())
            put("lentil", firstDeviceCountry)
        }
    }

    private fun buildFirstCountryCode(): String = LocalPrefs.userFirstCountryCode.ifBlank {
        val countryCode = Locale.getDefault().country
        LocalPrefs.userFirstCountryCode = countryCode
        return@ifBlank countryCode
    }

    private fun buildDeviceId(): String = LocalPrefs.userDeviceId.ifBlank {
        val uuid = UUID.randomUUID().toString()
        LocalPrefs.userDeviceId = uuid
        return@ifBlank uuid
    }


}