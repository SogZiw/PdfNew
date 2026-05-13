package com.word.file.manager.pdf.base.helper

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RatingBar
import androidx.core.net.toUri
import com.google.android.play.core.review.ReviewManagerFactory
import com.word.file.manager.pdf.base.utils.dpToPx
import com.word.file.manager.pdf.databinding.DialogReviewBinding
import com.word.file.manager.pdf.isDebug

object AppReviewHelper {

    fun showIfCan(activity: Activity) {
        if (activity.isFinishing || activity.isDestroyed) return
        if (LocalPrefs.hasReviewedDevice) return
        if (UserBlockHelper.canShowExtra(false).not()) return
        if (DateUtils.isToday(LocalPrefs.reviewDialogShowTime)) return
        val parent = activity.window.decorView as? ViewGroup ?: return
        val dialogBinding = DialogReviewBinding.inflate(LayoutInflater.from(activity), parent, false)
        val dialog = AlertDialog.Builder(activity).setView(dialogBinding.root).create()
        dialogBinding.ratingView.onRatingBarChangeListener = RatingBar.OnRatingBarChangeListener { _, rating, _ ->
            dialogBinding.btnSure.isEnabled = rating > 0f
        }
        dialogBinding.btnSure.setOnClickListener {
            dialog.dismiss()
            LocalPrefs.hasReviewedDevice = true
            if (dialogBinding.ratingView.rating >= REVIEW_STORE_THRESHOLD) {
                launchInAppReview(activity)
            } else {
                openFeedbackEmail(activity)
            }
        }
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(activity.resources.displayMetrics.widthPixels - 30.dpToPx(activity), WindowManager.LayoutParams.WRAP_CONTENT)
        LocalPrefs.reviewDialogShowTime = System.currentTimeMillis()
    }

    private fun launchInAppReview(activity: Activity) {
        if (isDebug) return
        val manager = ReviewManagerFactory.create(activity)
        manager.requestReviewFlow().addOnSuccessListener { reviewInfo ->
            manager.launchReviewFlow(activity, reviewInfo)
        }
    }

    private fun openFeedbackEmail(activity: Activity) {
        runCatching {
            activity.startActivity(Intent(Intent.ACTION_SENDTO).apply {
                data = "mailto:${FEEDBACK_EMAIL}".toUri()
            })
        }
    }

    private const val REVIEW_STORE_THRESHOLD = 4.5f
    private const val FEEDBACK_EMAIL = "clickawayapps@gmail.com"
}
