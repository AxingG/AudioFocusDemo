package com.dailyyoga.audiofocusactivity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements YogaAudioManager.AudioListener {

    TextView mTvMusic;
    MediaPlayer mMediaPlayer;
    MediaPlayer.OnPreparedListener mListener;
    private int status = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initListener();
        initMediaPlayer();
        mTvMusic = findViewById(R.id.tv_music);
        mTvMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (status == 0) {
                    Toast.makeText(MainActivity.this, "播放器没有准备好", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (status == 1 || status == 3) {
                    startPlay();
                } else if (status == 2) {
                    pausePlay();
                } else {
                    initMediaPlayer();
                    Toast.makeText(MainActivity.this, "播放器被销毁，开始重新创建", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initListener() {
        mListener = new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                status = 1;
            }
        };
    }

    private void initMediaPlayer() {
        try {
            AssetFileDescriptor descriptor = this.getAssets().openFd("music.mp3");
            if (mMediaPlayer == null) {
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                mMediaPlayer.setLooping(true);
                mMediaPlayer.setOnPreparedListener(mListener);
                mMediaPlayer.prepareAsync();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startPlay() {
        int result = YogaAudioManager.requestAudioFocus(this);
        if (result == 0) {
            Log.e("YogaAudioManager", "请求焦点失败");
        }
        if (mMediaPlayer != null) {
            status = 2;
            mTvMusic.setText("正在播放");
            mMediaPlayer.start();
        }
    }

    private void pausePlay() {
        if (mMediaPlayer != null) {
            status = 3;
            mMediaPlayer.pause();
            mTvMusic.setText("播放音乐");
        }
    }

    private void releasePlay() {
        if (mMediaPlayer != null) {
            status = 4;
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlay();
        YogaAudioManager.releaseFocus();
    }

    @Override
    public void audioStart() {
        startPlay();
    }

    @Override
    public void audioPause() {
        pausePlay();
    }
}
