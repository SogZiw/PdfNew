package com.word.file.manager.pdf.base.helper

import android.os.Bundle
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.analytics.FirebaseAnalytics
import com.word.file.manager.pdf.app
import com.word.file.manager.pdf.base.helper.net.NetworkCenter
import com.word.file.manager.pdf.base.utils.showLog
import com.word.file.manager.pdf.isDebug
import java.util.Currency

object EventCenter {

    private val firebaseAnalytics by lazy { FirebaseAnalytics.getInstance(app) }
    private val facebookLogger by lazy { AppEventsLogger.newLogger(app) }

    fun logEvent(
        eventName: String,
        params: Map<String, Any?> = emptyMap(),
        withFirebase: Boolean = false,
    ) {
        "$eventName:$params".showLog("EventCenter")
        if (withFirebase && !isDebug) {
            firebaseAnalytics.logEvent(eventName, params.toFirebaseBundle())
        }
        NetworkCenter.customEvent(eventName, params)
    }

    fun facebookPurchase(revenue: Double, currencyCode: String) {
        if (isDebug) return
        runCatching {
            facebookLogger.logPurchase(revenue.toBigDecimal(), Currency.getInstance(currencyCode))
        }
    }

    private fun Map<String, Any?>.toFirebaseBundle(): Bundle {
        return Bundle().apply {
            this@toFirebaseBundle.forEach { (key, value) -> putEventParam(key, value) }
        }
    }

    private fun Bundle.putEventParam(key: String, value: Any?) {
        when (value) {
            null -> return
            is String -> putString(key, value)
            is Int -> putInt(key, value)
            is Long -> putLong(key, value)
            is Double -> putDouble(key, value)
            is Float -> putFloat(key, value)
            is Boolean -> putBoolean(key, value)
            else -> putString(key, value.toString())
        }
    }
}
