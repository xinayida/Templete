package com.futuretech.common.helper

import android.animation.Animator
import android.animation.AnimatorInflater
import android.content.Context
import android.content.res.Resources
import android.view.animation.Animation
import android.view.animation.AnimationUtils

/**
 * FragmentAnimationHelper
 *
 * @author why
 * @since 2022/8/22
 */
object FragmentAnimationHelper {

    fun createAnimation(
        context: Context,
        nextAnim: Int = 0,
        onStart: () -> Unit = {},
        onEnd: () -> Unit = {}
    ): AnimationOrAnimator? {
        runCatching {
            var animation: Animation?
            val animator: Animator?
            if (nextAnim != 0) {
                val dir: String = context.resources.getResourceTypeName(nextAnim)
                val isAnim = "anim" == dir
                var successfulLoad = false
                if (isAnim) {
                    // try AnimationUtils first
                    try {
                        animation = AnimationUtils.loadAnimation(context, nextAnim)
                        if (animation != null) {
                            animation.setAnimationListener(object : Animation.AnimationListener {
                                override fun onAnimationStart(animation: Animation?) {
                                    onStart.invoke()
                                }

                                override fun onAnimationEnd(animation: Animation?) {
                                    onEnd.invoke()
                                }

                                override fun onAnimationRepeat(animation: Animation?) {
                                }
                            })
                            return AnimationOrAnimator(animation)
                        }
                        // A null animation may be returned and that is acceptable
                        successfulLoad = true // succeeded in loading animation, but it is null
                    } catch (e: Resources.NotFoundException) {
                        throw e // Rethrow it -- the resource should be found if it is provided.
                    } catch (e: RuntimeException) {
                        // Other exceptions can occur when loading an Animator from AnimationUtils.
                    }
                }
                if (!successfulLoad) {
                    // try Animator
                    try {
                        animator = AnimatorInflater.loadAnimator(context, nextAnim)
                        if (animator != null) {
                            animator.addListener(object : Animator.AnimatorListener {
                                override fun onAnimationStart(animation: Animator?) {
                                    onStart.invoke()
                                }

                                override fun onAnimationEnd(animation: Animator?) {
                                    onEnd.invoke()
                                }

                                override fun onAnimationCancel(animation: Animator?) {
                                }

                                override fun onAnimationRepeat(animation: Animator?) {
                                }

                            })
                            return AnimationOrAnimator(animator)
                        }
                    } catch (e: RuntimeException) {
                        if (isAnim) {
                            // Rethrow it -- we already tried AnimationUtils and it failed.
                            throw e
                        }
                        // Otherwise, it is probably an animation resource
                        animation = AnimationUtils.loadAnimation(context, nextAnim)
                        if (animation != null) {
                            animation.setAnimationListener(object : Animation.AnimationListener {
                                override fun onAnimationStart(animation: Animation?) {
                                    onStart.invoke()
                                }

                                override fun onAnimationEnd(animation: Animation?) {
                                    onEnd.invoke()
                                }

                                override fun onAnimationRepeat(animation: Animation?) {
                                }
                            })
                            return AnimationOrAnimator(animation)
                        }
                    }
                }
            }
        }
        return null
    }

    class AnimationOrAnimator {
        val animation: Animation?
        val animator: Animator?

        constructor(animation: Animation?) {
            this.animation = animation
            animator = null
        }

        constructor(animator: Animator?) {
            animation = null
            this.animator = animator
        }
    }
}