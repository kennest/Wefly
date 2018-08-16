package com.wefly.wealert.activities;

import android.app.Activity;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Button;

import com.github.piasy.rxandroidaudio.AudioRecorder;
import com.wefly.wealert.R;

import java.io.File;
import java.util.ArrayList;


public class RecorderActivity extends Activity {
    Button btnSave;
    AudioRecorder mAudioRecorder;
    File mAudioFile;
    String audioPath;
    ArrayList<String> audioPathList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> {
            StopRecord();
            finish();
        });

        Thread thread = new Thread() {
            public void run() {
                Log.d("TREAD RUNNNING", "OK");
                initRecord();
            }
        };
        thread.start();
        setResult(200, getIntent());
    }

    private void initRecord() {
        mAudioRecorder = AudioRecorder.getInstance();
        audioPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + System.nanoTime() + ".m4a";
        mAudioFile = new File(audioPath);
        mAudioRecorder.prepareRecord(MediaRecorder.AudioSource.MIC,
                MediaRecorder.OutputFormat.MPEG_4, MediaRecorder.AudioEncoder.AAC,
                mAudioFile);
        mAudioRecorder.startRecord();
        Log.v("INIT RECORD", "OK");
    }

    @Override
    public void finish() {
        audioPathList.add(audioPath);
        getIntent().putExtra("audioPath", audioPath);
        Log.v("RECORD FILEPATH", audioPath);
        setResult(200, getIntent());
        super.finish();
    }

    public void StopRecord() {
        mAudioRecorder.stopRecord();
    }
}
