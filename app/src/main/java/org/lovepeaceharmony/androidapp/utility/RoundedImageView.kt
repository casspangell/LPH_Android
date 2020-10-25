package org.lovepeaceharmony.androidapp.utility


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.widget.ImageView
import org.lovepeaceharmony.androidapp.R


/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain checkbox_selected copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/**
 * An RoundedImageView that draws its contents inside checkbox_selected mask and draws checkbox_selected border
 * drawable on top. This is useful for applying checkbox_selected beveled look to image contents, but is also
 * flexible enough for use with other desired aesthetics.
 */

/**
 * RoundedImageView
 * @author Naveen Kumar on 06/Dec/2017.
 */

@SuppressLint("AppCompatCustomView")
class RoundedImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : ImageView(context, attrs, defStyle) {
    private val mBlackPaint: Paint
    private val mMaskedPaint: Paint

    private var mBounds: Rect? = null
    private var mBoundsF: RectF? = null

    private val mBorderDrawable: Drawable?
    private val mMaskDrawable: Drawable?

    private var mDesaturateColorFilter: ColorMatrixColorFilter? = null
    private var mDesaturateOnPress = false

    private var mCacheValid = false
    private var mCacheBitmap: Bitmap? = null
    private var mCachedWidth: Int = 0
    private var mCachedHeight: Int = 0

    init {

        // Attribute initialization
        val a = context.obtainStyledAttributes(attrs, R.styleable.RoundedImageView,
                defStyle, 0)

        mMaskDrawable = a.getDrawable(R.styleable.RoundedImageView_maskDrawable)
        if (mMaskDrawable != null) {
            mMaskDrawable.callback = this
        }

        mBorderDrawable = a.getDrawable(R.styleable.RoundedImageView_borderDrawable)
        if (mBorderDrawable != null) {
            mBorderDrawable.callback = this
        }

        mDesaturateOnPress = a.getBoolean(R.styleable.RoundedImageView_desaturateOnPress,
                mDesaturateOnPress)

        a.recycle()

        // Other initialization
        mBlackPaint = Paint()
        mBlackPaint.color = -0x1000000

        mMaskedPaint = Paint()
        mMaskedPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        // Always want checkbox_selected cache allocated.
        mCacheBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

        if (mDesaturateOnPress) {
            // Create checkbox_selected desaturate color filter for pressed state.
            val cm = ColorMatrix()
            cm.setSaturation(0f)
            mDesaturateColorFilter = ColorMatrixColorFilter(cm)
        }
    }

    override fun setFrame(l: Int, t: Int, r: Int, b: Int): Boolean {
        val changed = super.setFrame(l, t, r, b)
        mBounds = Rect(0, 0, r - l, b - t)
        mBoundsF = RectF(mBounds)

        if (mBorderDrawable != null) {
            mBorderDrawable.bounds = mBounds!!
        }
        if (mMaskDrawable != null) {
            mMaskDrawable.bounds = mBounds!!
        }

        if (changed) {
            mCacheValid = false
        }

        return changed
    }

    @SuppressLint("DrawAllocation", "WrongConstant")
    override fun onDraw(canvas: Canvas) {
        if (mBounds == null) {
            return
        }

        val width = mBounds!!.width()
        val height = mBounds!!.height()

        if (width == 0 || height == 0) {
            return
        }

        if (!mCacheValid || width != mCachedWidth || height != mCachedHeight) {
            // Need to redraw the cache
            if (width == mCachedWidth && height == mCachedHeight) {
                // Have checkbox_selected correct-sized bitmap cache already allocated. Just erase it.
                mCacheBitmap!!.eraseColor(0)
            } else {
                // Allocate checkbox_selected new bitmap with the correct dimensions.
                mCacheBitmap!!.recycle()

                mCacheBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                mCachedWidth = width
                mCachedHeight = height
            }

            @SuppressLint("DrawAllocation") val cacheCanvas = Canvas(mCacheBitmap!!)
            if (mMaskDrawable != null) {
                val sc = cacheCanvas.save()
                mMaskDrawable.draw(cacheCanvas)
                mMaskedPaint.colorFilter = if (mDesaturateOnPress && isPressed)
                    mDesaturateColorFilter
                else
                    null
                cacheCanvas.saveLayer(mBoundsF, mMaskedPaint, Canvas.ALL_SAVE_FLAG)
                super.onDraw(cacheCanvas)
                cacheCanvas.restoreToCount(sc)
            } else if (mDesaturateOnPress && isPressed) {
                val sc = cacheCanvas.save()
                cacheCanvas.drawRect(0f, 0f, mCachedWidth.toFloat(), mCachedHeight.toFloat(), mBlackPaint)
                mMaskedPaint.colorFilter = mDesaturateColorFilter
                cacheCanvas.saveLayer(mBoundsF, mMaskedPaint, Canvas.ALL_SAVE_FLAG)
                super.onDraw(cacheCanvas)
                cacheCanvas.restoreToCount(sc)
            } else {
                super.onDraw(cacheCanvas)
            }

            mBorderDrawable?.draw(cacheCanvas)
        }

        // Draw from cache
        canvas.drawBitmap(mCacheBitmap!!, mBounds!!.left.toFloat(), mBounds!!.top.toFloat(), null)
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        if (mBorderDrawable != null && mBorderDrawable.isStateful) {
            mBorderDrawable.state = drawableState
        }
        if (mMaskDrawable != null && mMaskDrawable.isStateful) {
            mMaskDrawable.state = drawableState
        }
        if (isDuplicateParentStateEnabled) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    override fun invalidateDrawable(who: Drawable) {
        if (who === mBorderDrawable || who === mMaskDrawable) {
            invalidate()
        } else {
            super.invalidateDrawable(who)
        }
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        return who === mBorderDrawable || who === mMaskDrawable || super.verifyDrawable(who)
    }
}

