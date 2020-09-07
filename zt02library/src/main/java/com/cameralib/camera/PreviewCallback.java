/*
 * Copyright (C) 2010 ZXing authors
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

package com.cameralib.camera;




import android.graphics.Point;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import com.cameralib.OCRMyApplication;
import com.littt.zt02library.R;


final class PreviewCallback implements Camera.PreviewCallback {
    private static final String TAG = PreviewCallback.class.getName();
    private final CameraConfigurationManager mConfigManager;
    private Handler mPreviewHandler;
    private int mPreviewMessage;
    private int bOnff=0;

    PreviewCallback(CameraConfigurationManager configManager) {
        this.mConfigManager = configManager;
    }

    void setHandler(Handler previewHandler, int previewMessage) {
        this.mPreviewHandler = previewHandler;
        this.mPreviewMessage = previewMessage;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera)
    {
        Point point = mConfigManager.getCameraResolution();

        if (mPreviewHandler != null)
        {
           if(System.currentTimeMillis()- OCRMyApplication.BeginTime<1500)bOnff=0;
            Message message =mPreviewHandler.obtainMessage(mPreviewMessage, point.x, point.y, data);   //发送消息 A   001
            message.what= R.id.decode;
            message.sendToTarget();
            mPreviewHandler = null;
        }
        else
        {
            Log.v(TAG, "no handler callback.");
        }

    }



}
