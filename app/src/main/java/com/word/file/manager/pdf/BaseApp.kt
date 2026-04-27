package com.word.file.manager.pdf

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.word.file.manager.pdf.base.data.database.AppDatabase
import com.word.file.manager.pdf.modules.main.MainViewModel

class BaseApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    lateinit var mainViewModel: MainViewModel

    override fun onCreate() {
        super.onCreate()
        app = this
        mainViewModel = ViewModelProvider(
            ViewModelStore(),
            ViewModelProvider.AndroidViewModelFactory.getInstance(this),
        )[MainViewModel::class.java]
    }

}