package com.matisse.widget

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import com.matisse.R

/**
 * Created by liubo on 2018/9/4.
 */
class CheckView : View {

    companion object {
        const val UNCHECKED = Integer.MIN_VALUE
        private const val STROKE_WIDTH = 3.0f           // 圆环宽度
        private const val SHADOW_WIDTH = 6.0f           // 阴影宽度
        private const val SIZE = 30
        private const val STROKE_RADIUS = 11.5f
        private const val BG_RADIUS = 11.0f
        private const val CONTENT_SIZE = 16
    }

    private var countable = false
    private var checked = false
    private var checkedNum = 0
    private var strokePaint: Paint? = null
    private var backgroundPaint: Paint? = null
    private var textPaint: Paint? = null
    private var shadowPaint: Paint? = null
    private var checkDrawable: Drawable? = null
    private var density = 0f
    private var checkRect: Rect? = null
    private var enable = true
    private var halfDensitySize = 0f

    constructor(context: Context?) : this(context, null, 0)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        initParams()
    }

    private fun initParams() {
        density = context.resources?.displayMetrics?.density ?: 0f
        halfDensitySize = density * SIZE / 2f

        strokePaint = Paint().run {
            isAntiAlias = true
            style = Paint.Style.STROKE
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
            strokeWidth = STROKE_WIDTH * density
            this
        }

        val ta: TypedArray =
            context.theme.obtainStyledAttributes(intArrayOf(R.attr.item_checkCircle_borderColor))
        val defaultColor = ResourcesCompat.getColor(
            context.resources, R.color.item_checkCircle_borderColor, context.theme
        )
        val color = ta.getColor(0, defaultColor)
        ta.recycle()
        strokePaint?.color = color

        checkDrawable = ResourcesCompat.getDrawable(
            context.resources, R.drawable.ic_check_white_18dp, context.theme
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val sizeSpec = MeasureSpec.makeMeasureSpec((density * SIZE).toInt(), MeasureSpec.EXACTLY)
        super.onMeasure(sizeSpec, sizeSpec)
    }

    fun setEnable(enable: Boolean) {
        if (this.enable != enable) {
            this.enable = enable
            invalidate()
        }
    }

    fun setCountable(boolean: Boolean) {
        if (countable != boolean) {
            countable = boolean
            invalidate()
        }
    }

    fun setChecked(boolean: Boolean) {
        if (countable) {
            throw IllegalStateException("CheckView is countable, call setCheckedNum() instead.")
        }

        checked = boolean
        invalidate()
    }

    fun setCheckedNum(num: Int) {
        if (!countable) {
            throw IllegalStateException("CheckView is not countable, call setChecked() instead.")
        }
        if (num != UNCHECKED && num < 0) {
            throw  IllegalStateException("the num can't be negative")
        }
        checkedNum = num
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        // draw outer and inner shadow
        initShadowPaint()
        shadowPaint?.apply {
            canvas?.drawCircle(
                halfDensitySize, halfDensitySize,
                density.times(STROKE_RADIUS + STROKE_WIDTH / 2 + SHADOW_WIDTH), this
            )
        }

        // draw white stroke
        strokePaint?.apply {
            canvas?.drawCircle(
                halfDensitySize, halfDensitySize, density.times(STROKE_RADIUS), this
            )
        }

        // draw content
        if (countable) {
            if (checkedNum != UNCHECKED) {
                initBackgroundPaint()
                backgroundPaint?.apply {
                    canvas?.drawCircle(
                        halfDensitySize, halfDensitySize, density.times(BG_RADIUS), this
                    )
                }
                initTextPaint()
                textPaint?.apply {
                    val text = checkedNum.toString()
                    val baseX = (width - measureText(text)) / 2
                    val baseY = (height - descent() - ascent()) / 2
                    canvas?.drawText(text, baseX, baseY, this)
                }
            }
        } else {
            if (checked) {
                initBackgroundPaint()
                backgroundPaint?.apply {
                    canvas?.drawCircle(
                        halfDensitySize, halfDensitySize, BG_RADIUS * density, this
                    )
                }
                if (canvas != null) {
                    checkDrawable?.bounds = getCheckRect()
                    checkDrawable?.draw(canvas)
                }
            }
        }
        alpha = if (enable) 1.0f else 0.5f
    }

    private fun getCheckRect(): Rect {
        if (checkRect == null) {
            val rectPadding = (halfDensitySize - CONTENT_SIZE * density / 2).toInt()
            checkRect = Rect(
                rectPadding, rectPadding,
                (SIZE * density - rectPadding).toInt(), (SIZE * density - rectPadding).toInt()
            )
        }
        return checkRect!!
    }

    private fun initTextPaint() {
        if (textPaint == null) {
            textPaint = TextPaint().run {
                isAntiAlias = true
                color = Color.WHITE
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 12.0f * density
                this
            }
        }
    }

    private fun initBackgroundPaint() {
        if (backgroundPaint == null) {
            backgroundPaint = Paint()
            backgroundPaint?.isAntiAlias = true
            backgroundPaint?.style = Paint.Style.FILL
            val ta: TypedArray =
                context.theme.obtainStyledAttributes(intArrayOf(R.attr.item_checkCircle_backgroundColor))
            val defaultColor = ResourcesCompat.getColor(
                context.resources, R.color.item_checkCircle_backgroundColor, context.theme
            )
            val color = ta.getColor(0, defaultColor)
            ta.recycle()
            backgroundPaint?.color = color
        }
    }

    private fun initShadowPaint() {
        if (shadowPaint == null) {
            shadowPaint = Paint()
            shadowPaint?.isAntiAlias = true
            val outerRadius: Float = STROKE_RADIUS + STROKE_WIDTH / 2
            val innerRadius = outerRadius - STROKE_WIDTH
            val gradientRadius = outerRadius + SHADOW_WIDTH
            val stop0 = (innerRadius - STROKE_WIDTH) / gradientRadius
            val stop1 = innerRadius / gradientRadius
            val stop2 = outerRadius / gradientRadius
            val stop3 = 1f

            val shadow = ContextCompat.getColor(context, R.color.shadow)
            val shadowHint = ContextCompat.getColor(context, R.color.shadow_hint)
            shadowPaint?.shader = (RadialGradient(
                halfDensitySize, halfDensitySize, density.times(gradientRadius),
                intArrayOf(shadowHint, shadow, shadow, shadowHint),
                floatArrayOf(stop0, stop1, stop2, stop3), Shader.TileMode.CLAMP
            ))
        }
    }
}