package com.word.file.manager.pdf.ext

import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableContainer
import android.graphics.drawable.LayerDrawable
import android.view.View

fun View.startBackgroundAnimation() {
    post { background.restartAnimation() }
}

private fun Drawable?.restartAnimation() {
    when (this) {
        is Animatable -> {
            stop()
            start()
        }
        is LayerDrawable -> {
            for (index in 0 until numberOfLayers) {
                getDrawable(index).restartAnimation()
            }
        }
        is DrawableContainer -> current.restartAnimation()
    }
}
