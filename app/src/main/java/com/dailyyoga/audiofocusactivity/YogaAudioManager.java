package com.dailyyoga.audiofocusactivity;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;

/**
 * @author: ZhaoJiaXing@gmail.com
 * @created on: 2019/11/28 14:17
 * @description:
 */
class YogaAudioManager {

    private static AudioManager mAudioManager;
    private static AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener;
    private static AudioListener mListener;
    private static AudioFocusRequest mAudioFocusRequest;

    static int requestAudioFocus(AudioListener listener) {
        if (mListener == null) {
            mListener = listener;
        } else {
            mListener.audioPause();
            mListener = listener;
        }
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) AudioApplication.mInstance.getSystemService(Context.AUDIO_SERVICE);
        }
        if (mAudioFocusChangeListener == null) {
            mAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
                @Override
                public void onAudioFocusChange(int focusChange) {
                    switch (focusChange) {
                        case AudioManager.AUDIOFOCUS_GAIN:
                            // 重新获取
                            // 网易云音乐暂停,通话结束，微信聊天结束，录音播放完毕 // 8.0测试机可以收到 audio_focus_gain ，10.0不可以
                            Log.e("YogaAudioManager", "audio_focus_gain");
                            if (mListener != null) {
                                mListener.audioStart();
                            }
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS:
                            // 长时间丢失
                            // 网易云音乐播放 微信聊天
                            Log.e("YogaAudioManager", "audio_focus_loss");
                            if (mListener != null) {
                                mListener.audioPause();
                            }
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                            // 暂时丢失
                            // 电话接听 录音播放
                            Log.e("YogaAudioManager", "audio_focus_loss_transient");
                            if (mListener != null) {
                                mListener.audioPause();
                            }
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            // 暂时获得焦点，不需要暂停其它已经申请的音频播放，但是需要降低音量
                            // 放大 or 放小音量处理
                            Log.e("YogaAudioManager", "audio_focus_loss_transient_can_duck");
                            break;
                    }
                }
            };
        }

        if (mAudioManager == null) return 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 8.0及其以上
            if (mAudioFocusRequest == null) {
                mAudioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setAudioAttributes(new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build())
                        .setAcceptsDelayedFocusGain(true)
                        .setOnAudioFocusChangeListener(mAudioFocusChangeListener)
                        .build();
            }
            // AUDIOFOCUS_REQUEST_FAILED == 0
            // AUDIOFOCUS_REQUEST_GRANTED == 1
            // AUDIOFOCUS_REQUEST_DELAYED == 2
            return mAudioManager.requestAudioFocus(mAudioFocusRequest);
        } else {
            // 8.0以下
            // AUDIOFOCUS_REQUEST_FAILED == 0
            // AUDIOFOCUS_REQUEST_GRANTED == 1
            return mAudioManager.requestAudioFocus(mAudioFocusChangeListener,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK, AudioManager.AUDIOFOCUS_GAIN);
        }
    }

    public interface AudioListener {

        void audioStart();

        void audioPause();

    }

    static int releaseFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && mAudioFocusRequest != null && mAudioManager != null) {
            return mAudioManager.abandonAudioFocusRequest(mAudioFocusRequest);
        }
        return 1;
    }
}
