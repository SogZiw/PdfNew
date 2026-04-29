package com.word.file.manager.pdf.base

import android.content.Context
import androidx.activity.addCallback
import androidx.viewbinding.ViewBinding
import com.word.file.manager.pdf.base.utils.withSavedAppLocale

abstract class BaseActivity<VB : ViewBinding> : InsetsActivity<VB>() {

    private val selfActivity: BaseActivity<*> by lazy(LazyThreadSafetyMode.NONE) { this }

    protected val activity: BaseActivity<*> get() = selfActivity

    protected var isPageResumed: Boolean = true
        private set

    var printContext: Context? = null
        private set

    override fun attachBaseContext(newBase: Context?) {
        printContext = newBase
        super.attachBaseContext(newBase?.withSavedAppLocale())
    }

    override fun onBindingReady() {
        super.onBindingReady()
        registerBackHandler()
        initView()
    }

    protected abstract fun initView()

    protected open fun onClickBack() = closeCurrentPage()

    protected open fun closeCurrentPage() = finish()

    private fun registerBackHandler() {
        onBackPressedDispatcher.addCallback(owner = this) {
            onClickBack()
        }
    }

    private fun updatePageResumeState(resumed: Boolean) {
        isPageResumed = resumed
    }

    override fun onStart() {
        super.onStart()
        updatePageResumeState(false)
    }

    override fun onResume() {
        super.onResume()
        updatePageResumeState(true)
    }

    override fun onPause() {
        updatePageResumeState(false)
        super.onPause()
    }

    override fun onStop() {
        updatePageResumeState(false)
        super.onStop()
    }

    override fun onDestroy() {
        updatePageResumeState(false)
        super.onDestroy()
    }
}
