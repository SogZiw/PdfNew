package com.word.file.manager.pdf.modules

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.text.format.DateUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.word.file.manager.pdf.EXTRA_DOCUMENT_ACTION_TYPE
import com.word.file.manager.pdf.EXTRA_FROM_SET
import com.word.file.manager.pdf.EXTRA_NOTIFICATION_SCENE
import com.word.file.manager.pdf.EXTRA_NOTIFICATION_SURFACE
import com.word.file.manager.pdf.EXTRA_SHORTCUT_PAGE
import com.word.file.manager.pdf.SHORTCUT_PAGE_SCAN
import com.word.file.manager.pdf.SHORTCUT_PAGE_UNINSTALL
import com.word.file.manager.pdf.SHORTCUT_PAGE_VIEW
import com.word.file.manager.pdf.base.BaseActivity
import com.word.file.manager.pdf.base.data.DocumentActionType
import com.word.file.manager.pdf.base.data.DocumentOpenType
import com.word.file.manager.pdf.base.data.PdfCreateType
import com.word.file.manager.pdf.base.helper.EventCenter
import com.word.file.manager.pdf.base.helper.LocalPrefs
import com.word.file.manager.pdf.base.helper.UserBlockHelper
import com.word.file.manager.pdf.base.helper.ad.center.AdCenter
import com.word.file.manager.pdf.base.helper.notice.NoticeSurface
import com.word.file.manager.pdf.base.helper.notice.NotificationScene
import com.word.file.manager.pdf.base.helper.remote.RemoteLogicConfig
import com.word.file.manager.pdf.base.helper.services.CoreService
import com.word.file.manager.pdf.base.utils.isAtLeastAndroid13
import com.word.file.manager.pdf.databinding.ActivityRouteBinding
import com.word.file.manager.pdf.modules.guide.GuideFirstActivity
import com.word.file.manager.pdf.modules.permissions.ExtraGuideActivity
import com.word.file.manager.pdf.modules.permissions.hasOverlayPermission
import com.word.file.manager.pdf.modules.permissions.hasPostNotificationPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RouteActivity : BaseActivity<ActivityRouteBinding>() {

    private val documentActionType by lazy { intent?.getParcelableExtra<DocumentActionType>(EXTRA_DOCUMENT_ACTION_TYPE) }
    private val noticeSurface by lazy { intent?.getParcelableExtra<NoticeSurface>(EXTRA_NOTIFICATION_SURFACE) }
    private val notificationScene by lazy { intent?.getParcelableExtra<NotificationScene>(EXTRA_NOTIFICATION_SCENE) }
    private val shortcutPage by lazy { intent?.getStringExtra(EXTRA_SHORTCUT_PAGE) }
    private val viewModel by viewModels<RouteViewModel>()
    private var isFirstNF = false
    private val notificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (hasPostNotificationPermission()) EventCenter.logEvent("notify_permission_success", mapOf("list" to if (isFirstNF) "first" else "second"))
        viewModel.startLoadingLaunch(activity, source)
    }
    private val isFirstLoading by lazy { LocalPrefs.isFirstLaunch }
    private var source: String = "open_launch"

    override fun setViewBinding() = ActivityRouteBinding.inflate(layoutInflater)

    override fun initView() {
        AdCenter.lastFullAdShowTime = 0L
        observeLaunchState()
        viewModel.beginLaunch(activity)
        when (noticeSurface) {
            NoticeSurface.NORMAL -> {
                source = "normal_launch"
                EventCenter.logEvent("notification_popup_click", mapOf("list" to notificationScene?.sceneName))
            }

            NoticeSurface.MEDIA -> {
                source = "media_launch"
                EventCenter.logEvent("notification_media_click", mapOf("list" to notificationScene?.sceneName))
            }

            NoticeSurface.WINDOW -> {
                source = "win_launch"
                EventCenter.logEvent("notification_win_click", mapOf("list" to notificationScene?.sceneName))
            }

            else -> {
                if (NotificationScene.TOOLBAR == notificationScene) {
                    source = "notification_launch"
                    EventCenter.logEvent("notification_serve_click")
                }
            }
        }
        if (shortcutPage == SHORTCUT_PAGE_UNINSTALL) {
            source = "shortcut_launch"
            EventCenter.logEvent("shortcut_uninstall_click")
        }
        if (isFirstLoading) {
            LocalPrefs.isFirstLaunch = false
            EventCenter.logEvent("Loading_show_first")
            EventCenter.logEvent("Loading_show")
        } else {
            EventCenter.logEvent("Loading_show")
        }
    }

    private fun observeLaunchState() {
        viewModel.afterUMPLiveData.observe(this) {
            requestNoticeIfNeeded()
            lifecycleScope.launch(Dispatchers.Main) {
                delay(2000L)
                CoreService.showToolbar(activity)
            }
        }
        viewModel.nextJobLiveData.observe(this) {
            goNextPage()
        }
    }

    private fun requestNoticeIfNeeded() {
        if (!shouldRequestNoticePermission()) {
            viewModel.startLoadingLaunch(activity, source)
            return
        }
        if (!LocalPrefs.hasAskedNotificationPermission) {
            LocalPrefs.hasAskedNotificationPermission = true
            viewModel.startLoadAd(logEvent = false)
            isFirstNF = true
            requestPostNotification(tag = "start")
            return
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)) {
            viewModel.startLoadAd(logEvent = false)
            isFirstNF = false
            requestPostNotification(tag = "second")
            return
        }
        viewModel.startLoadingLaunch(activity, source)
    }

    private fun requestPostNotification(tag: String) {
        viewModel.logNoticeRequest(tag)
        if (isAtLeastAndroid13()) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else viewModel.startLoadingLaunch(activity, source)
    }

    private fun shouldRequestNoticePermission(): Boolean {
        return isAtLeastAndroid13() &&
                ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    }

    private fun goNextPage() {
        if (shortcutPage == SHORTCUT_PAGE_UNINSTALL) {
            startActivity(Intent(activity, GuideFirstActivity::class.java))
            finish()
            return
        }
        val actionType = readLaunchActionType()
        if (RemoteLogicConfig.fetchFeatureConfig().permissionPage
            && UserBlockHelper.canShowExtra(false)
            && hasOverlayPermission().not()
            && DateUtils.isToday(LocalPrefs.lastWinPageShow).not()
        ) {
            LocalPrefs.lastWinPageShow = System.currentTimeMillis()
            startActivity(Intent(activity, ExtraGuideActivity::class.java).apply {
                putLaunchActionType(actionType)
            })
            finish()
            return
        }
        if (!LocalPrefs.hasSeenIntroduce) {
            if (RemoteLogicConfig.fetchFeatureConfig().firstShow.pageLang) {
                startActivity(Intent(activity, LanguageActivity::class.java).apply {
                    putExtra(EXTRA_FROM_SET, false)
                    putLaunchActionType(actionType)
                })
                finish()
                return
            }
            if (RemoteLogicConfig.fetchFeatureConfig().firstShow.pageIntro) {
                startActivity(Intent(activity, IntroduceActivity::class.java).apply {
                    putLaunchActionType(actionType)
                })
                finish()
                return
            }
        }
        startActivity(Intent(activity, MainActivity::class.java).apply {
            putLaunchActionType(actionType)
        })
        finish()
    }

    private fun readLaunchActionType(): DocumentActionType? {
        return documentActionType ?: shortcutPage.toDocumentActionType()
    }

    private fun Intent.putLaunchActionType(actionType: DocumentActionType?) {
        if (actionType != null) putExtra(EXTRA_DOCUMENT_ACTION_TYPE, actionType)
    }

    private fun String?.toDocumentActionType(): DocumentActionType? {
        return when (this) {
            SHORTCUT_PAGE_SCAN -> PdfCreateType
            SHORTCUT_PAGE_VIEW -> DocumentOpenType
            else -> null
        }
    }

    override fun onUserBack() = Unit
}
