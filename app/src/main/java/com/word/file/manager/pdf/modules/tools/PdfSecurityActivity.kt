package com.word.file.manager.pdf.modules.tools

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.word.file.manager.pdf.EXTRA_DOCUMENT_ACTION_TYPE
import com.word.file.manager.pdf.EXTRA_FILE_ITEM
import com.word.file.manager.pdf.EXTRA_RESULT_TEXT
import com.word.file.manager.pdf.R
import com.word.file.manager.pdf.app
import com.word.file.manager.pdf.base.BaseActivity
import com.word.file.manager.pdf.base.data.DocumentActionType
import com.word.file.manager.pdf.base.data.FileItem
import com.word.file.manager.pdf.base.data.PdfLockType
import com.word.file.manager.pdf.base.data.PdfUnlockType
import com.word.file.manager.pdf.base.utils.isLockedPdfForTool
import com.word.file.manager.pdf.base.utils.isPdfPasswordValid
import com.word.file.manager.pdf.base.utils.isUsablePdfForTool
import com.word.file.manager.pdf.base.utils.lockPdfDocument
import com.word.file.manager.pdf.base.utils.showMessageToast
import com.word.file.manager.pdf.base.utils.unlockPdfDocument
import com.word.file.manager.pdf.databinding.ActivityPdfSecurityBinding
import com.word.file.manager.pdf.databinding.DialogPdfPasswordBinding
import com.word.file.manager.pdf.databinding.DialogPdfWorkingBinding
import com.word.file.manager.pdf.modules.PdfCreateResultActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PdfSecurityActivity : BaseActivity<ActivityPdfSecurityBinding>() {

    private lateinit var fileAdapter: PdfToolFileAdapter
    private val securityMode: SecurityMode by lazy { SecurityMode.from(readActionType()) }
    private var securityInProgress = false

    override fun setViewBinding(): ActivityPdfSecurityBinding {
        return ActivityPdfSecurityBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        binding.toolbar.actionBack.setOnClickListener { onClickBack() }
        binding.toolbar.toolbarTitle.text = getString(securityMode.titleRes)
        fileAdapter = PdfToolFileAdapter(
            pickMode = PdfToolFileAdapter.PickMode.Single,
            onFilePicked = { showPasswordDialog(it) },
        )
        binding.recyclerView.itemAnimator = null
        binding.recyclerView.adapter = fileAdapter
        observeTargetFiles()
    }

    private fun readActionType(): DocumentActionType {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(EXTRA_DOCUMENT_ACTION_TYPE, DocumentActionType::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra(EXTRA_DOCUMENT_ACTION_TYPE)
        } ?: PdfLockType
    }

    private fun observeTargetFiles() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                app.documentRepository.allFiles.collect { files ->
                    val targetFiles = files.filter { it.matchesSecurityMode() }
                    fileAdapter.submitList(targetFiles)
                    binding.emptyView.isVisible = targetFiles.isEmpty()
                }
            }
        }
    }

    private fun FileItem.matchesSecurityMode(): Boolean {
        return when (securityMode) {
            SecurityMode.Lock -> isUsablePdfForTool()
            SecurityMode.Unlock -> isLockedPdfForTool()
        }
    }

    private fun showPasswordDialog(fileItem: FileItem) {
        if (securityInProgress) return
        val dialogBinding = DialogPdfPasswordBinding.inflate(layoutInflater)
        val passwordDialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()
        dialogBinding.textPasswordTitle.text = getString(securityMode.titleRes)
        dialogBinding.btnCancel.setOnClickListener { passwordDialog.dismiss() }
        dialogBinding.btnConfirm.setOnClickListener {
            confirmPassword(fileItem, dialogBinding, passwordDialog)
        }
        passwordDialog.show()
    }

    private fun confirmPassword(
        fileItem: FileItem,
        dialogBinding: DialogPdfPasswordBinding,
        passwordDialog: androidx.appcompat.app.AlertDialog,
    ) {
        val password = dialogBinding.etPassword.text?.toString().orEmpty()
        if (password.isBlank()) {
            showMessageToast(getString(R.string.pdf_password_required))
            return
        }
        if (password.length > MAX_PASSWORD_LENGTH) {
            showMessageToast(getString(R.string.pdf_password_too_long))
            return
        }
        dialogBinding.btnConfirm.isEnabled = false
        lifecycleScope.launch {
            if (securityMode == SecurityMode.Unlock) {
                val passwordValid = withContext(Dispatchers.IO) {
                    isPdfPasswordValid(fileItem.absolutePath, password)
                }
                if (!passwordValid) {
                    dialogBinding.btnConfirm.isEnabled = true
                    dialogBinding.etPassword.setText("")
                    showMessageToast(getString(R.string.pdf_password_incorrect))
                    return@launch
                }
            }
            passwordDialog.dismiss()
            applySecurityChange(fileItem, password)
        }
    }

    private fun applySecurityChange(fileItem: FileItem, password: String) {
        if (securityInProgress) return
        lifecycleScope.launch {
            securityInProgress = true
            val dialog = showWorkingDialog(getString(securityMode.progressRes))
            val securityTask = async {
                when (securityMode) {
                    SecurityMode.Lock -> lockPdfDocument(fileItem, password)
                    SecurityMode.Unlock -> unlockPdfDocument(fileItem, password)
                }
            }
            delay(MIN_SECURITY_DURATION_MS)
            val success = securityTask.await()
            dialog.dismiss()
            if (!success) {
                securityInProgress = false
                showMessageToast(getString(R.string.common_error_message))
                return@launch
            }
            val resultItem = app.documentRepository.updatePdfSecurityState(
                fileItem = fileItem,
                encrypted = securityMode.outputEncrypted,
            )
            startActivity(Intent(this@PdfSecurityActivity, PdfCreateResultActivity::class.java).apply {
                putExtra(EXTRA_FILE_ITEM, resultItem)
                putExtra(EXTRA_RESULT_TEXT, getString(securityMode.resultTextRes))
            })
            finish()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showWorkingDialog(message: String) = MaterialAlertDialogBuilder(this)
        .setView(
            DialogPdfWorkingBinding.inflate(layoutInflater).apply {
                textProgress.text = "$message..."
            }.root,
        )
        .setCancelable(false)
        .create()
        .also { it.show() }

    private enum class SecurityMode(
        @param:StringRes val titleRes: Int,
        @param:StringRes val progressRes: Int,
        @param:StringRes val resultTextRes: Int,
        val outputEncrypted: Boolean,
    ) {
        Lock(
            titleRes = R.string.lock_pdf,
            progressRes = R.string.locking,
            resultTextRes = R.string.pdf_locked_successful,
            outputEncrypted = true,
        ),
        Unlock(
            titleRes = R.string.unlock_pdf,
            progressRes = R.string.unlocking,
            resultTextRes = R.string.pdf_unlocked_successful,
            outputEncrypted = false,
        );

        companion object {
            fun from(actionType: DocumentActionType): SecurityMode {
                return when (actionType) {
                    PdfUnlockType -> Unlock
                    else -> Lock
                }
            }
        }
    }

    private companion object {
        const val MAX_PASSWORD_LENGTH = 40
        const val MIN_SECURITY_DURATION_MS = 1_000L
    }
}
