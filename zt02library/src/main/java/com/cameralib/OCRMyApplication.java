package com.cameralib;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;


public class OCRMyApplication extends Application {
    public static Context sAppContext;

    public static int CardType=0;//

    public static final int PassPortNum=13;//护照
    public  static final int Home_VC=15;//回乡证 Home-Visiting Certificate
    public static final  int Cell_syndrome=10;//台胞证
    public static final  int Gang_AoPass=22;//港澳通行证
    public static  long BeginTime=0;
    public static  boolean autoFocusb=false;
    public static Bitmap bitmapface;
    public static boolean ReadPicOCREnable=false;//OCR分析 图片
    public static   boolean Read_OCR_IC_ThreadEnable=false;

    public static boolean ReadIDCardThreadEnable=false;

    public static final String[] RegAllData=new String[11];
    public static Bitmap cameraBitmap;//相机预览的图片直接放在这里
    public static boolean getDatafromocr=false;//
    public static int iType=0;
    public  static boolean ReadOCREnable=true;//取图分析


    public static boolean ReadFaceEnable=false;
    public static boolean ReadGetFaceOK=false;
    public static int ReadFaceCount=0;
    public static Bitmap srcBitmap;


    @Override
    public void onCreate() {
        super.onCreate();
        sAppContext = this;

    }


}
