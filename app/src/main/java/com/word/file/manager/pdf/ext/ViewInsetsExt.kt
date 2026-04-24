package com.word.file.manager.pdf.ext

import android.view.View
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

private data class InitialPadding(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
)

private inline fun View.doOnApplyWindowInsets(
    crossinline block: (View, WindowInsetsCompat, Insets) -> Unit,
) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        block(view, insets, systemBars)
        insets
    }
    requestApplyInsetsWhenAttached()
}

fun View.applySystemBarInsetsPadding(
    applyLeft: Boolean = true,
    applyTop: Boolean = true,
    applyRight: Boolean = true,
    applyBottom: Boolean = true,
) {
    val initial = InitialPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
    doOnApplyWindowInsets { view, _, systemBars ->
        view.updatePadding(
            left = initial.left + if (applyLeft) systemBars.left else 0,
            top = initial.top + if (applyTop) systemBars.top else 0,
            right = initial.right + if (applyRight) systemBars.right else 0,
            bottom = initial.bottom + if (applyBottom) systemBars.bottom else 0,
        )
    }
}

fun View.requestApplyInsetsWhenAttached() {
    if (isAttachedToWindow) {
        requestApplyInsets()
    } else {
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                v.removeOnAttachStateChangeListener(this)
                v.requestApplyInsets()
            }

            override fun onViewDetachedFromWindow(v: View) = Unit
        })
    }
}
