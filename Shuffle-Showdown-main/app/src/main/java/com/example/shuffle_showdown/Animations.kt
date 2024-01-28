package com.example.shuffle_showdown

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class Animations(activityI: AppCompatActivity) {
    private var activity: AppCompatActivity = activityI
    fun flipCard(container: ViewGroup) {
        // Setup
        val scale: Float = activity.resources.displayMetrics.density
        container.cameraDistance = 20000 * scale
        container.pivotX = container.width * 0.5f
        container.pivotY = 0f

        val animationSpeed: Long = 350
        val linearInterpolator = LinearInterpolator()

        var animations = listOf(
            ObjectAnimator.ofFloat(container, "rotationX", 0f, 90f),
            ObjectAnimator.ofFloat(container, "rotationX", 90f, 180f),
            ObjectAnimator.ofFloat(container, "rotationX", 180f, 270f),
            ObjectAnimator.ofFloat(container, "rotationX", 270f, 360f)
        )

        for (i in animations.indices) {
            animations[i].duration = if (i < animations.size - 1) animationSpeed else 0
            animations[i].interpolator = linearInterpolator
        }


        animations[0].addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                for (i in 1 until container.childCount) {
                     container.getChildAt(i).visibility = View.INVISIBLE
                }
            }
        })

        animations.last().addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                for (i in 1 until container.childCount) {
                    container.getChildAt(i).visibility = View.VISIBLE
                }
            }
        })

        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(animations)

        animatorSet.start()
    }

    fun fade(container: ViewGroup) {
        val fadeOut = ObjectAnimator.ofFloat(container, "alpha", 1f, 0.5f)
        fadeOut.duration = 400

        val fadeIn = ObjectAnimator.ofFloat(container, "alpha", 0.5f, 1f)
        fadeIn.duration = 400

        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(fadeOut, fadeIn)
        animatorSet.start()
    }
}