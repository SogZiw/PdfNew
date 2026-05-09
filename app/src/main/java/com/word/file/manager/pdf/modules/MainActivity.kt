package com.word.file.manager.pdf.modules

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.word.file.manager.pdf.AD_POS_ID
import com.word.file.manager.pdf.APP_AD_CHANCE
import com.word.file.manager.pdf.APP_AD_IMPRESSION
import com.word.file.manager.pdf.EXTRA_DOCUMENT_ACTION_TYPE
import com.word.file.manager.pdf.EXTRA_FILE_ITEM
import com.word.file.manager.pdf.EXTRA_RESULT_TEXT
import com.word.file.manager.pdf.R
import com.word.file.manager.pdf.app
import com.word.file.manager.pdf.base.data.DocumentActionType
import com.word.file.manager.pdf.base.data.DocumentOpenType
import com.word.file.manager.pdf.base.data.PdfCreateType
import com.word.file.manager.pdf.base.data.PdfLockType
import com.word.file.manager.pdf.base.data.PdfMergeType
import com.word.file.manager.pdf.base.data.PdfSplitType
import com.word.file.manager.pdf.base.data.PdfUnlockType
import com.word.file.manager.pdf.base.helper.EventCenter
import com.word.file.manager.pdf.base.helper.UserBlockHelper
import com.word.file.manager.pdf.base.helper.ad.center.AdCenter
import com.word.file.manager.pdf.base.helper.ad.model.AdSlot
import com.word.file.manager.pdf.base.helper.ad.model.LoadState
import com.word.file.manager.pdf.base.helper.ad.util.AdRevenueUtils
import com.word.file.manager.pdf.base.utils.copyScannerPdfToLibrary
import com.word.file.manager.pdf.base.utils.showMessageToast
import com.word.file.manager.pdf.databinding.ActivityMainBinding
import com.word.file.manager.pdf.hasGoSettings
import com.word.file.manager.pdf.modules.fragments.BookmarkFragment
import com.word.file.manager.pdf.modules.fragments.HomeFragment
import com.word.file.manager.pdf.modules.fragments.RecentFragment
import com.word.file.manager.pdf.modules.fragments.ToolsFragment
import com.word.file.manager.pdf.modules.permissions.StoragePermissionActivity
import com.word.file.manager.pdf.modules.permissions.hasStorageAccessPermission
import com.word.file.manager.pdf.modules.tools.PdfMergeActivity
import com.word.file.manager.pdf.modules.tools.PdfSecurityActivity
import com.word.file.manager.pdf.modules.tools.PdfSplitActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : StoragePermissionActivity<ActivityMainBinding>() {

    private val createPdfLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        hasGoSettings = false
        val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
        val pdfUri = scanResult?.pdf?.uri ?: return@registerForActivityResult
        persistCreatedPdf(pdfUri)
    }
    private var curLoadState = LoadState.Idle
    private var curAdView: AdView? = null

    override fun setViewBinding(): ActivityMainBinding = ActivityMainBinding.inflate(LayoutInflater.from(this))

    override fun initView() {
        initViewPager()
        lifecycleScope.launch {
            app.documentRepository.requestStorageAccess.collect {
                checkStoragePermission(DocumentOpenType)
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
        handleLaunchAction(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleLaunchAction(intent)
    }

    private fun handleLaunchAction(sourceIntent: Intent?) {
        val actionType = sourceIntent.readDocumentActionType() ?: return
        sourceIntent?.removeExtra(EXTRA_DOCUMENT_ACTION_TYPE)
        when (actionType) {
            DocumentOpenType -> changeTab(0)
            else -> checkStoragePermission(actionType)
        }
    }

    private fun Intent?.readDocumentActionType(): DocumentActionType? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this?.getParcelableExtra(EXTRA_DOCUMENT_ACTION_TYPE, DocumentActionType::class.java)
        } else {
            @Suppress("DEPRECATION")
            this?.getParcelableExtra(EXTRA_DOCUMENT_ACTION_TYPE)
        }
    }

    override fun onStorageAccessGranted(type: DocumentActionType?) {
        app.documentRepository.refreshFiles(activity)
        when (type) {
            PdfCreateType -> launchCreatePdfScanner()
            PdfMergeType -> startActivity(Intent(activity, PdfMergeActivity::class.java))
            PdfSplitType -> startActivity(Intent(activity, PdfSplitActivity::class.java))
            PdfLockType,
            PdfUnlockType -> startActivity(Intent(activity, PdfSecurityActivity::class.java).apply {
                putExtra(EXTRA_DOCUMENT_ACTION_TYPE, type)
            })

            else -> Unit
        }
    }

    fun openDocumentTool(type: DocumentActionType) {
        checkStoragePermission(type)
    }

    override fun onStorageAccessDenied() {
        app.documentRepository.updatePermissionGuide(true)
    }

    private fun initViewPager() {
        val fragments = listOf(
            HomeFragment(),
            RecentFragment(),
            BookmarkFragment(),
            ToolsFragment(),
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

    override fun onUserBack() {
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
                EventCenter.logEvent(APP_AD_CHANCE, mapOf(AD_POS_ID to "ad_scan_int"))
                AdCenter.scanInterstitial.showFullScreen(activity, eventName = "ad_scan_int", allowed = {
                    UserBlockHelper.canShowExtra()
                }, closed = {
                    startActivity(Intent(activity, PdfCreateResultActivity::class.java).apply {
                        putExtra(EXTRA_FILE_ITEM, createdItem)
                        putExtra(EXTRA_RESULT_TEXT, getString(R.string.pdf_created))
                    })
                })
            }
        }
    }

    override fun onResume() {
        super.onResume()
        AdCenter.scanInterstitial.preload()
        lifecycleScope.launch {
            delay(280L)
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                loadBanner()
            }
        }
    }

    private fun loadBanner() {
        val config = AdCenter.mainBanner.getAdUnitConf() ?: return
        EventCenter.logEvent(APP_AD_CHANCE, mapOf(AD_POS_ID to "ad_main_ban"))
        if (UserBlockHelper.canShowExtra().not()) return
        if (curLoadState == LoadState.Loading) return
        curLoadState = LoadState.Loading
        val adView = AdView(this)
        adView.adUnitId = config.placementId
        adView.setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, 360))
        adView.adListener = object : AdListener() {
            override fun onAdClicked() = Unit
            override fun onAdClosed() = Unit
            override fun onAdFailedToLoad(adError: LoadAdError) {
                curLoadState = LoadState.Idle
            }

            override fun onAdImpression() {
                EventCenter.logEvent(APP_AD_IMPRESSION, mapOf(AD_POS_ID to "ad_main_ban"))
            }

            override fun onAdLoaded() {
                curLoadState = LoadState.Idle
            }

            override fun onAdOpened() = Unit
        }
        adView.onPaidEventListener = OnPaidEventListener {
            AdRevenueUtils.logPaidValue(AdSlot.MainBanner, config, it, adView.responseInfo?.loadedAdapterResponseInfo)
        }
        curAdView?.destroy()
        curAdView = adView
        binding.exBanContainer.removeAllViews()
        binding.exBanContainer.addView(adView)
        val extras = Bundle().apply { putString("collapsible", "bottom") }
        adView.loadAd(
            AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
                .build()
        )
    }

}
