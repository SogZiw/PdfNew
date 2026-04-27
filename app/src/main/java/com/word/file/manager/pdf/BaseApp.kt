package com.word.file.manager.pdf

import android.app.Application

class BaseApp : Application() {

    override fun onCreate() {
        super.onCreate()
        app = this
    }

}