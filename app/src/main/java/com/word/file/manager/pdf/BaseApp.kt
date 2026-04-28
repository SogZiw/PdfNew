package com.word.file.manager.pdf

import android.app.Application
import com.word.file.manager.pdf.base.data.DocumentRepository
import com.word.file.manager.pdf.base.data.database.AppDatabase

class BaseApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val documentRepository by lazy { DocumentRepository(database) }

    override fun onCreate() {
        super.onCreate()
        app = this
    }

}
