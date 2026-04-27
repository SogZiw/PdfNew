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

    @Query("SELECT * FROM file_item_table WHERE recentViewTime > 0 ORDER BY recentViewTime DESC")
    fun getRecentFiles(): Flow<List<FileItem>>

    @Query("SELECT * FROM file_item_table WHERE isFavorite = 1 ORDER BY dateAdded DESC")
    fun getFavoriteFiles(): Flow<List<FileItem>>

    @Query("SELECT * FROM file_item_table WHERE filePath = :path LIMIT 1")
    suspend fun getFileByPath(path: String): FileItem?
}
