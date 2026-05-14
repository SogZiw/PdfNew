package com.word.file.manager.pdf.base.helper.notice

object NoticeHelper {

    var isNoticeOpen: Boolean = true
    var blockStartHour: Int = 0
    var blockEndHour: Int = 0

    var timeConfig: NfConfigItem? = null
    var unlockConfig: NfConfigItem? = null
    var alarmConfig: NfConfigItem? = null

    var isMediaNoticeOpen: Boolean = false
    var mediaTimeConfig: NfConfigItem? = null
    var mediaUnlockConfig: NfConfigItem? = null
    var mediaAlarmConfig: NfConfigItem? = null

    var isWindowNoticeOpen: Boolean = false
    var userNoticePercent: Int = 50
    var windowTimeConfig: NfConfigItem? = null
    var windowUnlockConfig: NfConfigItem? = null
}
