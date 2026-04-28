package com.word.file.manager.pdf.base.utils

import android.content.Context
import android.content.Intent
import android.content.Context.PRINT_SERVICE
import android.media.MediaScannerConnection
import android.net.Uri
import android.print.PrintManager
import android.provider.MediaStore
import android.text.format.Formatter
import androidx.core.content.FileProvider
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import com.word.file.manager.pdf.app
import com.word.file.manager.pdf.base.data.FileCategory
import com.word.file.manager.pdf.base.data.FileItem
import com.word.file.manager.pdf.base.data.FileTabFilter
import com.word.file.manager.pdf.base.helper.PdfPrintAdapter
import com.artifex.mupdf.fitz.Document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.random.Random
import androidx.core.net.toUri

val supportedMimeTypes: Map<String, FileCategory>
    get() = mapOf(
        "application/pdf" to FileCategory.Pdf,
        "application/msword" to FileCategory.Word,
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document" to FileCategory.Word,
        "application/vnd.openxmlformats-officedocument.wordprocessingml.template" to FileCategory.Word,
        "application/vnd.ms-excel" to FileCategory.Excel,
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" to FileCategory.Excel,
        "application/vnd.openxmlformats-officedocument.spreadsheetml.template" to FileCategory.Excel,
        "application/vnd.ms-powerpoint" to FileCategory.Ppt,
        "application/vnd.openxmlformats-officedocument.presentationml.presentation" to FileCategory.Ppt,
        "application/vnd.openxmlformats-officedocument.presentationml.template" to FileCategory.Ppt,
        "application/vnd.openxmlformats-officedocument.presentationml.slideshow" to FileCategory.Ppt,
    )

fun FileItem.getFileCategory(): FileCategory? = supportedMimeTypes[contentMime]

fun FileItem.matchesFilter(filter: FileTabFilter): Boolean {
    return when (filter) {
        FileTabFilter.All -> true
        FileTabFilter.Pdf -> getFileCategory() == FileCategory.Pdf
        FileTabFilter.Word -> getFileCategory() == FileCategory.Word
        FileTabFilter.Excel -> getFileCategory() == FileCategory.Excel
        FileTabFilter.Ppt -> getFileCategory() == FileCategory.Ppt
    }
}

fun Long.formatFileDate(pattern: String = "yyyy/MM/dd HH:mm"): String {
    return runCatching { SimpleDateFormat(pattern, Locale.getDefault()).format(this) }.getOrDefault("")
}

fun FileItem.buildInfoText(context: Context): String {
    return "${createdAtMillis.formatFileDate()} ${Formatter.formatFileSize(context, fileBytes)}"
}

fun querySupportedFiles(context: Context): List<FileItem> {
    val result = mutableListOf<FileItem>()
    val uri: Uri = MediaStore.Files.getContentUri("external")
    val projection = arrayOf(
        MediaStore.Files.FileColumns.DISPLAY_NAME,
        MediaStore.Files.FileColumns.DATA,
        MediaStore.Files.FileColumns.MIME_TYPE,
        MediaStore.Files.FileColumns.SIZE,
        MediaStore.Files.FileColumns.DATE_ADDED,
    )
    val selection = buildString {
        append("${MediaStore.Files.FileColumns.MIME_TYPE} IN (")
        append(supportedMimeTypes.keys.joinToString(",") { "?" })
        append(")")
    }
    context.contentResolver.query(
        uri,
        projection,
        selection,
        supportedMimeTypes.keys.toTypedArray(),
        "${MediaStore.Files.FileColumns.DATE_ADDED} DESC",
    )?.use { cursor ->
        val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
        val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
        val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
        val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
        val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
        while (cursor.moveToNext()) {
            val path = cursor.getStringOrNull(dataColumn).orEmpty()
            val file = File(path)
            val size = cursor.getLongOrNull(sizeColumn) ?: 0L
            if (!file.exists() || size <= 0L) continue
            val fileCategory = supportedMimeTypes[cursor.getStringOrNull(mimeColumn).orEmpty()]
            result.add(
                FileItem(
                    documentTitle = cursor.getStringOrNull(nameColumn).orEmpty(),
                    absolutePath = path,
                    contentMime = cursor.getStringOrNull(mimeColumn).orEmpty(),
                    fileBytes = size,
                    createdAtMillis = (cursor.getLongOrNull(dateColumn) ?: 0L) * 1000L,
                    encryptedFlag = fileCategory == FileCategory.Pdf && isPdfEncrypt(path),
                ),
            )
        }
    }
    return result
}

fun Context.openFileBySystem(item: FileItem) {
    runCatching {
        startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(createFileUri(this@openFileBySystem, item.absolutePath), item.contentMime.ifBlank { "*/*" })
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            },
        )
    }.onFailure {
        showMessageToast(getString(com.word.file.manager.pdf.R.string.common_error_message))
    }
}

fun Context.shareFile(item: FileItem) {
    runCatching {
        startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = item.contentMime.ifBlank { "*/*" }
                    putExtra(Intent.EXTRA_STREAM, createFileUri(this@shareFile, item.absolutePath))
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                },
                getString(com.word.file.manager.pdf.R.string.share),
            ),
        )
    }.onFailure {
        showMessageToast(getString(com.word.file.manager.pdf.R.string.common_error_message))
    }
}

fun Context.printPdf(item: FileItem) {
    val printManager = getSystemService(PRINT_SERVICE) as? PrintManager ?: return
    printManager.print("Print_PDF_Job", PdfPrintAdapter(item), null)
}

private fun createFileUri(context: Context, path: String): Uri {
    return if (path.startsWith("/")) {
        FileProvider.getUriForFile(context, "${context.packageName}.fileProvider", File(path))
    } else path.toUri()
}

suspend fun markFileAsRecent(item: FileItem) {
    withContext(Dispatchers.IO) {
        val dbItem = app.database.fileItemDao().getFileByPath(item.absolutePath) ?: item
        dbItem.lastViewedAtMillis = System.currentTimeMillis()
        app.database.fileItemDao().upsert(dbItem)
    }
}

fun isPdfEncrypt(filePath: String): Boolean {
    return runCatching {
        val document = Document.openDocument(filePath)
        val needsPassword = document.needsPassword()
        document.destroy()
        needsPassword
    }.getOrDefault(false)
}

fun isPdfPasswordRequired(filePath: String): Boolean = isPdfEncrypt(filePath)

fun isPdfPasswordValid(filePath: String, password: String): Boolean {
    return runCatching {
        val document = Document.openDocument(filePath)
        val valid = document.authenticatePassword(password)
        document.destroy()
        valid
    }.getOrDefault(false)
}

suspend fun renameFileItem(fileItem: FileItem, rawName: String): FileItem? {
    return withContext(Dispatchers.IO) {
        val originalFile = File(fileItem.absolutePath)
        val safeName = rawName.replace(Regex("[\\\\/:*?\"<>|\\x00]"), "_").trim()
        if (safeName.isBlank()) return@withContext null
        val extension = originalFile.extension
        val targetName = if (extension.isBlank()) safeName else "$safeName.$extension"
        var targetFile = File(originalFile.parentFile, targetName)
        if (targetFile.exists()) {
            val suffixName = if (extension.isBlank()) {
                "${safeName}${Random.nextInt(1, 10000)}"
            } else {
                "${safeName}${Random.nextInt(1, 10000)}.$extension"
            }
            targetFile = File(originalFile.parentFile, suffixName)
        }
        if (!originalFile.renameTo(targetFile)) return@withContext null
        MediaScannerConnection.scanFile(app, arrayOf(targetFile.absolutePath), null, null)
        val displayName = targetFile.name
        fileItem.copy(
            documentTitle = displayName,
            absolutePath = targetFile.absolutePath,
        )
    }
}

suspend fun deleteFileItem(fileItem: FileItem): Boolean {
    return withContext(Dispatchers.IO) {
        val targetFile = File(fileItem.absolutePath)
        val deleted = targetFile.delete()
        if (deleted) {
            MediaScannerConnection.scanFile(app, arrayOf(targetFile.absolutePath), null, null)
        }
        deleted
    }
}
