package com.bontouch.logo

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

class ProgressLogo @JvmOverloads constructor(context: Context,
                                             attrs: AttributeSet? = null,
                                             defStyleAttrs: Int = 0)
    : View(context, attrs, defStyleAttrs) {

    companion object {
        private const val ANIMATION_DURATION = 1500L
        private const val SEGMENT_SCALE_FACTOR = 0.1f
        private const val ARC_SEGMENT_START_ANGLE = 120f
        private const val ARC_SEGMENT_SWEEP_ANGLE = -340f
    }

    private val paint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val path = Path()
    private val segment = Path()
    private val measure = PathMeasure()
    lateinit private var objectAnimator: ObjectAnimator

    override fun onDraw(canvas: Canvas?) {
        canvas?.drawPath(segment, paint)
    }

    /**
     * Calculates segment of path to draw during one evaluation step
     * of the object animator
     */
    @Suppress("UNUSED")
    private fun setSegment(value: Float) {
        val start = if (value - width * SEGMENT_SCALE_FACTOR < 0) 0f else value - width * SEGMENT_SCALE_FACTOR
        val end = if (value + width * SEGMENT_SCALE_FACTOR > measure.length) measure.length else value + width * SEGMENT_SCALE_FACTOR
        segment.rewind()
        measure.getSegment(start, end, segment, true)
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        // set stroke width as 5 % of the average of width and height
        paint.strokeWidth = ((w + h) / 2f) / 20f

        path.reset()
        // calculate first arc of path including padding
        path.arcTo(0f + paddingLeft,
                0f + paddingTop,
                w * 1f - paddingRight,
                h * 1f - paddingBottom,
                ARC_SEGMENT_START_ANGLE,
                ARC_SEGMENT_SWEEP_ANGLE,
                true)

        // add steps to path with alternating stepping of 10 and 20 %
        // of width and height
        path.rLineTo(0f, -h * 0.1f)
        path.rLineTo(w * 0.2f, 0f)
        path.rLineTo(0f, -h * 0.1f)
        path.rLineTo(w * 0.2f, 0f)
        path.rLineTo(0f, -h * 0.1f)
        path.rLineTo(w * 0.2f, 0f)

        measure.setPath(path, false)

        objectAnimator = ObjectAnimator.ofFloat(this, "segment", 0f, measure.length)
        with(objectAnimator) {
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            duration = ANIMATION_DURATION
            start()
        }
    }
}
