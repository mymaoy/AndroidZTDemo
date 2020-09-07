/*
 * Copyright (C) 2008 ZXing authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.cameralib.decode;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.cameralib.OCRMyApplication;
import com.cameralib.camera.CameraManager;
import com.cameralib.google.nozxing.Result;
import com.littt.zt02library.R;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


/**
 * This class handles all the messaging which comprises the state machine for capture.
 */
public final class CaptureActivityHandler extends Handler {
    private static final String TAG = "ABC";

    private final DecodeThread mDecodeThread;
    private State mState;
    private   Timer timerpic;
    public CaptureActivityHandler() {

        mDecodeThread = new DecodeThread();
        mDecodeThread.start();
        mState = State.SUCCESS;
        restartPreviewAndDecode();
        taskpic.run();
        timerpic = new Timer(true);
        timerpic.schedule(taskpic,100, 100); //延时1000ms后执行，1000ms执行一次
    }
    int GetPicCount=2;
    TimerTask taskpic = new TimerTask() {
        public void run() {
            if(GetPicCount>0)
                GetPicCount--;
            if(GetPicCount==0)
            {
                mState = State.PREVIEW;
                CameraManager.get().requestPreviewFrame(mDecodeThread.getHandler(), R.id.decode);
                GetPicCount=2;
            }
            if(OCRMyApplication.getDatafromocr)
            {
                Log.v("ABC","getDatafromocr true");
//                try {
//                    Message message =Message.obtain(mDecodeThread.getHandler(), R.id.decode_failedB);
//                    message.sendToTarget();
//                    } catch (Exception e) {
//                    e.printStackTrace();
//                    }
                OCRMyApplication.getDatafromocr=false;
                // SmActivity.updateUI(OCRMyApplication.RegAllData,true);
            }

        }
    };
    @Override
    public void handleMessage(Message message) {


        if(message.what== R.id.auto_focus)
        {

            if (mState == State.PREVIEW)
            {
                CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
            }
        }
        else if(message.what==R.id.decode_succeeded)
        {
            Log.e(TAG, "Got decode succeeded message");
            mState = State.SUCCESS;
            //mActivity.handleDecode((Result) message.obj);
        }
        else if(message.what==R.id.decode_failed)
        {
            mState = State.PREVIEW;
            CameraManager.get().requestPreviewFrame(mDecodeThread.getHandler(), R.id.decode);
        }
//        else if(message.what==R.id.decode_failedA)
//        {
//            mState = State.PREVIEW;
//            CameraManager.get().requestPreviewFrame(mDecodeThread.getHandler(), R.id.decodeA);
//        }
        else if(message.what==R.id.decode_failedB)
        {
//            mState = State.PREVIEW;
//            CameraManager.get().requestPreviewFrame(mDecodeThread.getHandler(), R.id.decodeB);

            Log.v("ABC","decode_RegAllData true");
            //SmActivity.updateUI(OCRMyApplication.RegAllData,true);

        }
        else if(message.what==R.id.decode_failedC)
        {

            Log.v("ABC","decode_failedC---");
           // SmActivity.UpdateFace(OCRMyApplication.bitmapface);

        }
        else if(message.what==R.id.deconde_ocr)
        {

        }
        else if(message.what== R.id.deconde_ActivityClose)
        {
            ActivityFormClose();
        }

    }
    private void ActivityFormClose(){
    }



    public void restartPreviewAndDecode() {
        Log.v("ABC","restartPreviewAndDecode 1");
        if (mState != State.PREVIEW) {
            Log.v("ABC","restartPreviewAndDecode 2");
            CameraManager.get().startPreview();
            mState = State.PREVIEW;
            CameraManager.get().requestPreviewFrame(mDecodeThread.getHandler(), R.id.decode);
            CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
        }
    }

    private enum State {
        PREVIEW, SUCCESS, DONE
    }

    public void onPause() {

        mState = State.DONE;
        CameraManager.get().stopPreview();
    }
    public void WriteTXT(String sInfor)
    {
        FileWriter writer = null;
        String sName="OCRTest.TXT";
        sInfor=GetTimeStr()+"\r\n"+sInfor;
        sName= Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+sName;
        File f = new File(sName);
        if (!f.exists())
        {
            try{
                f.createNewFile();
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        try {
            writer = new FileWriter(sName, true);
            if (writer != null)writer.write(sInfor);
            if (writer != null)writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public String GetTimeStr()
    {
        SimpleDateFormat formatter    =   new    SimpleDateFormat    ("[yyyy年MM月dd日HH:mm:ss]");
        Date curDate    =   new    Date(System.currentTimeMillis());
        String    str    =    formatter.format(curDate);
        return str;
    }

}