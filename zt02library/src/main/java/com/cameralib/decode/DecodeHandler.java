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

package com.cameralib.decode;

import android.content.Intent;
import android.graphics.Bitmap;

import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.ZTCardReaderLib;
import com.cameralib.OCRMyApplication;

import com.cameralib.google.nozxing.BarcodeFormat;
import com.cameralib.google.nozxing.DecodeHintType;
import com.cameralib.google.nozxing.MultiFormatReader;
import com.littt.zt02library.R;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;

import java.util.Map;




final class DecodeHandler extends Handler {


    private final MultiFormatReader mMultiFormatReader;
    private final Map<DecodeHintType, Object> mHints;

    ZTCardReaderLib ztCardReaderLib=new ZTCardReaderLib();



    DecodeHandler()
    {


        mMultiFormatReader = new MultiFormatReader();
        mHints = new Hashtable<>();
        mHints.put(DecodeHintType.CHARACTER_SET, "utf-8");
        mHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        Collection<BarcodeFormat> barcodeFormats = new ArrayList<>();
        barcodeFormats.add(BarcodeFormat.CODE_39);
        barcodeFormats.add(BarcodeFormat.CODE_128); // 快递单常用格式39,128
        barcodeFormats.add(BarcodeFormat.QR_CODE); //扫描格式自行添加
        mHints.put(DecodeHintType.POSSIBLE_FORMATS, barcodeFormats);

    }

    @Override
    public void handleMessage(Message message)
    {

        if(message.what==R.id.decode)
            decode((byte[]) message.obj, message.arg1, message.arg2);
        else
        {
            Looper looper = Looper.myLooper();
            if (null != looper) {
                looper.quit();
            }
        }
    }

    /**
     * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency, reuse the same reader
     * objects from one decode to the next.     *
     * @param data The YUV preview frame.
     * @param width The width of the preview frame.
     * @param height The height of the preview frame.
     */
    byte[] rawImage;
    Bitmap bitmap;
    ByteArrayOutputStream baos;

    boolean GetFaceEnable=false;
    private void decode(byte[] data, int width, int height) {

        RecongBmp(data,width, height,"A");
    }



    private void RecongBmp(byte[] data, int width, int height,String cc) {


    Log.v("ABC","PIC ----");
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;
        YuvImage yuvimage = new YuvImage(
                data,
                ImageFormat.NV21,
                width,
                height,
                null);
        baos = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(0, 0, width, height), 100, baos);// 80--JPG图片的质量[0-100],100最高
        rawImage = baos.toByteArray();
      //  将rawImage转换成bitmap
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        bitmap = BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length, options);
        if(OCRMyApplication.ReadOCREnable)
       OCRRegString(bitmap);
       //GetFaceMatFromBmp(bitmap);
        


//        mMultiFormatReader.reset();
//        try
//        {
//            Message message = Message.obtain(mActivity.getCaptureActivityHandler(), R.id.decode_failed);
//            message.sendToTarget();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    static  Bitmap bitmapreg;
    long regtimebefore;
    private void OCRRegString(Bitmap bitmap)
    {
       // Log.v("ABC","OCRRegString ----1");


        if(OCRMyApplication.Read_OCR_IC_ThreadEnable) return;//如果正在读 芯片,就不做OCR 分析
        if(OCRMyApplication.ReadPicOCREnable) return;
        bitmapreg=bitmap;
        //Log.v("ABC","OCRRegString ----2");
        regtimebefore=System.currentTimeMillis();
        new Thread(new Runnable() {
            @Override
            public void run() {

                OCRMyApplication.ReadPicOCREnable=true;

                byte[]    bux= ztCardReaderLib.RegBmpOCRData(bitmapreg);
              // Log.v("ABC","OCRRegString ----3");
                if (bux != null)
                {

                    String s = new String(bux);
                    if (s.length() > 5)
                    {

                      //  Log.v("ABC", "reg=" + s);
                        if (bux.length >= 206) {

                            String[] sInfo = new String[8];
                            int n = 0;
                            byte[] btype = new byte[4];
                            System.arraycopy(bux, 0, btype, 0, 4);
                            n += 4;
                            int m = 0;
                            sInfo[m++] = new String(btype);
                            byte[] bcode = new byte[20];
                            System.arraycopy(bux, n, bcode, 0, 20);
                            n += 20;
                            n += 2;
                            n += 4;

                            sInfo[m++] = new String(bcode);
                            Log.v("ABC","reg="+new String(bcode));

                            byte[] bCHNname = new byte[64];//中文名字与英文名字

                            System.arraycopy(bux, n, bCHNname, 0, 64);
                            n += 64;


                            byte[] bEngname = new byte[64];//中文名字与英文名字

                            System.arraycopy(bux, n, bEngname, 0, 64);
                            n += 64;


                            sInfo[m++] = new String(bEngname);//+new String(bCHNname);


                            byte[] bBirth = new byte[16];

                            System.arraycopy(bux, n, bBirth, 0, 16);
                            n += 16;

                            sInfo[m++] = new String(bBirth);

                            byte[] bsex = new byte[4];

                            System.arraycopy(bux, n, bsex, 0, 4);
                            n += 4;

                            sInfo[m++] = new String(bsex);
                            byte[] bguoji = new byte[4];
                            System.arraycopy(bux, n, bguoji, 0, 4);
                            n += 4;
                            n += 8;
                            sInfo[m++] = new String(bguoji);

                            byte[] bvalu = new byte[16];
                            System.arraycopy(bux, n, bvalu, 0, 16);
                            n += 16;

                            sInfo[m++] = new String(bvalu);

                            sInfo[m++] = " "+(System.currentTimeMillis()-regtimebefore)+" ms";//  +GetTimeStr();

                            for (int i = 0; i < 8; i++)
                                OCRMyApplication.RegAllData[i] = sInfo[i];

//
//                            try {
//                                Message message = Message.obtain(mActivity.getCaptureActivityHandler(), R.id.decode_failedB);
//                                message.sendToTarget();
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }





                            GetFaceEnable = true;

                        }
                    }


                }

                OCRMyApplication.ReadPicOCREnable=false;
            }


    }).start();
    }

    private String GetTimeStr() {
        String str = "";
        SimpleDateFormat formatter = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ");// new SimpleDateFormat("HH:mm:ss:SSS");
        Date curDate = new Date(System.currentTimeMillis());
        str = formatter.format(curDate);
        return str;
    }


    private  Bitmap createBitmapAA(Bitmap source, int x, int y, int width, int height, Matrix m, boolean filter) {
        Bitmap bitmap;
        try {
            bitmap = Bitmap.createBitmap(source, x, y, width, height, m, filter);
        } catch (OutOfMemoryError localOutOfMemoryError) {
            gc();
            bitmap = Bitmap.createBitmap(source, x, y, width, height, m, filter);
        }
        return bitmap;
    }
    private static void gc() {
        System.gc();
        // 表示java虚拟机会做一些努力运行已被丢弃对象（即没有被任何对象引用的对象）的 finalize
        // 方法，前提是这些被丢弃对象的finalize方法还没有被调用过
        System.runFinalization();
    }

    private void GetFaceMatFromBmp(Bitmap bitmap)
    {

        bitmapreg=bitmap;
        new Thread(new Runnable() {
            @Override
            public void run() {

                Log.v("ABC","Get Face IN");
                Bitmap bitmapdes= Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888);
                int ret=ztCardReaderLib.GetFaceBitmap(bitmapreg,bitmapdes);

                if(ret==0)
                {
                    Log.v("ABC","Get Face OK");
                    ztCardReaderLib.LEDRedOn();
//                    OCRMyApplication.bitmapface=bitmapdes;
//                    try {
//                        Message message = Message.obtain(mActivity.getCaptureActivityHandler(), R.id.decode_failedC);
//                        message.sendToTarget();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                  //  GetFaceEnable = false;
                }

            }
        }
        ).start();

    }

private Bitmap BmpCut(Bitmap bt)
{
    Matrix matrix = new Matrix();
    matrix.postScale(1, 1);
   Bitmap bmp= createBitmapAA(bt, 100,bitmap.getHeight()-180,bitmap.getWidth(),180, matrix,false);
   return bmp;
}
static  String RegSfile;
    private void  saveImageAA(Bitmap bmp,String sName)
    {
        Log.v("ABC","saveImageAA ");
        File appDir = new File(Environment.getExternalStorageDirectory(), "aapic");
        if (!appDir.exists())
        {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis()+sName + ".jpg";

        //  String fileName = sName+ ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        RegSfile=file.getAbsolutePath();
        new Thread(new Runnable() {
            @Override
            public void run() {

                Log.v("ABC","sf="+RegSfile);
                ztCardReaderLib.RegOcrData(RegSfile, 1);
            }
        }).start();

    }

}
