package com.word.file.manager.pdf.modules.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.word.file.manager.pdf.base.BaseFragment
import com.word.file.manager.pdf.databinding.FragmentHomeBinding

class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    override fun setViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun initView(savedInstanceState: Bundle?) {

    }


}