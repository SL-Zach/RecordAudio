package com.demo.recordaudio;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.Random;

/**
 * @author : Zach
 * @desc : 音频裁剪自定义View
 * @date : 2021/1/23 15:11
 */
public class AudioClipView extends View {

    private final int SCROLL_NONE = 0X20;
    private final int SCROLL_LEFT_THUMB = 0X21;
    private final int SCROLL_RIGHT_THUMB = 0X22;
    private final int SCROLL_CURSOR = 0X23;

    private final int DEFAULT_MAX_MILLISECOND = 120 * 1000;
    private final int DEFAULT_MIN_MILLISECOND = 5 * 1000;
    /**
     * 上下文
     */
    private Context mContext;
    /**
     * 滑动状态
     */
    private int mScrollState = SCROLL_NONE;
    /**
     * 选中部分的矩形范围
     */
    private RectF mRectF;
    /**
     * 左边滑动图标的矩形范围
     */
    private RectF mLeftRectF;
    /**
     * 右边滑动图标的矩形范围
     */
    private RectF mRightRectF;
    /**
     * 指示器的矩形范围
     */
    private RectF mCursorRectF;
    /**
     * 波形的矩形范围
     */
    private RectF mWaveRectF;
    /**
     * 画笔
     */
    private Paint mPaint;
    /**
     * 指示器宽度 8dp
     */
    private float mCursorWidth;
    /**
     * 左边滑块图片
     */
    private Bitmap mLeftBitmap;
    /**
     * 右边滑块图片
     */
    private Bitmap mRightBitmap;
    /**
     * 左边滑块资源ID
     */
    private int mLeftBitmapId;
    /**
     * 右边滑块资源ID
     */
    private int mRightBitmapId;
    /**
     * 波形线宽度
     */
    private int mWaveLineWidth;
    /**
     * 波形线间隔
     */
    private int mWaveLineGap;
    /**
     * 最小间隔时长对应的px
     */
    private float mMinIntervalPx = 200;
    /**
     * 最大间隔时长对应的px
     */
    private float mMaxIntervalPx = 200;

    private float mLastX = 0f;

    private float minDuration = 0f;
    private float durationPx = 0f;

    private int mBackgroundColor;
    private int mCursorColor;
    private int mRoundRadius;
    private int mBorderColor;
    private float mBorderWidth = 6;
    private int mViewHeight;

    private OnScrollListener mOnScrollListener;

    /**
     * 最大时长 120s
     */
    private int mMaxMilliSecond = DEFAULT_MAX_MILLISECOND;
    /**
     * 最小时长 5s
     */
    private int mMinMilliSecond = DEFAULT_MIN_MILLISECOND;

    public AudioClipView(Context context) {
        super(context);
        mContext = context;
        getAttr(null);
        init();
    }

    public AudioClipView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        getAttr(attrs);
        init();
    }

    public AudioClipView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        getAttr(attrs);
        init();
    }

    public void setOnScrollListener(OnScrollListener mOnScrollListener) {
        this.mOnScrollListener = mOnScrollListener;
    }

    private void updateThumbCallback(boolean isLeft) {
        if (mOnScrollListener != null) {
            float indexPx = mCursorRectF.left + mCursorWidth / 2;
            int indexTime = (int) ((indexPx - getLeftBitmapWidth()) * mMaxMilliSecond / mMaxIntervalPx);
            ScrollInfo info = new ScrollInfo();
            if (isLeft) {
                int startTime = (int) ((mLeftRectF.right - getLeftBitmapWidth()) * mMaxMilliSecond / mMaxIntervalPx);
                info.setTime(startTime);
                info.setPosition(mLeftRectF.right);
            } else {
                int endTime = (int) ((mRightRectF.left - getRightBitmapWidth()) * mMaxMilliSecond / mMaxIntervalPx);
                info.setTime(endTime);
                info.setPosition(mRightRectF.left);
            }
            mOnScrollListener.onScrollThumb(isLeft, info);
        }
    }

    private void updateCursorCallback() {
        if (mOnScrollListener != null) {
            float indexPx = mCursorRectF.left + mCursorWidth / 2;
            int indexTime = (int) ((indexPx - getLeftBitmapWidth()) * mMaxMilliSecond / mMaxIntervalPx);
            ScrollInfo info = new ScrollInfo();
            info.setTime(indexTime);
            info.setPosition(indexPx);
            mOnScrollListener.onScrollCursor(info);
        }
    }

    private void initPx() {
        // 延时处理，保证mWidth > 0
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getWidth() > 0) {
                    mMaxIntervalPx = getWidth() - getLeftBitmapWidth() - getRightBitmapWidth();
                    mMinIntervalPx = mMaxIntervalPx * mMinMilliSecond / mMaxMilliSecond;
                }
            }
        }, 100);
    }

    private void init() {
        mRectF = new RectF();
        mLeftRectF = new RectF();
        mRightRectF = new RectF();
        mCursorRectF = new RectF();
        mWaveRectF = new RectF();
        mPaint = new Paint();

        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(mBorderWidth);
        //todo 处理自定义属性，线的颜色，边界的切图
        mLeftBitmap = BitmapFactory.decodeResource(getContext().getResources(), mLeftBitmapId);
        mRightBitmap = BitmapFactory.decodeResource(getContext().getResources(), mRightBitmapId);
        mLeftRectF.left = 0f;
        mLeftRectF.right = getLeftBitmapWidth();
        mRectF.left = mLeftRectF.right;
        mCursorRectF.left = mLeftRectF.right - mCursorWidth / 2;
        mCursorRectF.top = 0;
        mCursorRectF.right = mLeftRectF.right + mCursorWidth / 2;
        mCursorRectF.bottom = mViewHeight;
        mWaveRectF.left = mLeftRectF.right;
        mWaveRectF.top = mLeftRectF.top + mBorderWidth;
        initPx();
    }

    /**
     * 设置最小时长，单位毫秒
     *
     * @param minMilliSec
     */
    public void setMinInterval(int minMilliSec) {
        if (minMilliSec >= DEFAULT_MIN_MILLISECOND && minMilliSec <= DEFAULT_MAX_MILLISECOND) {
            mMinMilliSecond = minMilliSec;
        }
        initPx();
    }

    /**
     * 设置最小时长，单位毫秒
     *
     * @param maxMilliSec
     */
    public void setMaxInterval(int maxMilliSec) {
        if (maxMilliSec >= DEFAULT_MIN_MILLISECOND && maxMilliSec <= DEFAULT_MAX_MILLISECOND) {
            mMaxMilliSecond = maxMilliSec;
        }
        initPx();
    }


    /**
     * 更新指示器位置
     */
    public void updateCursor(float indexTime) {
        if (getWidth() == 0) {// 布局还未测量
            return;
        }
        float index = indexTime * mMaxIntervalPx / mMaxMilliSecond + getLeftBitmapWidth();
        mCursorRectF.left = index - mCursorWidth / 2;
        mCursorRectF.right = mCursorRectF.left + mCursorWidth;

        if (index > mRightRectF.left) {
            index = mRightRectF.left;
            mCursorRectF.right = index + mCursorWidth / 2;
            mCursorRectF.left = index - mCursorWidth / 2;
        }
        updateCursorCallback();
        invalidate();
        if (index == mRightRectF.left) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    moveCursorToStart();
                    updateCursorCallback();
                    invalidate();
                }
            }, 20);
        }
    }

    private void getAttr(AttributeSet attrs) {
        // 读取属性值
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.AudioClipView);
        mBackgroundColor = typedArray.getColor(R.styleable.AudioClipView_backgroundColor, 0xFF1A1A1A);
        mCursorColor = typedArray.getColor(R.styleable.AudioClipView_cursorColor, Color.WHITE);
        mBorderColor = typedArray.getColor(R.styleable.AudioClipView_borderColor, 0xFF46AA5F);
        mBorderWidth = typedArray.getDimensionPixelSize(R.styleable.AudioClipView_borderWidth, 6);
        mRoundRadius = typedArray.getDimensionPixelSize(R.styleable.AudioClipView_roundRadius, 10);
        mCursorWidth = typedArray.getDimensionPixelSize(R.styleable.AudioClipView_cursorWidth, 6);
        mWaveLineWidth = typedArray.getDimensionPixelSize(R.styleable.AudioClipView_waveLineWidth, 4);
        mWaveLineGap = typedArray.getDimensionPixelSize(R.styleable.AudioClipView_waveLineGap, 6);
        mViewHeight = typedArray.getDimensionPixelSize(R.styleable.AudioClipView_viewHeight, (int) DisplayKit.dp2px(60));
        mLeftBitmapId = typedArray.getResourceId(R.styleable.AudioClipView_leftImage, 0);
        mRightBitmapId = typedArray.getResourceId(R.styleable.AudioClipView_rightImage, 0);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mRightRectF.right = Integer.valueOf(w).floatValue();
        mRightRectF.left = mRightRectF.right - getRightBitmapWidth();
        mRectF.right = mRightRectF.left;
        mRectF.top = 0;
        mRectF.bottom = Integer.valueOf(h).floatValue();

        mWaveRectF.right = mRightRectF.left;
        mWaveRectF.bottom = Integer.valueOf(h).floatValue() - mBorderWidth;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.UNSPECIFIED
                || heightMode == MeasureSpec.AT_MOST) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(mViewHeight, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制背景颜色
        int width = getWidth();
        int height = getHeight();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mBackgroundColor);
        canvas.drawRoundRect(0, 0, width, height, mRoundRadius, mRoundRadius, mPaint);
        // 画音频波形
        float left = mWaveRectF.left;
        int size = (int) (mWaveRectF.width() / mWaveLineWidth);
        mPaint.setColor(Color.parseColor("#666666"));
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            float right = left + mWaveLineWidth;
            int offset = random.nextInt(35) + mWaveLineGap;
            if (i % 2 != 0) {
                canvas.drawRect(left, mWaveRectF.top + offset, right, mWaveRectF.bottom - offset, mPaint);
            }
            left = right;
        }

        mPaint.setColor(Color.parseColor("#4446AA5F"));
        canvas.drawRect(mRectF.left, mRectF.top, mRectF.right, mRectF.bottom, mPaint);

        // 画指示器位置
        mPaint.setColor(mCursorColor);
        canvas.drawRect(mCursorRectF, mPaint);

        //画上下两根线
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mBorderColor);
        canvas.drawLine(mRectF.left, mBorderWidth / 2, mRectF.right, mBorderWidth / 2, mPaint);
        canvas.drawLine(mRectF.left, height - mBorderWidth / 2, mRectF.right, height - mBorderWidth / 2, mPaint);

        //画左边界
        Matrix matrix1 = new Matrix();
        matrix1.postTranslate(mLeftRectF.left, 0);
        matrix1.postScale(1, getHeight() / getLeftBitmapHeight(), mLeftRectF.left, 0);
        canvas.drawBitmap(this.mLeftBitmap, matrix1, mPaint);

        //画右边界
        Matrix matrix2 = new Matrix();
        matrix2.postTranslate(mRightRectF.right - getRightBitmapWidth(), 0);
        matrix2.postScale(1, getHeight() / getRightBitmapHeight(), mRightRectF.right - getRightBitmapWidth(), 0);
        canvas.drawBitmap(this.mRightBitmap, matrix2, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean consumed = false;
        if (!isEnabled()) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //判断是否点击点在边界上，如果是则处理此次事件。
                float downX = event.getX();
                mLastX = downX;
                if (downX > mLeftRectF.left && downX < mLeftRectF.left + getLeftBitmapWidth()) {
                    mScrollState = SCROLL_LEFT_THUMB;
                    mLastX = downX;
                    consumed = true;
                } else if (downX > mRightRectF.right - getRightBitmapWidth() && downX < mRightRectF.right) {
                    mScrollState = SCROLL_RIGHT_THUMB;
                    mLastX = downX;
                    consumed = true;
                } else if (downX > mLeftRectF.left + getRightBitmapWidth() && downX < mRightRectF.right - getRightBitmapWidth()) {
                    mScrollState = SCROLL_CURSOR;
                    mLastX = downX;
                    consumed = true;
                } else {
                    mScrollState = SCROLL_NONE;
                    consumed = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float dx = moveX - mLastX;
                if (mScrollState == SCROLL_LEFT_THUMB) {
                    float newPadding = Math.max(mLeftRectF.left + dx, 0f);
                    if (newPadding + getLeftBitmapWidth() < mRightRectF.right - getRightBitmapWidth()) {
                        if (mRightRectF.right - getRightBitmapWidth() - newPadding - getLeftBitmapWidth() > mMinIntervalPx) {
                            mLeftRectF.left = newPadding;
                            mLeftRectF.right = newPadding + getRightBitmapWidth();
                            moveCursorToStart();
                            updateThumbCallback(true);
                        } else {
                            mLeftRectF.left = mRightRectF.right - getRightBitmapWidth() - mMinIntervalPx - getLeftBitmapWidth();
                        }
                    } else {
                        mLeftRectF.left = mRightRectF.right - 2 * getRightBitmapWidth();
                    }
                    consumed = true;
                } else if (mScrollState == SCROLL_RIGHT_THUMB) {
                    float newPadding = Math.min(mRightRectF.right + dx, getWidth());
                    if (newPadding - getRightBitmapWidth() > mLeftRectF.left + getLeftBitmapWidth()) {
                        if (newPadding - getRightBitmapWidth() - mLeftRectF.left - getLeftBitmapWidth() > mMinIntervalPx) {
                            mRightRectF.left = newPadding - getRightBitmapWidth();
                            mRightRectF.right = newPadding;
                            moveCursorToStart();
                            updateThumbCallback(false);
                        } else {
                            mRightRectF.right = getRightBitmapWidth() + mLeftRectF.left + getLeftBitmapWidth() + mMinIntervalPx;
                        }
                    } else {
                        mRightRectF.right = mLeftRectF.left + 2 * getLeftBitmapWidth();
                    }
                    consumed = true;
                } else if (mScrollState == SCROLL_CURSOR) {
                    mCursorRectF.left = moveX - mCursorWidth / 2;
                    mCursorRectF.right = moveX + mCursorWidth / 2;
                    consumed = true;
                } else {
                    consumed = false;
                }
                if (consumed) {
                    mLastX = moveX;
                    mRectF.left = mLeftRectF.left + getLeftBitmapWidth();
                    mRectF.right = mRightRectF.right - getRightBitmapWidth();
                    if (mCursorRectF.left + mCursorWidth / 2 < mLeftRectF.right) {
                        mCursorRectF.left = mLeftRectF.right - mCursorWidth / 2;
                        mCursorRectF.right = mCursorRectF.left + mCursorWidth;
                    }
                    if (mCursorRectF.right - mCursorWidth / 2 > mRightRectF.left) {
                        mCursorRectF.right = mRightRectF.left + mCursorWidth / 2;
                        mCursorRectF.left = mCursorRectF.right - mCursorWidth;
                    }
                    updateCursorCallback();
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                //这里不考虑长按等事件，简单处理。 有需要可以再处理。
                consumed = true;
                mScrollState = SCROLL_NONE;
                break;
        }
        return consumed;
    }

    /**
     * 移动指示器到最左边
     */
    private void moveCursorToStart() {
        if (getWidth() == 0) {
            return;
        }
        mCursorRectF.left = mLeftRectF.right - mCursorWidth / 2;
        mCursorRectF.right = mCursorRectF.left + mCursorWidth;
        invalidate();
    }

    private float getLeftBitmapWidth() {
        return mLeftBitmap.getWidth();
    }

    private float getRightBitmapWidth() {
        return mRightBitmap.getWidth();
    }

    private float getLeftBitmapHeight() {
        return mLeftBitmap.getHeight();
    }

    private float getRightBitmapHeight() {
        return mRightBitmap.getHeight();
    }

    /**
     * 获得左边的坐标
     */
    private float getStart() {
        return mRectF.left;
    }

    /**
     * 获得右边的坐标
     */
    private float getEnd() {
        return mRectF.right;
    }

    public class ScrollInfo {
        private int time; //对应时间,毫秒
        private float position; //位置,px

        public int getTime() {
            return time;
        }

        public void setTime(int time) {
            this.time = time;
        }

        public float getPosition() {
            return position;
        }

        public void setPosition(float position) {
            this.position = position;
        }

        @Override
        public String toString() {
            return "ScrollInfo{" +
                    "time=" + time +
                    ", position=" + position +
                    '}';
        }
    }

    public interface OnScrollListener {
        /**
         * 滑动两边滑块监听
         *
         * @param info 滑动信息
         */
        void onScrollThumb(boolean isLeftThumb, ScrollInfo info);

        /**
         * 手动去滑动指示器
         *
         * @param info 滑动信息
         */
        void onScrollCursor(ScrollInfo info);
    }

}
