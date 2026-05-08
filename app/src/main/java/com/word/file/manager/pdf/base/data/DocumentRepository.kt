package com.word.file.manager.pdf.base.data

import android.content.Context
import com.word.file.manager.pdf.base.data.database.AppDatabase
import com.word.file.manager.pdf.base.utils.deleteFileItem
import com.word.file.manager.pdf.base.utils.registerCreatedFile
import com.word.file.manager.pdf.base.utils.querySupportedFiles
import com.word.file.manager.pdf.base.utils.renameFileItem
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
import java.io.File

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
        if (_allFiles.value.isNotEmpty()) {
            _showPermissionGuide.value = false
            return
        }
        scope.launch {
            val storedFiles = database.fileItemDao().getAllFiles().associateBy { it.absolutePath }
            _allFiles.value = querySupportedFiles(context).map { scannedItem ->
                val storedItem = storedFiles[scannedItem.absolutePath]
                if (storedItem == null) {
                    scannedItem
                } else {
                    scannedItem.copy(
                        recordId = storedItem.recordId,
                        lastViewedAtMillis = storedItem.lastViewedAtMillis,
                        collectedFlag = storedItem.collectedFlag,
                        encryptedFlag = scannedItem.encryptedFlag,
                    )
                }
            }
            _showPermissionGuide.value = false
        }
    }

    suspend fun toggleFavorite(fileItem: FileItem): Boolean {
        return withContext(Dispatchers.IO) {
            val storedItem = database.fileItemDao().getFileByPath(fileItem.absolutePath)
            val targetItem = (storedItem ?: fileItem).copy()
            val nextFavoriteState = !(storedItem?.collectedFlag ?: fileItem.collectedFlag)
            targetItem.collectedFlag = nextFavoriteState
            database.fileItemDao().upsert(targetItem)
            _allFiles.update { files ->
                files.map { item ->
                    if (item.absolutePath == fileItem.absolutePath) {
                        item.copy(collectedFlag = nextFavoriteState)
                    } else {
                        item
                    }
                }
            }
            nextFavoriteState
        }
    }

    suspend fun renameDocument(fileItem: FileItem, rawName: String): FileItem? {
        return withContext(Dispatchers.IO) {
            val renamedItem = renameFileItem(fileItem, rawName) ?: return@withContext null
            val storedItem = database.fileItemDao().getFileByPath(fileItem.absolutePath)
            val targetItem = (storedItem ?: fileItem).copy(
                recordId = storedItem?.recordId ?: fileItem.recordId,
                documentTitle = renamedItem.documentTitle,
                absolutePath = renamedItem.absolutePath,
            )
            database.fileItemDao().upsert(targetItem)
            _allFiles.update { files ->
                files.map { item ->
                    if (item.absolutePath == fileItem.absolutePath) targetItem else item
                }
            }
            targetItem
        }
    }

    suspend fun deleteDocument(fileItem: FileItem): Boolean {
        return withContext(Dispatchers.IO) {
            val storedItem = database.fileItemDao().getFileByPath(fileItem.absolutePath) ?: fileItem
            val deleted = deleteFileItem(fileItem)
            if (deleted) {
                if (storedItem.recordId != 0L) {
                    database.fileItemDao().delete(storedItem)
                }
                _allFiles.update { files ->
                    files.filterNot { it.absolutePath == fileItem.absolutePath }
                }
            }
            deleted
        }
    }

    suspend fun registerCreatedPdf(file: File): FileItem {
        return withContext(Dispatchers.IO) {
            val createdItem = registerCreatedFile(file)
            database.fileItemDao().upsert(createdItem)
            _allFiles.update { files ->
                listOf(createdItem) + files.filterNot { it.absolutePath == createdItem.absolutePath }
            }
            createdItem
        }
    }

    suspend fun registerToolOutputPdf(file: File): FileItem {
        return withContext(Dispatchers.IO) {
            val outputItem = registerCreatedFile(file).copy(lastViewedAtMillis = System.currentTimeMillis())
            database.fileItemDao().upsert(outputItem)
            _allFiles.update { files ->
                listOf(outputItem) + files.filterNot { it.absolutePath == outputItem.absolutePath }
            }
            outputItem
        }
    }

    suspend fun updatePdfSecurityState(fileItem: FileItem, encrypted: Boolean): FileItem {
        return withContext(Dispatchers.IO) {
            val sourceFile = File(fileItem.absolutePath)
            val storedItem = database.fileItemDao().getFileByPath(fileItem.absolutePath)
            val baseItem = storedItem ?: fileItem
            val updatedItem = baseItem.copy(
                fileBytes = sourceFile.length().takeIf { it > 0L } ?: baseItem.fileBytes,
                encryptedFlag = encrypted,
                lastViewedAtMillis = System.currentTimeMillis(),
            )
            database.fileItemDao().upsert(updatedItem)
            _allFiles.update { files ->
                files.map { item ->
                    if (item.absolutePath == updatedItem.absolutePath) updatedItem else item
                }
            }
            updatedItem
        }
    }
}
