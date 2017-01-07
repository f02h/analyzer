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

import java.io.File;
import java.io.IOException;

public class PlayButton extends Button {
    boolean mStartPlaying = true;

    private MediaPlayer   mPlayer = null;
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";

    OnClickListener clicker = new OnClickListener() {
        public void onClick(View v) {

            switch (v.getId()) {

                case R.id.PlayButton1:
                    // do your code
                    try {
                        onPlay(mStartPlaying,0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (mStartPlaying) {
                        setText("Stop playing");
                    } else {
                        setText("Start playing");
                    }
                    mStartPlaying = !mStartPlaying;
                    break;

                case R.id.PlayButton2:
                    // do your code
                    try {
                        onPlay(mStartPlaying,1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (mStartPlaying) {
                        setText("Stop playing");
                    } else {
                        setText("Start playing");
                    }
                    mStartPlaying = !mStartPlaying;
                    break;
                default:
                    break;
            }
//
//            try {
//                onPlay(mStartPlaying);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            if (mStartPlaying) {
//                setText("Stop playing");
//            } else {
//                setText("Start playing");
//            }
//            mStartPlaying = !mStartPlaying;
        }
    };

    public PlayButton(Context ctx , AttributeSet attrs) {
        super(ctx,attrs);
        setText("Start playing");
        setOnClickListener(clicker);
    }

    private void onPlay(boolean start, int pos) throws IOException {
        if (start) {
            startPlaying(pos);
        } else {
            stopPlaying();
        }
    }

    private void startPlaying(int pos) throws IOException {
//        String filepath = Environment.getExternalStorageDirectory().getPath();
//        File f = new File(filepath,AUDIO_RECORDER_FOLDER);
//        File file[] = f.listFiles();
//        mPlayer = MediaPlayer.create(MainActivity.getAppContext(), R.raw.piano2);
        mPlayer = new MediaPlayer();
        mPlayer.setDataSource(MainActivity.spectrogramViewFilename[pos]);
        mPlayer.prepare();
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