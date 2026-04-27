package com.word.file.manager.pdf.base.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.word.file.manager.pdf.R
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "file_item_table")
data class FileItem(
    var fileName: String,
    var filePath: String,
    val mimeType: String,
    val size: Long,
    val dateAdded: Long,
    var recentViewTime: Long = 0L,
    var isFavorite: Boolean = false,
    @PrimaryKey(autoGenerate = true) var uid: Long = 0L,
) : Parcelable

enum class FileCategory(val iconRes: Int) {
    Pdf(R.drawable.ic_file_pdf),
    Word(R.drawable.ic_file_word),
    Excel(R.drawable.ic_file_excel),
    Ppt(R.drawable.ic_file_ppt),
}
