package com.demo.recordaudio;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

/**
 * @author : Zach
 * @desc :
 * @date : 2020/12/22 12:05
 */
public final class DisplayKit {

    /**
     * Get Display
     *
     * @param context Context for get WindowManager
     * @return Display
     */
    private static Display getDisplay(Context context) {
        WindowManager wm;
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            wm = activity.getWindowManager();
        } else {
            wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        if (wm != null) {
            return wm.getDefaultDisplay();
        }
        return null;
    }

    /**
     * 获取屏幕实际高度
     *
     * @param context 上下文
     * @return 实际高度
     */
    public static int getRealHeight(Context context) {
        Display display = getDisplay(context);
        if (display == null) {
            return 0;
        }
        DisplayMetrics dm = new DisplayMetrics();
        display.getRealMetrics(dm);
        return dm.heightPixels;
    }

    /**
     * 获取屏幕实际宽度
     *
     * @param context 上下文
     * @return 实际宽度
     */
    public static int getRealWidth(Context context) {
        Display display = getDisplay(context);
        if (display == null) {
            return 0;
        }
        DisplayMetrics dm = new DisplayMetrics();
        display.getRealMetrics(dm);
        return dm.widthPixels;
    }

    /**
     * 获取屏幕高度，以像素为单位.
     *
     * @param context
     * @return 屏幕高度
     */
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) return -1;
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            wm.getDefaultDisplay().getRealSize(point);
        } else {
            wm.getDefaultDisplay().getSize(point);
        }
        return point.y;
    }

    /**
     * 获取屏幕宽度，以像素为单位.
     *
     * @param context
     * @return 屏幕宽度
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) return -1;
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            wm.getDefaultDisplay().getRealSize(point);
        } else {
            wm.getDefaultDisplay().getSize(point);
        }
        return point.x;
    }

    /**
     * 获取状态栏的高度
     *
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    /**
     * dp转px
     *
     * @param dpValue dp值
     * @return px值
     */
    public static float dp2px(final float dpValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue,
                Resources.getSystem().getDisplayMetrics());
    }

    /**
     * px转dp
     *
     * @param pxValue px值
     * @return dp值
     */
    public static float px2dp(final float pxValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (pxValue / scale + 0.5f);
    }

    /**
     * sp转px
     *
     * @param spValue sp值
     * @return px值
     */
    public static float sp2px(final float spValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue,
                Resources.getSystem().getDisplayMetrics());
    }

    /**
     * px转sp
     *
     * @param pxValue px值
     * @return sp值
     */
    public static float px2sp(final float pxValue) {
        final float fontScale = Resources.getSystem().getDisplayMetrics().scaledDensity;
        return (pxValue / fontScale + 0.5f);
    }
}
