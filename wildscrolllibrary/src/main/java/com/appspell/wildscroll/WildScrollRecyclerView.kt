package com.appspell.wildscroll

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.AttributeSet
import android.view.MotionEvent
import appspell.com.wildscroll.R

class WildScrollRecyclerView : RecyclerView {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val sections = Sections(this)
    private val fastScroll = FastScroll(this)

    private var showSections = true
    private val textPaint = Paint()
    private val textSelectedPaint = Paint()
    private val sectionsPaint = Paint()
    private val sectionsRect = Rect()


    init {

        val textColor = ResourcesCompat.getColor(context.resources, R.color.primary_material_dark, null) //FIXME
        val textSelectedColor = ResourcesCompat.getColor(context.resources, R.color.accent_material_dark, null) //FIXME
        val backgroundColor = ResourcesCompat.getColor(context.resources, R.color.ripple_material_light, null) //FIXME

        val size = context.resources.getDimension(R.dimen.notification_action_text_size) //FIXME
        val selectedSize = context.resources.getDimension(R.dimen.notification_action_text_size)//FIXME

        val paddingLeft = context.resources.getDimension(R.dimen.abc_button_padding_horizontal_material)//FIXME
        val paddingRight = context.resources.getDimension(R.dimen.abc_button_padding_horizontal_material)//FIXME

        val textTypeFace = Typeface.DEFAULT
        val textSelectedTypeFace = Typeface.DEFAULT

        with(textPaint) {
            color = textColor
            isAntiAlias = true
            textSize = size
            typeface = textTypeFace
        }

        with(textSelectedPaint) {
            color = textSelectedColor
            isAntiAlias = true
            textSize = selectedSize
            typeface = textSelectedTypeFace
        }

        with(sectionsPaint) {
            color = backgroundColor
        }

        with(sections) {
            this.paddingLeft = paddingLeft
            this.paddingRight = paddingRight
        }

        with(fastScroll) {
            this.sections = sections
        }

        addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val firstItemPosition = when (layoutManager) {
                    is LinearLayoutManager -> (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    is StaggeredGridLayoutManager -> (layoutManager as StaggeredGridLayoutManager).findFirstVisibleItemPositions(null)[0]
                    else -> RecyclerView.NO_POSITION
                }

                sections.selected = fastScroll.getSectionIndexByScrollPosition(firstItemPosition)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
    }

    override fun onSizeChanged(width: Int, height: Int, oldw: Int, oldh: Int) {
        refreshSectionsUI(width, height)
        super.onSizeChanged(width, height, oldw, oldh)
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)

        canvas!!
        if (showSections) {
            canvas.drawRect(sectionsRect, sectionsPaint)

            fastScroll.sections = sections//FIXME
            val posX = sections.left + sections.paddingLeft

            sections.sections.entries.forEachIndexed { index, section ->
                val top = sections.top + (index + 1) * sections.height - sections.height / 2

                when (sections.selected == index) {
                    true -> canvas.drawText(section.key, posX, top + textSelectedPaint.textSize / 2, textSelectedPaint)
                    false -> canvas.drawText(section.key, posX, top + textPaint.textSize / 2, textPaint)
                }
            }
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {

        fastScroll.sections = sections //TODO
        if (fastScroll.onTouchEvent(ev)) {
            return true
        }

        return super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        fastScroll.sections = sections //TODO
        return if (showSections && fastScroll.onInterceptTouchEvent(ev)) true
        else super.onInterceptTouchEvent(ev)
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        adapter?.registerAdapterDataObserver(DataObserver())
        super.setAdapter(adapter)
    }

    override fun requestLayout() {
        if (sections != null) sections.refreshSections() //TODO unfortunately sections could be null =(
        super.requestLayout()
    }

    fun invalidateSectionBar() {
        invalidate(sectionsRect)
    }

    private fun refreshSectionsUI(width: Int, height: Int) {
        sections.changeSize(width, height, textSelectedPaint.textSize)

        if (sections.height > 0) {
            if (textPaint.textSize > sections.height) {
                textPaint.textSize = sections.height
            }

            if (textSelectedPaint.textSize > sections.height) {
                textSelectedPaint.textSize = sections.height
            }
        }

        with(sectionsRect) {
            left = sections.left.toInt()
            right = (sections.left + sections.width).toInt()
            top = 0
            bottom = height
        }
    }

    inner class DataObserver : AdapterDataObserver() {
        override fun onChanged() {
            sections.refreshSections()
            refreshSectionsUI(width, height)
            super.onChanged()
        }
    }
}