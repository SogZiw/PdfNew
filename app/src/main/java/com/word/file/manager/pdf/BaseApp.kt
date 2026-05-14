package com.word.file.manager.pdf

import android.app.Application
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.google.android.gms.ads.MobileAds
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.word.file.manager.pdf.base.data.DocumentRepository
import com.word.file.manager.pdf.base.data.database.AppDatabase
import com.word.file.manager.pdf.base.helper.AppLifeManger
import com.word.file.manager.pdf.base.helper.InstallReferrerHelper
import com.word.file.manager.pdf.base.helper.LocalPrefs
import com.word.file.manager.pdf.base.helper.net.BaseInfo
import com.word.file.manager.pdf.base.helper.net.NetworkCenter
import com.word.file.manager.pdf.base.helper.notice.NoticeShower
import com.word.file.manager.pdf.base.helper.services.CoreService
import com.word.file.manager.pdf.base.helper.remote.RemoteConfProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BaseApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val documentRepository by lazy { DocumentRepository(database) }

    override fun onCreate() {
        super.onCreate()
        app = this
        registerActivityLifecycleCallbacks(AppLifeManger)
        CoroutineScope(Dispatchers.IO).launch { MobileAds.initialize(this@BaseApp) }
        PDFBoxResourceLoader.init(this)
        initAdjust()
        RemoteConfProvider.init()
        firstSubscribeFMS()
        InstallReferrerHelper.fetch()
        NetworkCenter.cloak()
        CoreService.showToolbar(this)
        NoticeShower.startListeners()
    }

    private fun initAdjust() {
        Adjust.addGlobalCallbackParameter("customer_user_id", BaseInfo.deviceId)
        val config = AdjustConfig(this, "", if (isDebug) AdjustConfig.ENVIRONMENT_SANDBOX else AdjustConfig.ENVIRONMENT_PRODUCTION)
        Adjust.initSdk(config)
    }

    private fun firstSubscribeFMS() {
        if (isDebug || LocalPrefs.hasSubscribeFMS) return
        runCatching { Firebase.messaging.subscribeToTopic("PDF_agile").addOnSuccessListener { LocalPrefs.hasSubscribeFMS = true } }
    }

}
