package com.demo.recordaudio;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private CheckBox cb_record;
    private AudioRecordView record_view;
    private TextView tv_time;
    private ImageView iv_reset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cb_record = findViewById(R.id.cb_record);
        record_view = findViewById(R.id.record_view);
        tv_time = findViewById(R.id.tv_time);
        iv_reset = findViewById(R.id.iv_reset);
        cb_record.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                record_view.start();
            } else {
                record_view.stop();
            }
        });
        record_view.setOnRecordListener(time -> tv_time.setText(time));
        iv_reset.setOnClickListener(v -> record_view.reset());
    }
}