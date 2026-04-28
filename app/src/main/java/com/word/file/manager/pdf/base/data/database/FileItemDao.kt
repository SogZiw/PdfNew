package com.word.file.manager.pdf.base.data.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.word.file.manager.pdf.base.data.FileItem
import kotlinx.coroutines.flow.Flow

@Dao
interface FileItemDao {

    @Upsert
    suspend fun upsert(item: FileItem)

    @Query("SELECT * FROM doc_entry_records")
    suspend fun getAllFiles(): List<FileItem>

    @Query("SELECT * FROM doc_entry_records WHERE last_opened_epoch_ms > 0 ORDER BY last_opened_epoch_ms DESC")
    fun getRecentFiles(): Flow<List<FileItem>>

    @Query("SELECT * FROM doc_entry_records WHERE saved_flag = 1 ORDER BY created_epoch_ms DESC")
    fun getFavoriteFiles(): Flow<List<FileItem>>

    @Query("SELECT * FROM doc_entry_records WHERE storage_uri = :path LIMIT 1")
    suspend fun getFileByPath(path: String): FileItem?
}
