package com.xingkong.starsweather.util;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import java.util.ArrayList;

public class Ifly {

   public  SpeechSynthesizer  mTts;

    public  boolean status=true;

    /**
     * 语音识别
     * @param context
     */
    public static void Dialoginit(final Context context, final EditText text){
        RecognizerDialog mDialog=new RecognizerDialog(context,null);
        //2.设置accent、language等参数
        mDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        mDialog.setParameter(SpeechConstant.ACCENT, "mandarin");
        //3.设置回调接口
        mDialog.setListener(new RecognizerDialogListener() {
            @Override
            public void onResult(RecognizerResult recognizerResult, boolean isLast) {
                if (!isLast) {
                    //解析语音
                    final String result = parseJson(recognizerResult.getResultString());
                    ((FragmentActivity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            text.setText(result);
                        }
                    });
                }
            }

            @Override
            public void onError(SpeechError speechError) {
                Toast.makeText(context,"输入错误",Toast.LENGTH_LONG).show();
            }
        });
        //4.显示dialog，接收语音输入
        mDialog.show();
    }

    /**
     *将文字信息转化为可听的声音信息
     * @param context
     * @return
     */
    public  void speak(String text) {
        //1.创建SpeechSynthesizer对象, 第二个参数：本地合成时传InitListener
       //  mTts = SpeechSynthesizer.createSynthesizer(context, null);
//2.合成参数设置，详见《科大讯飞MSC API手册(Android)》SpeechSynthesizer 类
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoqi");//设置发音人
        mTts.setParameter(SpeechConstant.SPEED, "50");//设置语速
        mTts.setParameter(SpeechConstant.VOLUME, "80");//设置音量，范围0~100
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端
//设置合成音频保存位置（可自定义保存位置），保存在“./sdcard/iflytek.pcm”
//保存在SD卡需要在AndroidManifest.xml添加写SD卡权限
//如果不需要保存合成音频，注释该行代码
       // mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory().getPath()+"iflytek.MP3");
//3.开始合成
        mTts.startSpeaking(text,
//合成监听器
                new SynthesizerListener() {
                    //会话结束回调接口，没有错误时，error为null
                    public void onCompleted(SpeechError error) {
                    }

                    //缓冲进度回调
                    //percent为缓冲进度0~100，beginPos为缓冲音频在文本中开始位置，endPos表示缓冲音频在文本中结束位置，info为附加信息。
                    public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
                    }

                    //开始播放
                    public void onSpeakBegin() {
                    }

                    //暂停播放
                    public void onSpeakPaused() {

                    }

                    //播放进度回调
                    //percent为播放进度0~100,beginPos为播放音频在文本中开始位置，endPos表示播放音频在文本中结束位置.
                    public void onSpeakProgress(int percent, int beginPos, int endPos) {
                    }

                    //恢复播放回调接口
                    public void onSpeakResumed() {
                    }

                    //会话事件回调接口
                    public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
                    }
                }
        );
    }



    private static String parseJson(String resultString){
        Gson gson=new Gson();
        Voice voice=gson.fromJson(resultString,Voice.class);

        StringBuilder sb=new StringBuilder();
        ArrayList<Voice.WSBean> ws=voice.ws;
        for(Voice.WSBean wsbean:ws){
            String word=wsbean.cw.get(0).w;
            sb.append(word);
        }
        return sb.toString();
    }

    public class Voice{
        public ArrayList<WSBean> ws;
        public class WSBean{
            public ArrayList<CWBean> cw;
        }

        public class CWBean{
            public String w;
        }
    }
}
