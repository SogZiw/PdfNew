package com.word.file.manager.pdf.base.helper.notice.extra

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import com.word.file.manager.pdf.EXTRA_DOCUMENT_ACTION_TYPE
import com.word.file.manager.pdf.EXTRA_NOTIFICATION_SCENE
import com.word.file.manager.pdf.EXTRA_NOTIFICATION_SURFACE
import com.word.file.manager.pdf.app
import com.word.file.manager.pdf.base.data.DocumentActionType
import com.word.file.manager.pdf.base.helper.EventCenter
import com.word.file.manager.pdf.base.helper.LocalPrefs.isFirstMistouch
import com.word.file.manager.pdf.base.helper.ad.center.AdCenter
import com.word.file.manager.pdf.base.helper.notice.ContentItems
import com.word.file.manager.pdf.base.helper.notice.NoticeHelper
import com.word.file.manager.pdf.base.helper.notice.NoticeShower
import com.word.file.manager.pdf.base.helper.notice.NoticeSurface
import com.word.file.manager.pdf.base.helper.notice.NoticeUtils
import com.word.file.manager.pdf.base.helper.notice.NotificationScene
import com.word.file.manager.pdf.base.utils.dpToPx
import com.word.file.manager.pdf.base.utils.startButtonAnimation
import com.word.file.manager.pdf.databinding.LayoutExtraViewBinding
import com.word.file.manager.pdf.modules.RouteActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

@SuppressLint("StaticFieldLeak")
object ExtraBuilder {

    private var overlayView: View? = null
    private val marginOffset by lazy { 20.dpToPx(app) }
    private var isNeedForceMistouch = false
    private val mutex = Mutex()

    fun showExtraView(content: ContentItems, scene: NotificationScene, surface: NoticeSurface) {
        if (overlayView != null) return
        EventCenter.logEvent("notification_win_show", mapOf("list" to scene.sceneName))
        NoticeHelper.updateShowRecord(scene, surface)
        isNeedForceMistouch = Random.nextInt(0, 101) <= NoticeHelper.userNoticePercent
        if (isFirstMistouch || isNeedForceMistouch) {
            AdCenter.appOpen.preload()
        }
        NoticeShower.workScope.launch(Dispatchers.Main) {
            mutex.withLock {
                val viewBinding = LayoutExtraViewBinding.inflate(LayoutInflater.from(app))
                overlayView = viewBinding.root
                NoticeUtils.addViewByReflection(app, overlayView, marginOffset)
                viewBinding.imageIcon.setImageResource(content.actionType.getShortcutsIconRes())
                viewBinding.textContent.text = content.resolveMessage(app)
                viewBinding.textButton.text = content.resolveButton(app)
                viewBinding.textButton.startButtonAnimation()
                viewBinding.root.setOnClickListener {
                    dismissOverlayView()
                    openRoute(content.actionType, scene, surface)
                    EventCenter.logEvent("notification_win_click", mapOf("list" to scene.sceneName))
                }
                viewBinding.actionRemove.setOnClickListener {
                    if (isFirstMistouch) {
                        isFirstMistouch = false
                        dismissOverlayView()
                        openRoute(content.actionType, scene, surface)
                        EventCenter.logEvent("notification_win_click", mapOf("list" to scene.sceneName))
                        return@setOnClickListener
                    }
                    if (isNeedForceMistouch) {
                        isNeedForceMistouch = false
                        dismissOverlayView()
                        openRoute(content.actionType, scene, surface)
                        EventCenter.logEvent("notification_win_click", mapOf("list" to scene.sceneName))
                        return@setOnClickListener
                    }
                    dismissOverlayView()
                }
            }
        }
    }

    private fun openRoute(actionType: DocumentActionType, scene: NotificationScene, surface: NoticeSurface) {
        val intent = Intent(app, RouteActivity::class.java).apply {
            putExtra(EXTRA_DOCUMENT_ACTION_TYPE, actionType)
            putExtra(EXTRA_NOTIFICATION_SCENE, scene as Parcelable)
            putExtra(EXTRA_NOTIFICATION_SURFACE, surface as Parcelable)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        app.startActivity(intent)
    }

    private fun dismissOverlayView() {
        val view = overlayView ?: return
        NoticeUtils.removeViewByReflection(app, view)
        overlayView = null
    }

}