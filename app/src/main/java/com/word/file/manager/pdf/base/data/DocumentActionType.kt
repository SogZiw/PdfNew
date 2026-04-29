package com.word.file.manager.pdf.base.data

import android.content.Context
import android.os.Parcelable
import androidx.annotation.DrawableRes
import com.word.file.manager.pdf.R
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class DocumentActionType : Parcelable {
    open fun getActionName(context: Context): String = ""
    @DrawableRes
    open fun getMenuIconRes(): Int = R.drawable.ic_menu_open
}

data object DocumentOpenType : DocumentActionType() {
    override fun getActionName(context: Context): String = "view"
    override fun getMenuIconRes(): Int = R.drawable.ic_menu_open
}

data object PdfMergeType : DocumentActionType() {
    override fun getActionName(context: Context): String = context.getString(R.string.merge_pdf)
    override fun getMenuIconRes(): Int = R.drawable.ic_menu_merge_pdf
}

data object PdfSplitType : DocumentActionType() {
    override fun getActionName(context: Context): String = context.getString(R.string.split_pdf)
    override fun getMenuIconRes(): Int = R.drawable.ic_menu_pdf_split
}

data object PdfLockType : DocumentActionType() {
    override fun getActionName(context: Context): String = context.getString(R.string.lock_pdf)
    override fun getMenuIconRes(): Int = R.drawable.ic_menu_lock
}

data object PdfUnlockType : DocumentActionType() {
    override fun getActionName(context: Context): String = context.getString(R.string.unlock_pdf)
    override fun getMenuIconRes(): Int = R.drawable.ic_menu_unlock
}

data object PdfPrintType : DocumentActionType() {
    override fun getActionName(context: Context): String = context.getString(R.string.print)
    override fun getMenuIconRes(): Int = R.drawable.ic_menu_print_pdf
}

data object PdfCreateType : DocumentActionType() {
    override fun getActionName(context: Context): String = context.getString(R.string.create_pdf)
}
