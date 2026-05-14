package com.word.file.manager.pdf.base.helper.notice

import com.word.file.manager.pdf.base.data.DocumentActionType

data class ContentItems(
    val message: Int,
    val button: Int,
    val actionType: DocumentActionType,
    val notificationId: Int,
)

data class NfConfigItem(
    val first: Int,
    val interval: Int,
    val maxCounts: Int,
)

enum class NotificationScene(val sceneName: String) {
    UNLOCK("unlock"),
    TIME("time"),
    ALARM("alarm"),
}