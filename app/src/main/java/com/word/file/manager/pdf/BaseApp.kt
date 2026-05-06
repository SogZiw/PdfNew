package com.word.file.manager.pdf

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.word.file.manager.pdf.base.data.DocumentRepository
import com.word.file.manager.pdf.base.data.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BaseApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val documentRepository by lazy { DocumentRepository(database) }

    override fun onCreate() {
        super.onCreate()
        app = this
        CoroutineScope(Dispatchers.IO).launch { MobileAds.initialize(this@BaseApp) }
        PDFBoxResourceLoader.init(this)
    }

}
