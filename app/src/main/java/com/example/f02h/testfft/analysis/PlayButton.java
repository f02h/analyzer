package com.example.f02h.testfft.analysis;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.AudioRecord;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.f02h.testfft.MainActivity;
import com.example.f02h.testfft.R;

import java.io.IOException;

public class PlayButton extends Button {
    boolean mStartPlaying = true;

    private MediaPlayer   mPlayer = null;


    OnClickListener clicker = new OnClickListener() {
        public void onClick(View v) {
            onPlay(mStartPlaying);
            if (mStartPlaying) {
                setText("Stop playing");
            } else {
                setText("Start playing");
            }
            mStartPlaying = !mStartPlaying;
        }
    };

    public PlayButton(Context ctx , AttributeSet attrs) {
        super(ctx,attrs);
        setText("Start playing");
        setOnClickListener(clicker);
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        mPlayer = MediaPlayer.create(MainActivity.getAppContext(), R.raw.sp10);
        mPlayer.start();
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

//    public void onPause() {
//        super.onPause();
//        if (mRecorder != null) {
//            mRecorder.release();
//            mRecorder = null;
//        }
//
//        if (mPlayer != null) {
//            mPlayer.release();
//            mPlayer = null;
//        }
//    }
}