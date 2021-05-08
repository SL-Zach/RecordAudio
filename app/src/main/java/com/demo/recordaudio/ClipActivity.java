package com.demo.recordaudio;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * @author : Zach
 * @desc :
 * @date : 2021/5/8 15:12
 */
public class ClipActivity extends AppCompatActivity {

    AudioClipView clip_view;
    private int cur = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clip);
        clip_view = findViewById(R.id.clip_view);
        clip_view.setOnScrollListener(new AudioClipView.OnScrollListener() {
            @Override
            public void onScrollThumb(boolean isLeftThumb, AudioClipView.ScrollInfo info) {
                if (isLeftThumb) {
                    Log.i("xie", "leftThumb: " + intToTime(info.getTime() / 1000));
                } else {
                    Log.d("xie", "rightThumb: " + intToTime(info.getTime() / 1000));
                }
            }

            @Override
            public void onScrollCursor(AudioClipView.ScrollInfo info) {
                cur = info.getTime();
                Log.w("xie", "onScrollCursor: " + intToTime(cur / 1000));
            }
        });
    }

    /**
     * 将秒转换为00:00
     *
     * @param seconds
     * @return
     */
    public String intToTime(int seconds) {
        if (seconds <= 0) {
            return "00:00";
        }
        String time = "";
        int min = seconds / 60;
        if (min < 10) {
            time = time + "0" + min + ":";
        } else {
            time = time + min + ":";
        }
        int sec = seconds - min * 60;
        if (sec < 10) {
            time = time + "0" + sec;
        } else {
            time = time + sec;
        }
        return time;
    }
}
