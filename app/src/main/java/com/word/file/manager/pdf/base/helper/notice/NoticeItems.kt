package com.word.file.manager.pdf.base.helper.notice

import android.content.Context
import com.word.file.manager.pdf.base.data.DocumentActionType

data class ContentItems(
    val message: Int,
    val button: Int,
    val actionType: DocumentActionType,
    val notificationId: Int,
    val messageText: String = "",
    val buttonText: String = "",
) {
    fun resolveMessage(context: Context): String {
        return messageText.ifBlank { context.getString(message) }
    }

    fun resolveButton(context: Context): String {
        return buttonText.ifBlank { context.getString(button) }
    }
}

data class NfConfigItem(
    val first: Int,
    val interval: Int,
    val maxCounts: Int,
)

data class NoticeShowRecord(
    val lastShowTime: Long,
    val dailyShowCount: Int,
    val dailyCountTime: Long,
)

enum class NotificationScene(val sceneName: String) {
    UNLOCK("unlock"),
    TIME("time"),
    ALARM("alarm"),
}

enum class NoticeSurface(val storeName: String) {
    NORMAL("normal"),
    MEDIA("media"),
    WINDOW("window"),
}
