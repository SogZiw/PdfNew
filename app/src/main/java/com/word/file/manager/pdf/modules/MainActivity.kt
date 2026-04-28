package com.word.file.manager.pdf.modules

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.word.file.manager.pdf.app
import com.word.file.manager.pdf.base.data.DocumentActionType
import com.word.file.manager.pdf.base.data.PdfCreateType
import com.word.file.manager.pdf.databinding.ActivityMainBinding
import com.word.file.manager.pdf.base.utils.copyScannerPdfToLibrary
import com.word.file.manager.pdf.base.utils.showMessageToast
import com.word.file.manager.pdf.modules.fragments.BookmarkFragment
import com.word.file.manager.pdf.modules.fragments.HomeFragment
import com.word.file.manager.pdf.modules.fragments.RecentFragment
import com.word.file.manager.pdf.modules.permissions.StoragePermissionActivity
import com.word.file.manager.pdf.modules.permissions.hasStorageAccessPermission
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.word.file.manager.pdf.EXTRA_FILE_ITEM
import com.word.file.manager.pdf.EXTRA_RESULT_TEXT
import com.word.file.manager.pdf.R
import com.word.file.manager.pdf.hasGoSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : StoragePermissionActivity<ActivityMainBinding>() {

    private val createPdfLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        hasGoSettings = false
        val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
        val pdfUri = scanResult?.pdf?.uri ?: return@registerForActivityResult
        persistCreatedPdf(pdfUri)
    }

    override fun setViewBinding(): ActivityMainBinding = ActivityMainBinding.inflate(LayoutInflater.from(this))

    override fun initView() {
        initViewPager()
        lifecycleScope.launch {
            app.documentRepository.requestStorageAccess.collect {
                checkStoragePermission(com.word.file.manager.pdf.base.data.DocumentOpenType)
            }
        }
        if (hasStorageAccessPermission()) {
            app.documentRepository.refreshFiles(activity)
        } else {
            app.documentRepository.updatePermissionGuide(true)
        }
        binding.btnAdd.setOnClickListener {
            checkStoragePermission(PdfCreateType)
        }
    }

    override fun onStorageAccessGranted(type: DocumentActionType?) {
        if (type == PdfCreateType) {
            app.documentRepository.refreshFiles(activity)
            launchCreatePdfScanner()
        } else {
            app.documentRepository.refreshFiles(activity)
        }
    }

    override fun onStorageAccessDenied() {
        app.documentRepository.updatePermissionGuide(true)
    }

    private fun initViewPager() {
        val fragments = listOf(
            HomeFragment(),
            RecentFragment(),
            BookmarkFragment(),
            Fragment(),
        )
        binding.viewPager.isUserInputEnabled = false
        binding.viewPager.offscreenPageLimit = fragments.size
        binding.viewPager.adapter = object : FragmentStateAdapter(supportFragmentManager, lifecycle) {
            override fun getItemCount(): Int = fragments.size
            override fun createFragment(position: Int): Fragment = fragments[position]
        }
        binding.tabHome.setOnClickListener { changeTab(0) }
        binding.tabRecently.setOnClickListener { changeTab(1) }
        binding.tabHistory.setOnClickListener { changeTab(2) }
        binding.tabTools.setOnClickListener { changeTab(3) }
        changeTab(0)
    }

    private fun changeTab(index: Int) {
        binding.tabHome.isSelected = index == 0
        binding.tabRecently.isSelected = index == 1
        binding.tabHistory.isSelected = index == 2
        binding.tabTools.isSelected = index == 3
        binding.viewPager.setCurrentItem(index, false)
    }

    override fun onClickBack() {
        moveTaskToBack(true)
    }

    private fun launchCreatePdfScanner() {
        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_PDF)
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()
        GmsDocumentScanning.getClient(options)
            .getStartScanIntent(this)
            .addOnSuccessListener { intentSender ->
                hasGoSettings = true
                createPdfLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener {
                showMessageToast(getString(R.string.common_error_message))
            }
    }

    private fun persistCreatedPdf(sourceUri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            val createdFile = copyScannerPdfToLibrary(activity, sourceUri)
            if (createdFile == null) {
                withContext(Dispatchers.Main) {
                    showMessageToast(getString(R.string.common_error_message))
                }
                return@launch
            }
            val createdItem = app.documentRepository.registerCreatedPdf(createdFile)
            withContext(Dispatchers.Main) {
                startActivity(Intent(activity, PdfCreateResultActivity::class.java).apply {
                    putExtra(EXTRA_FILE_ITEM, createdItem)
                    putExtra(EXTRA_RESULT_TEXT, getString(R.string.pdf_created))
                })
            }
        }
    }

}