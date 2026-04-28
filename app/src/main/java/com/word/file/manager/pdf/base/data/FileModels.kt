package com.word.file.manager.pdf.base.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.word.file.manager.pdf.R
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "doc_entry_records")
data class FileItem(
    @ColumnInfo(name = "display_title")
    var documentTitle: String,
    @ColumnInfo(name = "storage_uri")
    var absolutePath: String,
    @ColumnInfo(name = "media_kind")
    val contentMime: String,
    @ColumnInfo(name = "byte_size")
    val fileBytes: Long,
    @ColumnInfo(name = "created_epoch_ms")
    val createdAtMillis: Long,
    @ColumnInfo(name = "lock_state")
    var encryptedFlag: Boolean = false,
    @ColumnInfo(name = "last_opened_epoch_ms")
    var lastViewedAtMillis: Long = 0L,
    @ColumnInfo(name = "saved_flag")
    var collectedFlag: Boolean = false,
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "local_id")
    var recordId: Long = 0L,
) : Parcelable

enum class FileCategory(val iconRes: Int) {
    Pdf(R.drawable.ic_file_pdf),
    Word(R.drawable.ic_file_word),
    Excel(R.drawable.ic_file_excel),
    Ppt(R.drawable.ic_file_ppt),
}
