package com.word.file.manager.pdf.base.data

import android.content.Context
import com.word.file.manager.pdf.base.data.database.AppDatabase
import com.word.file.manager.pdf.base.utils.querySupportedFiles
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DocumentRepository(private val database: AppDatabase) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _requestStorageAccess = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val requestStorageAccess: SharedFlow<Unit> = _requestStorageAccess.asSharedFlow()

    private val _showPermissionGuide = MutableStateFlow(false)
    val showPermissionGuide: StateFlow<Boolean> = _showPermissionGuide.asStateFlow()

    private val _allFiles = MutableStateFlow<List<FileItem>>(emptyList())
    val allFiles: StateFlow<List<FileItem>> = _allFiles.asStateFlow()

    private val _recentFiles = MutableStateFlow<List<FileItem>>(emptyList())
    val recentFiles: StateFlow<List<FileItem>> = _recentFiles.asStateFlow()

    private val _bookmarkFiles = MutableStateFlow<List<FileItem>>(emptyList())
    val bookmarkFiles: StateFlow<List<FileItem>> = _bookmarkFiles.asStateFlow()

    init {
        scope.launch {
            database.fileItemDao().getRecentFiles().collect {
                _recentFiles.value = it
            }
        }
        scope.launch {
            database.fileItemDao().getFavoriteFiles().collect {
                _bookmarkFiles.value = it
            }
        }
    }

    fun requestStoragePermission() {
        _requestStorageAccess.tryEmit(Unit)
    }

    fun updatePermissionGuide(visible: Boolean) {
        _showPermissionGuide.value = visible
    }

    fun refreshFiles(context: Context) {
        scope.launch {
            _allFiles.value = querySupportedFiles(context)
            _showPermissionGuide.value = false
        }
    }

    suspend fun toggleFavorite(fileItem: FileItem): Boolean {
        return withContext(Dispatchers.IO) {
            val storedItem = database.fileItemDao().getFileByPath(fileItem.filePath)
            val targetItem = (storedItem ?: fileItem).copy()
            val nextFavoriteState = !(storedItem?.isFavorite ?: fileItem.isFavorite)
            targetItem.isFavorite = nextFavoriteState
            database.fileItemDao().upsert(targetItem)
            _allFiles.update { files ->
                files.map { item ->
                    if (item.filePath == fileItem.filePath) {
                        item.copy(isFavorite = nextFavoriteState)
                    } else {
                        item
                    }
                }
            }
            nextFavoriteState
        }
    }
}
