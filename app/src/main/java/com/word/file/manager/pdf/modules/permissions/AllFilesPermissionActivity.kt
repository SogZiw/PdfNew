package com.word.file.manager.pdf.modules.permissions

import androidx.lifecycle.lifecycleScope
import com.word.file.manager.pdf.base.BaseActivity
import com.word.file.manager.pdf.base.utils.buildPeriodicSignalFlow
import com.word.file.manager.pdf.databinding.ActivityAllFilesPermissionBinding
import com.word.file.manager.pdf.hasGoSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AllFilesPermissionActivity : BaseActivity<ActivityAllFilesPermissionBinding>() {

    private var permissionWatchJob: Job? = null
    private var hasOpenedSettings: Boolean = false

    override fun setViewBinding() = ActivityAllFilesPermissionBinding.inflate(layoutInflater)

    override fun initView() = Unit

    override fun onResume() {
        super.onResume()
        if (hasOpenedSettings) {
            finish()
        } else {
            openSettingsAndWatchPermission()
        }
    }

    private fun startPermissionWatcher() {
        permissionWatchJob?.cancel()
        permissionWatchJob = lifecycleScope.launch(Dispatchers.Main) {
            buildPeriodicSignalFlow(intervalMs = 200L, firstDelayMs = 200L).collect {
                if (hasStorageAccessPermission()) {
                    permissionWatchJob?.cancel()
                    startActivity(createAllFilesPermissionPageIntent(activity))
                }
            }
        }
    }

    private fun openSettingsAndWatchPermission() {
        hasOpenedSettings = true
        lifecycleScope.launch(Dispatchers.Main) {
            hasGoSettings = true
            openManageAllFilesAccessSettings(onFallbackFailure = ::finish)
        }
        startPermissionWatcher()
    }

    override fun onDestroy() {
        hasGoSettings = false
        super.onDestroy()
    }
}
