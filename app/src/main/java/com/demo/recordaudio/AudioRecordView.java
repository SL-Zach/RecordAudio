package com.demo.recordaudio;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author : Zach
 * @desc :
 * @date : 2021/2/10 9:06
 */
public class AudioRecordView extends TextureView implements TextureView.SurfaceTextureListener {

    private final int DEFAULT_ZERO_DECIBEL_COLOR = 0xFF323232;
    private final int DEFAULT_SMALLER_DECIBEL_COLOR = 0xFF17A851;
    private final int DEFAULT_MEDIUM_DECIBEL_COLOR = 0xFFBFC122;
    private final int DEFAULT_LARGER_DECIBEL_COLOR = 0xFFC33535;
    private final int DEFAULT_RULER_COLOR = 0xff323232;
    private final int DEFAULT_TEXT_COLOR = 0xff9B9B9B;
    private final int DEFAULT_RULER_SPACE = (int) DisplayKit.dp2px(6);
    private final float DEFAULT_TEXT_SIZE = DisplayKit.sp2px(10);
    private final float DEFAULT_RULER_WIDTH = DisplayKit.dp2px(0.8f);
    private final float DEFAULT_SMALLER_RULER_HEIGHT = DisplayKit.dp2px(3f);
    private final float DEFAULT_LARGER_RULER_HEIGHT = DisplayKit.dp2px(7f);
    private final float DEFAULT_WAVE_WIDTH = DisplayKit.dp2px(1.8f);
    private final float DEFAULT_DECIBEL_HEIGHT = DisplayKit.dp2px(20f);

    /**
     * 刻度
     */
    private Paint mRulerPaint;//刻度画笔
    private int mRulerColor = DEFAULT_RULER_COLOR;//刻度的颜色
    private float mRulerWidth = DEFAULT_RULER_WIDTH;//刻度的宽度
    private float mRulerHeightSmall = DEFAULT_SMALLER_RULER_HEIGHT;//小刻度的高度
    private float mRulerHeightBig = DEFAULT_LARGER_RULER_HEIGHT;//大刻度的高度

    /**
     * 文本画笔
     */
    private TextPaint mTextPaint;
    private int mTextColor = DEFAULT_TEXT_COLOR;//文本颜色
    private float mTextSize = DEFAULT_TEXT_SIZE;//文本大小
    private int mRulerSpace = DEFAULT_RULER_SPACE;//刻度间的间隔

    private float waveWidth = DEFAULT_WAVE_WIDTH;
    private float waveSpace = DisplayKit.dp2px(2.8f);

    private float mDecibelHeight = DEFAULT_DECIBEL_HEIGHT;

    /**
     * 波形区域画笔
     */
    private Paint mAreaPaint;
    private int mAreaBg = 0xFF171717;//视频背景颜色

    /**
     * 中轴线画笔
     */
    private Paint mCursorPaint;
    private int mCursorColor = 0xff46AA5F;//中轴线画笔
    private int mCursorWidth = (int) DisplayKit.dp2px(1);
    private OnRecordListener mOnRecordListener;

    /**
     * 初始化偏移量
     */

    private float mInitPix;
    /**
     * 移动偏移量
     */
    private float mPixOffset = 0;//移动/滑动的距离，绘制的时候需要根据这个值来确定位置

    /**
     * 每秒有多少个像素
     */
    private float mPixSecond = 0;//每秒有多少个像素，s/px

    /**
     * 当前时间秒数（中轴线时间）
     */
    private float mCurrentSecond = 0;
    /**
     * 当前分贝值
     */
    private int mDecibel;

    /**
     * 波形数据集合
     */
    private List<Integer> mWaveDataList = new ArrayList<>();

    /**
     * 波形画笔
     */
    private Paint mWavePaint;
    /**
     * 波形画笔
     */
    private Paint mDecibelPaint;
    /**
     * 波形颜色
     */
    private int mWaveColor = 0xff434343;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mPixOffset -= (mRulerWidth + mRulerSpace) / 2;
            int max = (int) DisplayKit.dp2px(20f);
            int min = (int) DisplayKit.dp2px(10f);
            Random random = new Random();
            int s = random.nextInt(max) % (max - min + 1) + min;
            setDecibel(s);
            refreshCanvas();
            mHandler.sendEmptyMessageDelayed(100, 100);
            super.handleMessage(msg);
        }
    };

    public AudioRecordView(Context context) {
        super(context);
        setSurfaceTextureListener(this);
        initPaint();
    }

    public AudioRecordView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSurfaceTextureListener(this);
        initPaint();
    }

    public AudioRecordView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setSurfaceTextureListener(this);
        initPaint();
    }

    /**
     * 初始化属性
     *
     * @param attrs
     * @param defStyleAttr
     */
    private void initAttr(AttributeSet attrs, int defStyleAttr) {

    }

    public void reset() {
        mWaveDataList.clear();
        mPixOffset = mInitPix;
        refreshCanvas();
    }

    /**
     * 波纹算法
     *
     * @param decibel
     * @return
     */
    public double getValue(double decibel) {
        double y;
        if (decibel <= -15) {
            y = new Random().nextInt(2);
        } else {
            decibel = (decibel) / 10;
            y = 3.5 * decibel * decibel - 32;
        }

        if (y < 1) {
            y = 1;
        }
        Log.i("deb", "y=" + y);
        /**
         * 这个高度换机器要做适配
         */
        if (y > DisplayKit.dp2px(130)) {
            y = DisplayKit.dp2px(130);
        }
        return y;
    }

    /**
     * 设置分贝
     *
     * @param decibel
     */
    public void setDecibel(double decibel) {
        this.mDecibel = (int) getValue(decibel);
        int max = 20;
        int min = 3;
        Random random = new Random();
        int s = random.nextInt(max) % (max - min + 1) + min;
        mWaveDataList.add(0, this.mDecibel);
        invalidate();
    }

    public void setOnRecordListener(OnRecordListener mOnRecordListener) {
        this.mOnRecordListener = mOnRecordListener;
    }

    public void start() {
        mHandler.sendEmptyMessage(100);
    }

    public void stop() {
        mHandler.removeCallbacksAndMessages(null);
    }


    /**
     * 初始化画笔
     */
    private void initPaint() {
        mRulerPaint = new Paint();
        mRulerPaint.setAntiAlias(true);
        mRulerPaint.setColor(mRulerColor);
        mRulerPaint.setStrokeWidth(mRulerWidth);

        mCursorPaint = new Paint();
        mCursorPaint.setAntiAlias(true);
        mCursorPaint.setStrokeWidth(mCursorWidth);
        mCursorPaint.setColor(mCursorColor);

        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);

        mWavePaint = new TextPaint();
        mWavePaint.setAntiAlias(true);
        mWavePaint.setColor(mWaveColor);
        mWavePaint.setStrokeWidth(waveWidth);
        mWavePaint.setStrokeCap(Paint.Cap.ROUND);
        mWavePaint.setTextAlign(Paint.Align.RIGHT);

        mAreaPaint = new Paint();
        mAreaPaint.setAntiAlias(true);
        mDecibelPaint = new Paint();
        mDecibelPaint.setAntiAlias(true);
    }

    /**
     * 刷新视图
     */
    private void refreshCanvas() {
        Canvas canvas = lockCanvas();
        if (canvas != null) {
            canvas.drawColor(0xFF101010);//画背景、由于TextureView不支持直接设置背景颜色，只能按这种方式
            drawTextAndRuler(canvas);//画文本和刻度
            drawRecodeArea(canvas);//画有效区域
            drawCenterLine(canvas);//画中间标线
            drawDecibel(canvas);//画中间标线
        }
        unlockCanvasAndPost(canvas);
        if (mOnRecordListener != null) {
            mOnRecordListener.onRecordedTime(toTime((int) mCurrentSecond / 10));
        }
    }

    private void drawRecodeArea(Canvas canvas) {
        mAreaPaint.setColor(mAreaBg);
        canvas.drawRect(0, mTextSize * 1.5f + mRulerHeightBig, getWidth(), getHeight() - mDecibelHeight, mAreaPaint);
        for (int i = 0; i < mWaveDataList.size(); i++) {
            float x = (getWidth() / 2 - (i * mRulerSpace));
            int y = mWaveDataList.get(i);
            canvas.drawLine(x, getHeight() / 2, x, getHeight() / 2 - y, mWavePaint);
            canvas.drawLine(x, getHeight() / 2, x, getHeight() / 2 + y, mWavePaint);
        }
    }

    /**
     * 画文本和刻度
     *
     * @param canvas
     */

    private void drawTextAndRuler(Canvas canvas) {
        int viewWidth = getWidth();
        int itemWidth = (int) (mRulerWidth + mRulerSpace);//单个view的宽度
        mPixSecond = 10f / itemWidth;//itemWidth表示一秒钟，
        int count = viewWidth / itemWidth;
        if (mPixOffset < 0) {//<0表示往左边移动-->右滑
            count += -mPixOffset / 10;//需要加上移动的距离
        }
        Log.d("xie", "mPixOffset = " + mPixOffset);
        int leftCount = 0;
        //从屏幕左边开始画刻度和文本
        for (int index = leftCount; index < count; index++) {
            float rightX = index * itemWidth + mPixOffset;//右边方向x坐标
            if (index == 0) {//根据最左边的时刻算最中间的时刻，左边时刻(rightX*每秒多少像素)+中间时刻（view的宽度/2*每秒多少像素）
                if (rightX < 0) {//15分钟之后的移动
                    mCurrentSecond = viewWidth * mPixSecond / 2f + Math.abs(rightX * mPixSecond);
                } else {//15分钟之前的移动
                    mCurrentSecond = viewWidth * mPixSecond / 2f - rightX * mPixSecond;
                }
            }
            int divisor = 10;//除数、刻度精度
            if (index % divisor == 0) {//大刻度
                //画刻度
                canvas.drawLine(rightX, mTextSize * 1.5f + mRulerHeightBig, rightX, mTextSize * 1.5f, mRulerPaint);
                //画文本
                draText(canvas, index, rightX);
            } else {//小刻度
                //画下面小刻度
                canvas.drawLine(rightX, mTextSize * 1.5f + mRulerHeightBig, rightX, mTextSize * 1.5f + mRulerHeightBig - mRulerHeightSmall, mRulerPaint);
            }
        }
    }

    public static String unitFormat(int i) {// 时分秒的格式转换
        if (i >= 0 && i < 10) {
            return "0" + i;
        } else {
            return "" + i;
        }
    }

    /**
     * 将int类型数字转换成时分秒毫秒的格式数据
     *
     * @param time long类型的数据
     * @return HH:mm:ss
     * @author zero 2019/04/11
     */
    public String secToTime(int time) {
        String timeStr = null;
        if (time <= 0) {
            return "00:00";
        } else {
            int second = time / 10;
            int minute = second / 60;
            if (second < 60) {
                timeStr = "00:" + unitFormat(second);
            } else if (minute < 60) {
                second = second % 60;
                timeStr = unitFormat(minute) + ":" + unitFormat(second);
            } else {// 数字>=3600 000的时候
                int hour = minute / 60;
                minute = minute % 60;
                second = second - hour * 3600 - minute * 60;
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
            }
        }
        return timeStr;
    }

    public String toTime(int time) {
        String timeStr = null;
        if (time <= 0) {
            return "00:00:00";
        } else {
            int second = time / 10;
            int minute = second / 60;
            if (second < 60) {
                timeStr = "00:00:" + unitFormat(second);
            } else if (minute < 60) {
                second = second % 60;
                timeStr = "00:" + unitFormat(minute) + ":" + unitFormat(second);
            } else {// 数字>=3600 000的时候
                int hour = minute / 60;
                minute = minute % 60;
                second = second - hour * 3600 - minute * 60;
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
            }
        }
        return timeStr;
    }

    /**
     * 画时间文本
     *
     * @param canvas
     * @param time   当前时间
     * @param x      所画时间x轴坐标
     */
    public void draText(Canvas canvas, int time, float x) {
        String keyText = secToTime(time);
        float keyTextWidth = mTextPaint.measureText(keyText);
        float keyTextX = x - keyTextWidth / 2;
//        if (keyTextX <= getWidth() / 2) {
//            keyTickTextPaint.setColor(0xFF990000);
//        } else {
//            keyTickTextPaint.setColor(0xFF9B9B9B);
//        }
        canvas.drawText(keyText, keyTextX, mTextSize * 1.0f, mTextPaint);
    }

    /**
     * 画中间线
     *
     * @param canvas
     */
    private void drawCenterLine(Canvas canvas) {
        canvas.drawLine(getWidth() / 2, mTextSize * 1.5f + mRulerHeightBig, getWidth() / 2, getHeight() - mDecibelHeight, mCursorPaint);
        canvas.drawCircle(getWidth() / 2, mTextSize * 1.5f + mRulerHeightBig, DisplayKit.dp2px(3), mCursorPaint);
    }

    private void drawDecibel(Canvas canvas) {
        mDecibelPaint.setColor(0xFF232323);
        canvas.drawRect(0, getHeight() - mDecibelHeight, getWidth(), getHeight(), mDecibelPaint);
        float gap = DisplayKit.dp2px(3);
        float width = (getWidth() - 2 * DisplayKit.dp2px(2) - 33 * gap) / 34;
        int num = new Random().nextInt(35);
        for (int i = 0; i < 35; i++) {
            if (i <= num) {
                if (i < 17) {
                    mDecibelPaint.setColor(DEFAULT_SMALLER_DECIBEL_COLOR);
                } else if (i >= 17 && i < 28) {
                    mDecibelPaint.setColor(DEFAULT_MEDIUM_DECIBEL_COLOR);
                } else {
                    mDecibelPaint.setColor(DEFAULT_LARGER_DECIBEL_COLOR);
                }
            } else {
                mDecibelPaint.setColor(DEFAULT_ZERO_DECIBEL_COLOR);
            }
            float left = DisplayKit.dp2px(2) + i * (gap + width);
            canvas.drawRect(left, getHeight() - mDecibelHeight, left + width, getHeight(), mDecibelPaint);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mInitPix = getWidth() / 2;
        mPixOffset = mInitPix;
        refreshCanvas();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public interface OnRecordListener {
        void onRecordedTime(String time);
    }
}
