package com.word.file.manager.pdf.base.helper.notice

import com.word.file.manager.pdf.R
import com.word.file.manager.pdf.base.data.DocumentBookmark
import com.word.file.manager.pdf.base.data.DocumentOpenType
import com.word.file.manager.pdf.base.data.DocumentTools
import com.word.file.manager.pdf.base.data.PdfCreateType

object NoticeContentManager {

    private const val VIEW_CONTENT_NOTIFICATION_ID = 31000
    private const val VIEW_CLEANUP_NOTIFICATION_ID = 31001
    private const val HISTORY_CONTENT_NOTIFICATION_ID = 31002
    private const val SCAN_CONTENT_NOTIFICATION_ID = 31003
    private const val TOOLS_CONTENT_NOTIFICATION_ID = 31004

    private var orderedIndex = 0
    var remoteContentList: List<List<ContentItems>> = listOf()

    fun updateRemoteContentList(contentList: List<List<ContentItems>>) {
        remoteContentList = contentList
        orderedIndex = 0
    }

    fun getCurrentItems(): ContentItems? {
        val contentList = remoteContentList.ifEmpty { contentItemGroups }
        if (orderedIndex > contentList.lastIndex) orderedIndex = 0
        return contentList.getOrNull(orderedIndex++)?.randomOrNull()
    }

    private val viewContentItems: List<ContentItems> = listOf(
        ContentItems(
            message = R.string.content_1_type_1,
            button = R.string.view,
            actionType = DocumentOpenType,
            notificationId = VIEW_CONTENT_NOTIFICATION_ID,
        ),
        ContentItems(
            message = R.string.content_1_type_2,
            button = R.string.button_optimize,
            actionType = DocumentOpenType,
            notificationId = VIEW_CONTENT_NOTIFICATION_ID,
        ),
        ContentItems(
            message = R.string.content_1_type_3,
            button = R.string.open,
            actionType = DocumentOpenType,
            notificationId = VIEW_CONTENT_NOTIFICATION_ID,
        ),
        ContentItems(
            message = R.string.content_1_type_4,
            button = R.string.button_read,
            actionType = DocumentOpenType,
            notificationId = VIEW_CONTENT_NOTIFICATION_ID,
        ),
        ContentItems(
            message = R.string.content_1_type_5,
            button = R.string.button_check,
            actionType = DocumentOpenType,
            notificationId = VIEW_CONTENT_NOTIFICATION_ID,
        ),
    )

    private val viewCleanupContentItems: List<ContentItems> = listOf(
        ContentItems(
            message = R.string.content_2_type_1,
            button = R.string.delete,
            actionType = DocumentOpenType,
            notificationId = VIEW_CLEANUP_NOTIFICATION_ID,
        ),
        ContentItems(
            message = R.string.content_2_type_2,
            button = R.string.delete,
            actionType = DocumentOpenType,
            notificationId = VIEW_CLEANUP_NOTIFICATION_ID,
        ),
        ContentItems(
            message = R.string.content_2_type_3,
            button = R.string.delete,
            actionType = DocumentOpenType,
            notificationId = VIEW_CLEANUP_NOTIFICATION_ID,
        ),
        ContentItems(
            message = R.string.content_2_type_4,
            button = R.string.delete,
            actionType = DocumentOpenType,
            notificationId = VIEW_CLEANUP_NOTIFICATION_ID,
        ),
        ContentItems(
            message = R.string.content_2_type_5,
            button = R.string.delete,
            actionType = DocumentOpenType,
            notificationId = VIEW_CLEANUP_NOTIFICATION_ID,
        ),
    )

    private val historyContentItems: List<ContentItems> = listOf(
        ContentItems(
            message = R.string.content_3_type_1,
            button = R.string.button_resume,
            actionType = DocumentBookmark,
            notificationId = HISTORY_CONTENT_NOTIFICATION_ID,
        ),
        ContentItems(
            message = R.string.content_3_type_2,
            button = R.string.button_find,
            actionType = DocumentBookmark,
            notificationId = HISTORY_CONTENT_NOTIFICATION_ID,
        ),
        ContentItems(
            message = R.string.content_3_type_3,
            button = R.string.view,
            actionType = DocumentBookmark,
            notificationId = HISTORY_CONTENT_NOTIFICATION_ID,
        ),
        ContentItems(
            message = R.string.content_3_type_4,
            button = R.string.button_go,
            actionType = DocumentBookmark,
            notificationId = HISTORY_CONTENT_NOTIFICATION_ID,
        ),
    )

    private val scanContentItems: List<ContentItems> = listOf(
        ContentItems(
            message = R.string.content_4_type_1,
            button = R.string.scan,
            actionType = PdfCreateType,
            notificationId = SCAN_CONTENT_NOTIFICATION_ID,
        ),
        ContentItems(
            message = R.string.content_4_type_2,
            button = R.string.start,
            actionType = PdfCreateType,
            notificationId = SCAN_CONTENT_NOTIFICATION_ID,
        ),
        ContentItems(
            message = R.string.content_4_type_3,
            button = R.string.button_create,
            actionType = PdfCreateType,
            notificationId = SCAN_CONTENT_NOTIFICATION_ID,
        ),
        ContentItems(
            message = R.string.content_4_type_4,
            button = R.string.button_go,
            actionType = PdfCreateType,
            notificationId = SCAN_CONTENT_NOTIFICATION_ID,
        ),
    )

    private val toolsContentItems: List<ContentItems> = listOf(
        ContentItems(
            message = R.string.content_5_type_1,
            button = R.string.button_explore,
            actionType = DocumentTools,
            notificationId = TOOLS_CONTENT_NOTIFICATION_ID,
        ),
        ContentItems(
            message = R.string.content_5_type_2,
            button = R.string.button_try,
            actionType = DocumentTools,
            notificationId = TOOLS_CONTENT_NOTIFICATION_ID,
        ),
        ContentItems(
            message = R.string.content_5_type_3,
            button = R.string.button_protect,
            actionType = DocumentTools,
            notificationId = TOOLS_CONTENT_NOTIFICATION_ID,
        ),
        ContentItems(
            message = R.string.content_5_type_4,
            button = R.string.button_more,
            actionType = DocumentTools,
            notificationId = TOOLS_CONTENT_NOTIFICATION_ID,
        ),
    )

    val contentItemGroups: List<List<ContentItems>> = listOf(
        viewContentItems,
        viewCleanupContentItems,
        historyContentItems,
        scanContentItems,
        toolsContentItems,
    )
}
