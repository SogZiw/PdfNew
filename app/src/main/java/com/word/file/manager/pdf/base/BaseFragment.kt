package com.word.file.manager.pdf.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.word.file.manager.pdf.ext.applySystemBarInsetsPadding

abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    private var _binding: VB? = null

    protected val binding: VB
        get() = checkNotNull(_binding) { "Binding is only valid between onCreateView and onDestroyView." }

    private var hostActivity: BaseActivity<*>? = null

    protected val activity: BaseActivity<*>
        get() = checkNotNull(hostActivity) { "Fragment is not attached to BaseActivity." }

    protected open val applyRootInsets: Boolean = false

    protected open val rootInsetsTarget: View
        get() = binding.root

    protected abstract fun setViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    override fun onAttach(context: Context) {
        super.onAttach(context)
        hostActivity = context as? BaseActivity<*>
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = setViewBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWindowInsets()
        dispatchFirstRender(savedInstanceState)
    }

    protected open fun dispatchFirstRender(savedInstanceState: Bundle?) {
        onBindingReady(savedInstanceState)
    }

    protected open fun onBindingReady(savedInstanceState: Bundle?) {
        initView(savedInstanceState)
    }

    protected abstract fun initView(savedInstanceState: Bundle?)

    protected open fun initWindowInsets() {
        if (!applyRootInsets) return
        rootInsetsTarget.applySystemBarInsetsPadding()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onDetach() {
        hostActivity = null
        super.onDetach()
    }
}
