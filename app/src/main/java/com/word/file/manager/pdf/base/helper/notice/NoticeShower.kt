package com.word.file.manager.pdf.base.helper.notice

import com.word.file.manager.pdf.base.helper.AppLifeManger
import com.word.file.manager.pdf.base.helper.UserBlockHelper
import com.word.file.manager.pdf.modules.permissions.hasOverlayPermission
import com.word.file.manager.pdf.modules.permissions.hasPostNotificationPermission

object NoticeShower {

    fun showNotice(scene: NotificationScene) {
        if (AppLifeManger.isAppForeground()) return
        if (NoticeHelper.isWindowNoticeOpen && UserBlockHelper.canShowExtra() && hasOverlayPermission() && NoticeHelper.canShowReminder(scene, NoticeSurface.WINDOW)) {
            showBySurface(scene, NoticeSurface.WINDOW)
            return
        }
        if (hasPostNotificationPermission()) {
            if (NoticeHelper.canShowReminder(scene, NoticeSurface.NORMAL)) {
                showBySurface(scene, NoticeSurface.NORMAL)
            }
        } else {
            if (NoticeHelper.canShowReminder(scene, NoticeSurface.MEDIA)) {
                showBySurface(scene, NoticeSurface.MEDIA)
            }
        }
    }

    fun showBySurface(scene: NotificationScene, surface: NoticeSurface) {

    }

}