package com.word.file.manager.pdf.base.permission

import androidx.activity.result.contract.ActivityResultContracts
import androidx.viewbinding.ViewBinding
import com.word.file.manager.pdf.R
import com.word.file.manager.pdf.base.BaseActivity
import com.word.file.manager.pdf.base.data.DocumentActionType
import com.word.file.manager.pdf.base.helper.LocalPrefs
import com.word.file.manager.pdf.base.utils.showMessageToast
import com.word.file.manager.pdf.hasGoSettings

abstract class StoragePermissionActivity<V : ViewBinding> : BaseActivity<V>() {

    private var pendingActionType: DocumentActionType? = null

    private val permissionRequestLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (hasStorageAccessPermission()) {
            onStorageAccessGranted(pendingActionType)
        } else {
            onStorageAccessDenied()
        }
    }

    private val settingsResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        hasGoSettings = false
        if (hasStorageAccessPermission()) {
            onStorageAccessGranted(pendingActionType)
        } else {
            onStorageAccessDenied()
        }
    }

    fun checkStoragePermission(type: DocumentActionType) {
        pendingActionType = type
        if (handleGrantedState(type)) return
        if (shouldOpenAllFilesAccessPage()) {
            openAllFilesPermissionPage()
        } else {
            handleLegacyStoragePermission()
        }
    }

    abstract fun onStorageAccessGranted(type: DocumentActionType?)

    open fun onStorageAccessDenied() = Unit

    private fun handleGrantedState(type: DocumentActionType): Boolean {
        if (!hasStorageAccessPermission()) return false
        onStorageAccessGranted(type)
        return true
    }

    private fun handleLegacyStoragePermission() {
        if (shouldRequestLegacyStoragePermission(activity, LocalPrefs.isFirstReqStorage)) {
            requestLegacyStoragePermission()
        } else {
            openAppDetailsSettings()
        }
    }

    private fun requestLegacyStoragePermission() {
        LocalPrefs.isFirstReqStorage = false
        permissionRequestLauncher.launch(legacyStoragePermissions)
    }

    private fun openAllFilesPermissionPage() {
        settingsResultLauncher.launch(createAllFilesPermissionPageIntent(activity))
    }

    private fun openAppDetailsSettings() {
        runCatching {
            hasGoSettings = true
            settingsResultLauncher.launch(createAppDetailsSettingsIntent(activity))
        }.onFailure {
            showMessageToast(getString(R.string.common_error_message))
        }
    }
}
