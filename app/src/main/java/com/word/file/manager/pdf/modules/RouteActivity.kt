package com.word.file.manager.pdf.modules

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.word.file.manager.pdf.EXTRA_DOCUMENT_ACTION_TYPE
import com.word.file.manager.pdf.EXTRA_FROM_SET
import com.word.file.manager.pdf.EXTRA_SHORTCUT_PAGE
import com.word.file.manager.pdf.SHORTCUT_PAGE_SCAN
import com.word.file.manager.pdf.SHORTCUT_PAGE_UNINSTALL
import com.word.file.manager.pdf.SHORTCUT_PAGE_VIEW
import com.word.file.manager.pdf.base.BaseActivity
import com.word.file.manager.pdf.base.data.DocumentActionType
import com.word.file.manager.pdf.base.data.DocumentOpenType
import com.word.file.manager.pdf.base.data.PdfCreateType
import com.word.file.manager.pdf.base.helper.LocalPrefs
import com.word.file.manager.pdf.base.helper.ad.center.AdCenter
import com.word.file.manager.pdf.databinding.ActivityRouteBinding
import com.word.file.manager.pdf.modules.guide.GuideFirstActivity

class RouteActivity : BaseActivity<ActivityRouteBinding>() {

    private val shortcutPage by lazy { intent?.getStringExtra(EXTRA_SHORTCUT_PAGE) }
    private val viewModel by viewModels<RouteViewModel>()

    private val notificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        viewModel.startLoadingLaunch(activity)
    }

    override fun setViewBinding() = ActivityRouteBinding.inflate(layoutInflater)

    override fun initView() {
        AdCenter.lastFullAdShowTime = 0L
        observeLaunchState()
        viewModel.beginLaunch(activity)
    }

    private fun observeLaunchState() {
        viewModel.afterUMPLiveData.observe(this) {
            requestNoticeIfNeeded()
        }
        viewModel.nextJobLiveData.observe(this) {
            goNextPage()
        }
    }

    private fun requestNoticeIfNeeded() {
        if (!shouldRequestNoticePermission()) {
            viewModel.startLoadingLaunch(activity)
            return
        }
        if (!LocalPrefs.hasAskedNotificationPermission) {
            LocalPrefs.hasAskedNotificationPermission = true
            viewModel.startLoadAd(logEvent = false)
            requestPostNotification(tag = "start")
            return
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)) {
            viewModel.startLoadAd(logEvent = false)
            requestPostNotification(tag = "second")
            return
        }
        viewModel.startLoadingLaunch(activity)
    }

    private fun requestPostNotification(tag: String) {
        viewModel.logNoticeRequest(tag)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else viewModel.startLoadingLaunch(activity)
    }

    private fun shouldRequestNoticePermission(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    }

    private fun goNextPage() {
        if (shortcutPage == SHORTCUT_PAGE_UNINSTALL) {
            startActivity(Intent(activity, GuideFirstActivity::class.java))
            finish()
            return
        }
        val actionType = shortcutPage.toDocumentActionType()
        if (!LocalPrefs.hasSeenIntroduce) {
            startActivity(Intent(activity, LanguageActivity::class.java).apply {
                putExtra(EXTRA_FROM_SET, false)
                putExtra(EXTRA_DOCUMENT_ACTION_TYPE, actionType)
            })
            finish()
            return
        }
        startActivity(Intent(activity, MainActivity::class.java).apply {
            putExtra(EXTRA_DOCUMENT_ACTION_TYPE, actionType)
        })
        finish()
    }

    private fun String?.toDocumentActionType(): DocumentActionType {
        return when (this) {
            SHORTCUT_PAGE_SCAN -> PdfCreateType
            SHORTCUT_PAGE_VIEW -> DocumentOpenType
            else -> DocumentOpenType
        }
    }

    override fun onUserBack() = Unit
}
