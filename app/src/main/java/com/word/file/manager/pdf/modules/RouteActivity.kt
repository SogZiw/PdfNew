package com.word.file.manager.pdf.modules

import android.content.Intent
import androidx.lifecycle.lifecycleScope
import com.word.file.manager.pdf.base.BaseActivity
import com.word.file.manager.pdf.databinding.ActivityRouteBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RouteActivity : BaseActivity<ActivityRouteBinding>() {

    override fun setViewBinding() = ActivityRouteBinding.inflate(layoutInflater)

    override fun initView() {
        lifecycleScope.launch {
            delay(3_000L)
            startActivity(Intent(activity, MainActivity::class.java))
            finish()
        }
    }

}
