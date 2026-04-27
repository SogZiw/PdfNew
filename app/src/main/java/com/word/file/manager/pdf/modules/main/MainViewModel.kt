package com.word.file.manager.pdf.modules.main

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.word.file.manager.pdf.app
import com.word.file.manager.pdf.base.data.FileItem
import com.word.file.manager.pdf.base.utils.querySupportedFiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    val requestStorageLiveData = MutableLiveData<Boolean>()
    val changePermissionVisible = MutableLiveData<Boolean>()
    val onScanResultLiveData = MutableLiveData<List<FileItem>>()
    val onRecentUpdateLiveData = MutableLiveData<List<FileItem>>()
    val onBookmarkUpdateLiveData = MutableLiveData<List<FileItem>>()
    var allFiles: List<FileItem> = emptyList()

    fun refreshFiles(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            allFiles = querySupportedFiles(context)
            changePermissionVisible.postValue(false)
            onScanResultLiveData.postValue(allFiles)
        }
    }

    fun collectRecentFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            app.database.fileItemDao().getRecentFiles().collect {
                onRecentUpdateLiveData.postValue(it)
            }
        }
    }

    fun collectBookmarkFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            app.database.fileItemDao().getFavoriteFiles().collect {
                onBookmarkUpdateLiveData.postValue(it)
            }
        }
    }
}
