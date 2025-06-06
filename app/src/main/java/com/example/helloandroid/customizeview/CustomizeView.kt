package com.example.helloandroid.customizeview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import java.util.*
import kotlin.math.*

class CustomizeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val mHourPointWidth = 15f     // 时针宽度
    private val mMinutePointWidth = 10f   // 分针宽度
    private val mSecondPointWidth = 4f    // 秒针宽度
    private val mPointRange = 20F         // 指针圆角弧度
    private val mNumberSpace = 10f        // 刻度线与数字之间的间距
    private val mCircleWidth = 4.0F       // 外圈圆的线条宽度
    private val scaleMax = 50             // 整点（长）刻度的长度
    private val scaleMin = 25             // 非整点（短）刻度的长度

    private var mWidth = 0                // View 宽度
    private var mHeight = 0               // View 高度
    private var radius = 300.0F           // 表盘半径，初始值 300

    // 画笔
    private val mPaint: Paint by lazy {
        Paint()
    }
    private val mRect: Rect by lazy {
        Rect()
    }


    // 初始化画笔属性，设置字体大小、加粗、抗锯齿，让绘制更清晰
    init {
        mPaint.textSize = 35F
        mPaint.typeface = Typeface.DEFAULT_BOLD
        mPaint.isAntiAlias = true
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        //调用了自定义的 onMeasuredSpec() 方法，对传入的宽度/高度进行计算
        //然后加上两倍 mCircleWidth（也就是外圈线条的宽度），防止线条画到外面去。
        mWidth = onMeasuredSpec(widthMeasureSpec) + (mCircleWidth * 2).toInt()
        mHeight = onMeasuredSpec(heightMeasureSpec) + (mCircleWidth * 2).toInt()

        //更新半径
        radius = (mWidth - mCircleWidth * 2) / 2
        setMeasuredDimension(mWidth, mHeight)
    }

    //计算“在不同测量模式下，这个 View 实际应该占多大”
    private fun onMeasuredSpec(measureSpec: Int): Int {

        var specViewSize = 0
        /*
        specMode：获取测量模式，有三个可能值：
        EXACTLY: 父布局希望你就是这个大小（比如 match_parent, 或设置了具体数值）。
        AT_MOST: 父布局希望你“不要超过这个大小”（比如 wrap_content）。
        UNSPECIFIED: 父布局没有限制（几乎不用管这种情况）。

        specSize：从 measureSpec 中取出的建议最大尺寸，单位是像素。
         */
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)

        when (specMode) {
            MeasureSpec.EXACTLY -> {
                specViewSize = specSize
            }
            MeasureSpec.AT_MOST -> {
                // 计算半径以宽高最小值为准
                specViewSize = min((radius * 2).toInt(), specSize)
            }
        }
        return specViewSize
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 设置圆心X轴位置
        val centerX: Float = (mWidth / 2).toFloat()
        // 设置圆心Y轴位置
        val centerY: Float = (mHeight / 2).toFloat()

        canvas.translate(centerX, centerY)

        /** 第一步：绘制最外层的圆 **/
        drawClock(canvas)

        /** 第二步：表盘一共60个刻度，1到12点整数属于长刻度，其余属于短刻度 **/
        drawClockScale(canvas)

        /** 第三步：绘制指针 **/
        drawPointer(canvas)

        postInvalidateDelayed(1000)

    }

    /**
     * 绘制表盘
     */
    private fun drawClock(canvas: Canvas) {

        // 设置外层圆画笔宽度
        mPaint.strokeWidth = mCircleWidth
        // 设置画笔颜色
        mPaint.color = Color.BLACK
        // 设置画笔空心风格
        mPaint.style = Paint.Style.STROKE
        // 绘制圆方法
        canvas.drawCircle(0F, 0F, radius, mPaint)
    }

    /**
     * 绘制表盘刻度
     */
    private fun drawClockScale(canvas: Canvas) {
        for (index in 1..60) {
            // 刻度绘制以12点钟为准，每次将表盘旋转6°，后续绘制都以12点钟为基准绘制
            canvas.rotate(6F, 0F, 0F)
            // 绘制长刻度线
            if (index % 5 == 0) {
                // 设置长刻度画笔宽度
                mPaint.strokeWidth = 4.0F
                // 绘制刻度线
                canvas.drawLine(0F, -radius, 0F, -radius + scaleMax, mPaint)
                /** 绘制文本 **/
                canvas.save()
                // 设置画笔宽度
                mPaint.strokeWidth = 1.0F
                // 设置画笔实心风格
                mPaint.style = Paint.Style.FILL
                mPaint.getTextBounds(
                    (index / 5).toString(),
                    0,
                    (index / 5).toString().length,
                    mRect
                )
                canvas.translate(0F, -radius + mNumberSpace + scaleMax + (mRect.height() / 2))
                canvas.rotate((index * -6).toFloat())
                canvas.drawText(
                    (index / 5).toString(), -mRect.width() / 2.toFloat(),
                    mRect.height().toFloat() / 2, mPaint
                )
                canvas.restore()
            }
            // 绘制短刻度线
            else {
                // 设置短刻度画笔宽度
                mPaint.strokeWidth = 2.0F
                canvas.drawLine(0F, -radius, 0F, -radius + scaleMin, mPaint)
            }
        }
    }

    /**
     * 绘制指针
     */
    private fun drawPointer(canvas: Canvas) {
        // 获取当前时间：时分秒
        val calendar = Calendar.getInstance()
        val hour = calendar[Calendar.HOUR]
        val minute = calendar[Calendar.MINUTE]
        val second = calendar[Calendar.SECOND]
        // 计算时分秒转过的角度
        val angleHour = (hour + minute.toFloat() / 60) * 360 / 12
        val angleMinute = (minute + second.toFloat() / 60) * 360 / 60
        val angleSecond = second * 360 / 60

        // 绘制时针
        canvas.save()
        // 旋转到时针的角度
        canvas.rotate(angleHour, 0F, 0F)
        val rectHour = RectF(
            -mHourPointWidth / 2,
            -radius / 2,
            mHourPointWidth / 2,
            radius / 6
        )
        // 设置时针画笔属性
        mPaint.color = Color.BLUE
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = mHourPointWidth
        canvas.drawRoundRect(rectHour, mPointRange, mPointRange, mPaint)
        canvas.restore()

        // 绘制分针
        canvas.save()
        // 旋转到分针的角度
        canvas.rotate(angleMinute, 0F, 0F)
        val rectMinute = RectF(
            -mMinutePointWidth / 2,
            -radius * 3.5f / 5,
            mMinutePointWidth / 2,
            radius / 6
        )
        // 设置分针画笔属性
        mPaint.color = Color.BLACK
        mPaint.strokeWidth = mMinutePointWidth
        canvas.drawRoundRect(rectMinute, mPointRange, mPointRange, mPaint)
        canvas.restore()

        // 绘制秒针
        canvas.save()
        // 旋转到分针的角度
        canvas.rotate(angleSecond.toFloat(), 0F, 0F)
        val rectSecond = RectF(
            -mSecondPointWidth / 2,
            -radius + 10,
            mSecondPointWidth / 2,
            radius / 6
        )
        // 设置秒针画笔属性
        mPaint.strokeWidth = mSecondPointWidth
        mPaint.color = Color.RED
        canvas.drawRoundRect(rectSecond, mPointRange, mPointRange, mPaint)
        canvas.restore()

        // 绘制原点
        mPaint.style = Paint.Style.FILL
        canvas.drawCircle(
            0F,
            0F, mSecondPointWidth * 4, mPaint
        )
    }
}