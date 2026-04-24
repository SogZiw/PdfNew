package com.word.file.manager.pdf.base

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.word.file.manager.pdf.ext.applySystemBarInsetsPadding

abstract class ImmersiveActivity<VB : ViewBinding> : AppCompatActivity() {

    protected lateinit var binding: VB
        private set

    protected open val immersiveStatusBar: Boolean = true

    protected open val applyRootInsets: Boolean = true

    protected open val rootInsetsTarget get() = binding.root

    protected open val isLightMode: Boolean = true

    protected abstract fun setViewBinding(): VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = setViewBinding()
        setContentView(binding.root)
        initEdgeToEdge()
        initWindowInsets()
        dispatchFirstRender()
    }

    protected open fun dispatchFirstRender() {
        onBindingReady()
    }

    protected open fun onBindingReady() = Unit

    protected open fun initEdgeToEdge() {
        if (!immersiveStatusBar) return
        enableEdgeToEdge(
            statusBarStyle = if (isLightMode) {
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            } else {
                SystemBarStyle.dark(Color.TRANSPARENT)
            },
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
    }

    protected open fun initWindowInsets() {
        if (!immersiveStatusBar || !applyRootInsets) return
        rootInsetsTarget.applySystemBarInsetsPadding()
    }
}
