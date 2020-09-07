package com;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.cameralib.OCRMyApplication;

import com.cameralib.camera.CameraManager;
import com.cameralib.decode.CaptureActivityHandler;
import com.cameralib.view.ScannerFinderView;
import com.invs.invswlt;
import com.littt.zt02library.R;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimerTask;

import at.mroland.android.apps.imagedecoder.BitmapImageFactory;
import me.devilsen.czxing.code.BarcodeReader;
import me.devilsen.czxing.code.CodeResult;

import static android.content.ContentValues.TAG;
import static android.support.v4.content.ContextCompat.getSystemService;
import static android.util.Config.LOGV;

public class ZTCardReaderLib implements   Camera.PictureCallback{


    private boolean ReadCardEnable = false;
    private int ReadIDCardCount = 0;
    private int ReadPassInforAndPhotoCount = 0;
    private boolean readpicEnable = false;
    private String sMRZAllString = "";
    private String sOCRComString = "";
    public Bitmap bitmap;
    LocalSocket Localreceiver;
    boolean threadsenddata=false;
    boolean ThreadRun=false;
    public static String SOCKET_ADDRESS = "AndroidUSB.local.socket.address";
    private ScannerFinderView mQrCodeFinderView;
    private CaptureActivityHandler mCaptureActivityHandler;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private boolean DeviceOpenEnable = false;
    USBLocalServerSocket mLocalServerSocket=new USBLocalServerSocket();
    Thread LocalClient_Thread_RX=new Thread(new LocalClient_Thread_RX());
    UsbManager manager;
    private UsbDevice device=null;
    UsbInterface[] usbinterface=null;
    UsbEndpoint[][] endpoint=new UsbEndpoint[5][5];
    UsbDeviceConnection connection=null;
    byte[] mybuffer=new byte[1024];
    private int myvid=0x0483,mypid=0x5760;
    boolean bUsbConnected=false;
    public Handler getCaptureActivityHandler()
    {

        return mCaptureActivityHandler;
    }
    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

        Log.v("ABC", "222  onPictureTaken:");
        if (data == null) {
            return;
        }
        Log.v("ABC", "111  onPictureTaken:");
        mCaptureActivityHandler.onPause();

    }
    DataTransfer mydatatransfer=new DataTransfer(1024);
    /***
     *
     * @param iType 0--OCR 1--芯片基本信息 2 --芯片基本信息与照片
     * @return
     */
    public String ReadCardData(int iType)
    {
       // OCRMyApplication.iType=iType;
        if(OCRMyApplication.ReadOCREnable)
        {
            OCRMyApplication.ReadOCREnable=false;

//            for(int i=0;i<OCRMyApplication.RegAllData.length;i++)
//            {
//                OCRMyApplication.RegAllData[i]=OCRMyApplication.RegAllData[i]+" 222";
//            }
            String s=StringToJSON(OCRMyApplication.RegAllData);
            return s;
        }
        return  "";
    }
    public String StringToJSON(String[] RegResult)
    {
        int n=0;
       String sjson="{\"Type\":\""+RegResult[n++]+"\",";
        sjson=sjson+"\"Number\":\""+RegResult[n++]+"\",";
        sjson=sjson+"\"SignCountry\":\""+RegResult[n++]+"\",";
        sjson=sjson+"\"CHNName\":\""+RegResult[n++]+"\",";
        sjson=sjson+"\"Name\":\""+RegResult[n++]+"\",";
        sjson=sjson+"\"Birth\":\""+RegResult[n++]+"\",";
        sjson=sjson+"\"Nation\":\""+RegResult[n++]+"\",";
        sjson=sjson+"\"Sex\":\""+RegResult[n++]+"\",";
        sjson=sjson+"\"ValidDate\":\""+RegResult[n++]+"\",";
        sjson=sjson+"\"PICData\":\""+RegResult[n++]+"\",";
        sjson=sjson+"\"QREData\":\""+RegResult[n++]+"\"}";
        return sjson;
    }

    public int InitAllDev() {
        //初始化设备
        CameraManager.init();
        initCamera();
        initUsb();
        DeviceOpenEnable = true;
       // new ReadCardThread().start();
        new EIDXXXThread().start();
        new USBRxThread().start();
        mLocalServerSocket.start();
        LocalClient_Thread_RX.start();

        if (mCaptureActivityHandler == null)
        {
             mCaptureActivityHandler = new CaptureActivityHandler();
        }

        return 0;
    }
    private boolean  initUsb()
    {
       /// IntentFilter filter = new IntentFilter("android.hardware.usb.action.USB_DEVICE_DETACHED");

        boolean b=false;
        manager = (UsbManager)OCRMyApplication.sAppContext.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Log.e(TAG, "get device list  = " + deviceList.size());
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            device = deviceIterator.next();
           // String sVid=senddataedittext.getText().toString();
            //sVid=sVid+ "vid: " + device.getVendorId() + "\t pid: " + device.getProductId();
            //  senddataedittext.append(sVid+"\r\n");
            if((device.getVendorId()==myvid)&&(device.getProductId()==mypid)){
                break;
            }
        }
        if((device!=null)&&(device.getVendorId()==myvid)&&(device.getProductId()==mypid))
        {
            String tt="Vid:"+String.format("0x%04X",device.getVendorId()) +" ,pid: " + String.format("0x%04X",device.getProductId());
            Log.v("ABC",tt);
          //  senddataedittext.append(tt +"\r\n");
        }
        else{
           // senddataedittext.append("未发现支持设备\r\n");
            return false;
        }

        PendingIntent pi = PendingIntent.getBroadcast(OCRMyApplication.sAppContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
        if(manager.hasPermission(device))
        {
        }
        else
        {
            manager.requestPermission(device, pi);
        }

        if(manager.hasPermission(device))
        {
            ;
        }
        else
        {
            //senddataedittext.setText(senddataedittext.getText()+"\n未获得访问权限");
        }

       // senddataedittext.setText(senddataedittext.getText() +"\n"+device.getDeviceName());

       // senddataedittext.setText(senddataedittext.getText() +"\n接口数为："+device.getInterfaceCount()+"\r\n");

        usbinterface=new UsbInterface[device.getInterfaceCount()];
        for(int i=0;i<device.getInterfaceCount();i++)
        {
            usbinterface[i]=device.getInterface(i);

            Log.v("ABC","接口"+i+"的端点数为："+usbinterface[i].getEndpointCount()+"\r\n");
          //  senddataedittext.setText(senddataedittext.getText() +"接口"+i+"的端点数为："+usbinterface[i].getEndpointCount()+"\r\n");
            for(int j=0;j<usbinterface[i].getEndpointCount();j++)
            {
                endpoint[i][j]=usbinterface[i].getEndpoint(j);
                if(endpoint[i][j].getDirection()==0 )
                {
                    //senddataedittext.setText(senddataedittext.getText()+"端点"+j+"的数据方向为输出  i="+i);
                    bUsbConnected=true;
                    threadsenddata=true;
                    Log.v("ABC","端点"+j+"的数据方向为输出  i="+i);

                }
                else
                {
                    Log.v("ABC","端点"+j+"的数据方向为输入 i="+i);
                   // senddataedittext.setText(senddataedittext.getText()+"端点"+j+"的数据方向为输入 i="+i);
                }
                b=true;
            }

        }
        if(b)
        {
            connection = manager.openDevice(device);
            manager.requestPermission(device, pi);
            connection.claimInterface(usbinterface[0], true);
            return true;
        }

        return false;
    }
    private boolean initCamera() {
        try {
            if (!CameraManager.get().openDriver()) {
                return false;
            }
        } catch (IOException e) {

            return false;
        } catch (RuntimeException re) {
            re.printStackTrace();
            return false;
        }
        //mQrCodeFinderView.setVisibility(View.VISIBLE);
        //  findViewById(R.id.qr_code_view_background).setVisibility(View.GONE);
        if (mCaptureActivityHandler == null) {
            mCaptureActivityHandler = new CaptureActivityHandler();
        }
        return true;
    }

//    public class ReadCardThread extends Thread {
//
//        public void run() {
//            while(true)
//            {
//                if(ReadCardEnable)
//                {
//                    int ret=TestFindCard();
//                    if(ret==0)
//                    {
//                        //读卡
//                        Log.v("ABC","寻卡成功 ");
//                        ReadIDWithPhoto();
//                        ReadCardEnable=false;
//                        ReadIDCardCount=3;
//                        continue;
//                    }
//
//                }
//
//
//                try {
//                    sleep(200);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

    long RegTimems;
    private static Bitmap bmpsrc;
    private static String s_code,s_Name,s_Sex,s_birth,s_day;
    private void ReadIDWithPhoto()
    {
        RegTimems=System.currentTimeMillis();
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] RxBuff = TestReadIDWithPhoto();
                RegTimems=System.currentTimeMillis()-RegTimems;
                if (RxBuff != null)
                {
                    int rxlen=RxBuff.length;
                    bmpsrc=null;
                    if(rxlen>20)
                    {

                        String sName,sNationA,sBirth,sAdr,sNumber,sNationB,sBeginTime,sEndTime,sSex;

                        int n=0;
                        sName=GetStringUTF16(RxBuff,0,15*2);
                        s_Name=sName;
                        n=15*2;
                        sSex=GetStringUTF16(RxBuff,n,1*2);
                        if(sSex.contains("1")) sSex="男";
                        s_Sex=sSex;
                        n=n+2;

                        sNationA=GetStringUTF16(RxBuff,n,2*2);
                        n=n+4;

                        sBirth=GetStringUTF16(RxBuff,n,8*2);
                        s_birth=sBirth;
                        n=n+16;
                        sAdr=GetStringUTF16(RxBuff,n,70);
                        n=n+70;
                        sNumber=GetStringUTF16(RxBuff,n,36);
                        s_code=sNumber;
                        n=n+36;

                        sNationB=GetStringUTF16(RxBuff,n,30);
                        n=n+30;

                        sBeginTime=GetStringUTF16(RxBuff,n,16);
                        n=n+16;

                        sEndTime=GetStringUTF16(RxBuff,n,16);
                        s_day=sEndTime;
                        n=n+16;

                        sName=sName.trim()+","+sSex.trim()+","+sNationA.trim()+","+sBirth.trim()+"\r\n"+sAdr.trim()+"\r\n"+sNumber.trim()+","+sNationB.trim()+"\r\n"+sBeginTime.trim()+","+sEndTime.trim();
                        Log.v("ABC",sName);
                        //sMRZAllString=sName;

                        if (rxlen < 1280) return;//1281,0
                        byte[] picbuf = new byte[1024];
                        System.arraycopy(RxBuff, 256, picbuf, 0, 1024);

                        Log.v("ABC","ZpData=0-------------------");
                        byte[]ZpData= new byte[0];
                        try {
                            ZpData = invswlt.Wlt2Bmp(picbuf);
                        } catch (Exception e) {
                            Log.v("ABC", "invswlt ---ERROR ");
                            // e.printStackTrace();
                        }
                        if(ZpData!=null)
                        {
                            Log.v("ABC","ZpData="+ZpData.length);
                        }
                        else
                        {
                            Log.v("ABC","ZpData=null");
                        }

                        try {
                            bmpsrc =BitmapFactory.decodeByteArray(ZpData, 0, 38862);
                        } catch (Exception e) {
                            Log.v("ABC", "BitmapFactory ---ERROR ");
                            //e.printStackTrace();
                        }
                        if(bmpsrc!=null)
                        {
                            Log.v("ABC", "bmpsrc OK ");
                        }
                        else
                        {
                            Log.v("ABC", "bmpsrc null ");
                        }
                        //RegString[7]=RegTimems+" ms";
                    }

                }
                else
                {
                    //需要重复读 ID
                    // ReadICNumber="AA";
                }
            }
        }).start();
    }

    private String GetStringUTF16(byte[]Rx,int index,int len)
    {
        byte[]Buff=new byte[len];
        System.arraycopy(Rx,index,Buff,0,len);
        String s="";
        try {
            s=new String(Buff,"utf-16LE");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return s;

    }

    TimerTask task = new TimerTask() {
        public void run() {

            Log.v("ABC","ReadIDCardCount="+ReadIDCardCount);
            if(ReadIDCardCount>0)
            {
                ReadIDCardCount--;
                if(ReadIDCardCount==0)
                    ReadCardEnable=true;//开始读卡
            }

            Log.v("ABD","ReadPassInforAndPhotoCount="+ReadPassInforAndPhotoCount);
            if(ReadPassInforAndPhotoCount>0)
            {
                ReadPassInforAndPhotoCount--;
                if(ReadPassInforAndPhotoCount==0)
                {
                    if(readpicEnable)
                        readPassInfor(sOCRComString);
                    else
                        readPassBaseInfor(sOCRComString);
                }
            }

        }
    };
    private void readPassBaseInfor(String sOCRA) {


        Log.v("ABC","readPassInfor 1");

        ReadIDCardCount=20;
        ReadCardEnable=false;
        if(OCRMyApplication.Read_OCR_IC_ThreadEnable)
            return;
        sMRZAllString=sOCRA;
        Log.v("ABC","readPassInfor 2");

        RegTimems=System.currentTimeMillis();
        new Thread(new Runnable() {
            @Override
            public void run() {
                OCRMyApplication.Read_OCR_IC_ThreadEnable=true;

                Log.v("ABC"," ReadPassInfo = "+sMRZAllString);
                byte[] Bux = TestReadPassInfo(sMRZAllString,0);

                RegTimems=System.currentTimeMillis()-RegTimems;

                Log.v("ABC"," ReadPassInfo 1");
                if (Bux != null)
                {
                    if(Bux.length>5)
                    {
                        byte[]BuxBaseInfor=new byte[100];
                        System.arraycopy(Bux,0,BuxBaseInfor,0,90);
                        try {
                            String s=new String(BuxBaseInfor,"utf-8");
                            Log.v("ABC","Elec="+s);
                            sMRZAllString=s;
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        Log.v("ABC","Bux.length<5");
                    }

                }
                else
                {
                    //需要重复读 lv
                    // ReadICNumber="AA";
                    Log.v("ABC","Bux = null");

                }

                OCRMyApplication.Read_OCR_IC_ThreadEnable=false;
                BeenOn(50);
                ReadIDCardCount=2;
            }
        }).start();
    }

    private void readPassInfor(String sOCRA) {


        Log.v("ABC","readPassInfor 1");

        ReadIDCardCount=20;
        ReadCardEnable=false;
        if(OCRMyApplication.Read_OCR_IC_ThreadEnable)
            return;
        sMRZAllString=sOCRA;
        Log.v("ABC","readPassInfor 2");

        RegTimems=System.currentTimeMillis();
        new Thread(new Runnable() {
            @Override
            public void run() {
                OCRMyApplication.Read_OCR_IC_ThreadEnable=true;



//                try {
//                    sleep(500);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }



                Log.v("ABC"," ReadPassInfo = "+sMRZAllString);
                byte[] Bux =TestReadPassInfo(sMRZAllString,1);

                RegTimems=System.currentTimeMillis()-RegTimems;

                Log.v("ABC"," ReadPassInfo 1");
                if (Bux != null)
                {
                    if(Bux.length>5)
                    {
                        byte[]BuxBaseInfor=new byte[100];
                        System.arraycopy(Bux,0,BuxBaseInfor,0,90);
                        try {
                            String s=new String(BuxBaseInfor,"utf-8");
                            Log.v("ABC","Elec="+s);
                            sMRZAllString=s;
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        bitmap=null;
                        Log.v("ABC","Bux.len="+Bux.length);
                        if(Bux.length>=20608);//128+20591)
                        {
                            Log.v("ABC","Pic 001");
                            byte[] PicBux = new byte[20480];
                            System.arraycopy(Bux, 128, PicBux, 0, 20480);

//                            String s="";
//                            for(int i=0;i<100;i++)
//                            {
//                                s=s+String.format("%02X ",PicBux[i]);
//                            }
//                            Log.v("ABC","PicBux="+s);
                            BitmapImageFactory fac = BitmapImageFactory.get(PicBux);

                            if(fac!=null)
                            {
                                Log.v("ABC","Pic 002");
                                bitmap = fac.getImage();
                                if(bitmap!=null)
                                    Log.v("ABC","Pic 003 --OK");

                            }
                            else
                            {
                                Log.v("ABC","Pic fac=null");
                            }

                        }

                    }
                    else
                    {
                        Log.v("ABC","Bux.length<5");
                    }

                }
                else
                {
                    //需要重复读 lv
                    // ReadICNumber="AA";
                    Log.v("ABC","Bux = null");

                }

                OCRMyApplication.Read_OCR_IC_ThreadEnable=false;
                ReadIDCardCount=2;
                BeenOn(50);
            }
        }).start();
    }

    public class USBRxThread extends Thread {

        public void run() {

            byte[]UsbRx=new byte[64];


            while (true)
            {
                if(bUsbConnected)
                {
                    int n=connection.bulkTransfer(endpoint[0][0], UsbRx, 64, 150);
                    if(n>0)
                    {
//                        String s="";
//                        for(int i=0;i<64;i++)
//                        {
//                            s=s+String.format("%02X ",UsbRx[i]);
//                        }
//                        Log.v("ABC","USBRX="+s);




                        //Log.v("ABC","USB RX");
                        TransmitTCPTXData(UsbRx);

                    }

//                    try {
//                        sleep(1);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }


                }
            }
        }
    }
    public int Tx_USB(byte[]TxBux,int len)
    {
//        byte[]TxBux=new byte[64];
//        TxBux[0]=0x21;
//        TxBux[2]=(byte)len;
//        System.arraycopy(bux,0,TxBux,3,len);
//        TxBux[63]=CRC_OR(TxBux);
        int n=connection.bulkTransfer(endpoint[0][1], TxBux, 64, 500);
        String s="USBT:---";
//        for(int i=0;i<64;i++)
//        {
//            s=s+String.format("%02X ",TxBux[i]);
//        }
        Log.v("ABC",s);
//
//        threadsenddata=true;
//        iComm=5;
        //白灯
//        byte[]buf={(byte)0x21,(byte)0x00,(byte)0x12,(byte)0xAA,(byte)0x00,(byte)0x0F,(byte)0x20,(byte)0x00,
//                (byte)0x0C,(byte)0x6B,(byte)0x68,(byte)0x6C,(byte)0x74,(byte)0x20,(byte)0x00,(byte)0x00,
//                (byte)0x02,(byte)0x00,(byte)0x03,(byte)0xF2,(byte)0x57,(byte)0x00,(byte)0x00,(byte)0x00,
//                (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
//                (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
//                (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
//                (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
//                (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x25};//25
//        connection.bulkTransfer(endpoint[0][1], buf, 64, 1000);
        return n;

    }
    private void  TxDataToUSBDev(byte[]Bux,int len)
    {
        //  if(len<=0) return;
        // if(len>64) return ;
        Tx_USB(Bux,len);
    }
    class LocalClient_Thread_RX extends Thread
    {


        public void run(){
            int kk=0;
            while(true)
            {
                if (ThreadRun)
                {
                    try {
                        InputStream ops = Localreceiver.getInputStream();
                        DataInputStream dos = new DataInputStream(ops);
                        kk = dos.available();
                        if (kk <= 0) continue;
                        byte[] buffer = new byte[kk];
                        dos.read(buffer);
                        if (kk > 0)
                        {

//                            String s="";
//                            for(int i=0;i<kk;i++)
//                            {
//                                s=s+String.format("%02X ",buffer[i]);
//                            }
//                            Log.d("ABC", "JAVA localsocket RX"+kk+","+s);
                            Log.d("ABC", "JAVA RX localsocket");
                            TxDataToUSBDev(buffer,kk);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        ThreadRun = false;
                    }
                }//if

            }//while

        }//run

    }
    public void TransmitTCPTXData(byte[]buff)
    {
        if(buff==null)return;
        if(Localreceiver==null) return;
        //  if(Localreceiver.getOutputStream()==null) return;
        //   Log.v("ABC","TransmitTCPTXData buff.len="+buff.length);
        try
        {

            OutputStream ops=Localreceiver.getOutputStream();
            if(ops==null) return;
            DataOutputStream dos=new DataOutputStream(ops);
            dos.write(buff);
            // Log.v("ABC","TransmitTCPTXData OK");

        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
    public class USBLocalServerSocket extends Thread {


        int bufferSize = 2048;
        byte[] buffer;
        int bytesRead;
        int totalBytesRead;
        int posOffset;
        LocalServerSocket server;

        InputStream input;
        private volatile boolean stopThread;

        public USBLocalServerSocket() {
            Log.d("ABC", " +++ Begin of localServerSocket() +++ ");
            buffer = new byte[bufferSize];
            bytesRead = 0;
            totalBytesRead = 0;
            posOffset = 0;

            try {
                server = new LocalServerSocket(SOCKET_ADDRESS);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.d("ABC", "The LocalServerSocket created failed !!!");
                e.printStackTrace();
            }

            stopThread = false;
        }

        public void run() {


            try {
                Localreceiver = server.accept();
                ThreadRun=true;
                //  LocalInputStream=receiver.getInputStream();
                Log.d("ABC", "The LocalServerSocket is OK !!!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void setStopThread(boolean value){
            stopThread = value;
            Thread.currentThread().interrupt(); // TODO : Check
        }
    }


    Bitmap tempBitmap;
    private Bitmap picBitmap;
    long regtimebefore;
    public class EIDXXXThread extends Thread {

        public void run() {

            int ret;
            while(true)
            {

                Log.v("ABC","EIDXXX--11");
                if(OCRMyApplication.Read_OCR_IC_ThreadEnable)
                {

                    try {
                        sleep(10);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    continue;//如果正在读 芯片,就不做OCR 分析
                }
                Log.v("ABC","EIDXXX--122");
                if(OCRMyApplication.srcBitmap==null)
                {
                    try {
                        sleep(10);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    continue;
                }

                Log.v("ABC_Reg","EIDXXXThread--");


                try {
                    tempBitmap=OCRMyApplication.srcBitmap.copy(Bitmap.Config.ARGB_8888,true);
                    regtimebefore=System.currentTimeMillis();
                    int iQre=0;
                    byte[]    bux= RegBmpOCRData(tempBitmap);
                    if (bux != null)
                    {
                        String s = new String(bux);
                        if (s.length() > 5)
                        {

                            if (bux.length >= 206)
                            {
                                //iQre=1;
                                Log.v("ABC_Reg","EIDXXXThread--Reg");
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
                                LEDWhiteOn();
                                OCRMyApplication.ReadOCREnable=true;

                                try {
                                    sleep(150);
                                } catch (InterruptedException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                tempBitmap=OCRMyApplication.srcBitmap.copy(Bitmap.Config.ARGB_8888,true);

                                Bitmap bitmapdes= Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888);
                                ret=GetFaceBitmap(tempBitmap,bitmapdes);

                                if(ret==0) {

                                    Log.v("ABC_Reg","EIDXXXThread--face");

                                    picBitmap=bitmapdes.copy(Bitmap.Config.ARGB_8888,true);

                                }

                                LEDRedOn();

                                iQre=1;
                                try {
                                    sleep(10);
                                } catch (InterruptedException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }

                                //ztCardReaderLib.LEDRedOn();
                                //                            for (int i = 0; i < 8; i++)
                                //                                OCRMyApplication.RegAllData[i] = sInfo[i];
                                //                            GetFaceEnable = true;
                            }
                        }


                        if(iQre==0)
                        {
                            //没有识别到就测试QRE

                            LEDWhiteOn();
                            Log.v("ABC","LED ON A");

                            try {


                                for(int mm=0;mm<10;mm++)
                                {
                                    sleep(30);
                                }

                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                            tempBitmap=OCRMyApplication.srcBitmap.copy(Bitmap.Config.ARGB_8888,true);
                            Log.v("ABC","LED ON B");
                            Bitmap qreBitmapA= Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888);
                            Bitmap qreBitmapB= Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888);
                            Bitmap qreBitmapC= Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888);
                            Bitmap qreBitmapD= Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888);
                            ret=QreReg(tempBitmap,qreBitmapA,qreBitmapB,qreBitmapC,qreBitmapD);
                            int getQre=0;

                            Log.v("ABC_Reg","ztCardReaderLib.QreReg ret="+ret);
                            if(ret==1)
                            {
                                CodeResult resultA= BarcodeReader.getInstance().read(qreBitmapA);
                                if (resultA == null) {
                                    Log.d("ABC_Reg", "no code");

                                } else {
                                    Log.d("ABC", "A-"+resultA.getText());
                                    OCRMyApplication.RegAllData[10]=resultA.getText();
                                    //GetDispQRE(resultA.getText());
                                }
                            }
                            else if(ret==2)
                            {
                                CodeResult resultA = BarcodeReader.getInstance().read(qreBitmapA);
                                if (resultA == null) {
                                    Log.d("ABC_Reg", "no code");

                                } else {
                                    Log.d("ABC_Reg", "A-"+resultA.getText());
                                    getQre=1;
                                    //GetDispQRE(resultA.getText());
                                }
                                if(getQre==0)
                                {
                                    CodeResult resultB = BarcodeReader.getInstance().read(qreBitmapB);
                                    if (resultB == null) {
                                        Log.d("ABC_Reg", "no code");

                                    } else {
                                        Log.d("ABC_Reg", "B-"+resultB.getText());
                                       // GetDispQRE(resultA.getText());
                                    }
                                }

                            }
                            else if(ret==3)
                            {
                                CodeResult resultA = BarcodeReader.getInstance().read(qreBitmapA);
                                if (resultA == null) {
                                    Log.d("ABC_Reg", "no code");

                                } else {
                                    Log.d("ABC_Reg", "A-"+resultA.getText());
                                    //GetDispQRE(resultA.getText());

                                    getQre=1;
                                }
                                if(getQre==0)
                                {
                                    CodeResult resultB = BarcodeReader.getInstance().read(qreBitmapB);
                                    if (resultB == null) {
                                        Log.d("ABC_Reg", "no code");

                                    } else {
                                        Log.d("ABC_Reg", "B-"+resultB.getText());
                                        //GetDispQRE(resultA.getText());
                                    }
                                }

                                if(getQre==0)
                                {
                                    CodeResult resultC = BarcodeReader.getInstance().read(qreBitmapB);
                                    if (resultC == null) {
                                        Log.d("ABC_Reg", "no code");

                                    } else {
                                        Log.d("ABC_Reg", "C-"+resultC.getText());
                                        //GetDispQRE(resultA.getText());
                                    }
                                }

                            }
                            else if(ret==4)
                            {
                                CodeResult resultA = BarcodeReader.getInstance().read(qreBitmapA);
                                if (resultA == null) {
                                    Log.d("ABC_Reg", "no code");

                                } else {
                                    Log.d("ABC_Reg", "A-"+resultA.getText());
                                   // GetDispQRE(resultA.getText());
                                    getQre=1;
                                }
                                if(getQre==0)
                                {
                                    CodeResult resultB = BarcodeReader.getInstance().read(qreBitmapB);
                                    if (resultB == null) {
                                        Log.d("ABC_Reg", "no code");

                                    } else {
                                        Log.d("ABC", "B-"+resultB.getText());
                                       // GetDispQRE(resultA.getText());
                                    }
                                }

                                if(getQre==0)
                                {
                                    CodeResult resultC = BarcodeReader.getInstance().read(qreBitmapB);
                                    if (resultC == null) {
                                        Log.d("ABC_Reg", "no code");

                                    } else {
                                        Log.d("ABC_Reg", "C-"+resultC.getText());
                                        //GetDispQRE(resultA.getText());
                                    }
                                }

                                if(getQre==0)
                                {
                                    CodeResult resultD = BarcodeReader.getInstance().read(qreBitmapB);
                                    if (resultD == null) {
                                        Log.d("ABC_Reg", "no code");

                                    } else {
                                        Log.d("ABC_Reg", "D-"+resultD.getText());
                                       // GetDispQRE(resultA.getText());
                                    }
                                }

                            }
                            LEDRedOn();

                            //                    try {
                            //                        sleep(50);
                            //                    } catch (InterruptedException e) {
                            //                        // TODO Auto-generated catch block
                            //                        e.printStackTrace();
                            //                    }

                        }
                    }
                    ret=TestFindCard();
                    if(ret==0) {
                        //读卡
                        Log.v("ABC_Reg", "寻卡成功 ");

                        RegTimems=System.currentTimeMillis();

                        byte[] RxBuff = TestReadIDWithPhoto();
                        RegTimems = System.currentTimeMillis() - RegTimems;
                        OCRMyApplication.RegAllData[6]=RegTimems+" ms";
                        if (RxBuff != null) {
                            int rxlen = RxBuff.length;
                            bmpsrc = null;
                            if (rxlen > 20) {

                                String sName, sNationA, sBirth, sAdr, sNumber, sNationB, sBeginTime, sEndTime, sSex;

                                int n = 0;
                                sName = GetStringUTF16(RxBuff, 0, 15 * 2);
                                s_Name = sName;
                                OCRMyApplication.RegAllData[3]=sName;

                                n = 15 * 2;
                                sSex = GetStringUTF16(RxBuff, n, 1 * 2);
                                if (sSex.contains("1")) sSex = "男";
                                s_Sex = sSex;
                                n = n + 2;
                                OCRMyApplication.RegAllData[7]=s_Sex;

                                sNationA = GetStringUTF16(RxBuff, n, 2 * 2);
                                n = n + 4;


                                sBirth = GetStringUTF16(RxBuff, n, 8 * 2);
                                s_birth = sBirth;
                                OCRMyApplication.RegAllData[5]=s_birth+"";
                                n = n + 16;
                                sAdr = GetStringUTF16(RxBuff, n, 70);
                                n = n + 70;
                                sNumber = GetStringUTF16(RxBuff, n, 36);
                                s_code = sNumber;

                                OCRMyApplication.RegAllData[1]=s_code+"";


                                n = n + 36;

                                sNationB = GetStringUTF16(RxBuff, n, 30);
                                n = n + 30;

                                sBeginTime = GetStringUTF16(RxBuff, n, 16);
                                n = n + 16;

                                sEndTime = GetStringUTF16(RxBuff, n, 16);
                                s_day = sEndTime;
                                OCRMyApplication.RegAllData[8]=s_day+"";
                                n = n + 16;

                                sName = sName.trim() + "," + sSex.trim() + "," + sNationA.trim() + "," + sBirth.trim() + "\r\n" + sAdr.trim() + "\r\n" + sNumber.trim() + "," + sNationB.trim() + "\r\n" + sBeginTime.trim() + "," + sEndTime.trim();
                                Log.v("ABC_Reg", sName);
                                sMRZAllString = sName;

                                OCRMyApplication.RegAllData[0]="二代证";

                                if (rxlen < 1280) return;//1281,0
                                byte[] picbuf = new byte[1024];
                                System.arraycopy(RxBuff, 256, picbuf, 0, 1024);

                                Log.v("ABC_Reg", "ZpData=0-------------------");
                                byte[] ZpData = new byte[0];
                                try {
                                    ZpData = invswlt.Wlt2Bmp(picbuf);
                                } catch (Exception e) {
                                    Log.v("ABC_Reg", "invswlt ---ERROR ");
                                    // e.printStackTrace();
                                }
                                if (ZpData != null) {
                                    Log.v("ABC_Reg", "ZpData=" + ZpData.length);
                                } else {
                                    Log.v("ABC_Reg", "ZpData=null");
                                }

                                OCRMyApplication.ReadOCREnable=true;
                                try {
                                    bmpsrc = BitmapFactory.decodeByteArray(ZpData, 0, 38862);
                                    OCRMyApplication.RegAllData[9]=convertIconToString(bmpsrc);
                                } catch (Exception e) {
                                    Log.v("ABC_Reg", "BitmapFactory ---ERROR ");
                                    //e.printStackTrace();
                                }
                                if (bmpsrc != null) {
                                    Log.v("ABC_Reg", "bmpsrc OK ");
                                } else {
                                    Log.v("ABC_Reg", "bmpsrc null ");
                                }
                               // RegString[7] = RegTimems + " ms";
                            }

//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//
//                                    if (bmpsrc != null)
//                                        img.setImageBitmap(bmpsrc);
//
//                                    tvIDInfor.setText(sMRZAllString);
//                                    tvregtime.setText("识读时间:" + RegString[7].trim());
//
//                                    tvType.setText("证件类型: 二代证");
//                                    tvCode.setText("证件号码:" + s_code);
//
//                                    tvNameEng.setText("证件姓名:" + s_Name);
//                                    tvBirth.setText("出生年月:" + s_birth);
//
//                                    tvSex.setText("持证性别:" + s_Sex);
//                                    tvGuoJi.setText("证件国籍:--");
//                                    tvValidiyDate.setText("有限期限:" + s_day);
//                                    ztCardReaderLib.BeenOn(50);
//                                    //WriteTXT(sMRZAllString+",耗时:"+RegString[7].trim()+"ms");
//
//                                }
//                            });
                        }

                    }
                }

                catch (Exception e)
                {
                    Log.v("ABC","EIDXXXThread---ERROR");
                }

            }

        }

    }

    private String convertIconToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();// outputstream
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] appicon = baos.toByteArray();// 转为byte数组
        String img = Base64.encodeToString(appicon, Base64.DEFAULT);
        return img;
    }














    //-----------------------
    static {
        System.loadLibrary("native-lib");
    }

    public native int SOInit();
    public native int SOII();
    public native byte[]RegBmp(Bitmap bitmap);
    public native byte []RegOcrData(String sfile,int iType);
    public native byte []RegBmpOCRData(Bitmap bitmap);
    public native int TestAA(Bitmap bmp);
    public native int GetFaceBitmap(Bitmap bmp,Bitmap desbmp);

    public native int BeenOn(int Delay);

    public native int TestFindCard();
    public native byte[]TestReadIDCard();
    public native byte[]TestGetVersion();
    public native int TestFindStuCard();
    public native byte[]TestGetStuCardInfo();
    public native int TestFindSoldierCard();
    public native byte[]TestGetSoldierCardInfo();
    public native byte[]TestFindCpuCard();
    public native byte[]TestResetCpuCard();
    public native byte[]TestCpuApdu();
    public native byte[]TestResetPSAM(int slot);
    public native byte[]TestPSAMApdu(String sAPUDU,int len);
    public native int TestABC(String s);
    public native int LEDRedOn();
    public native int LEDWhiteOn();
    public native byte[]TestReadPassInfo(String sOcr,int iMode);
    public native byte[]TestOcrString(String sNum,String sBirth,String sdatetime);
    public native byte[] TestReadIDWithPhoto();
    public native byte[] TestFindIDAndReadIDWithPhoto();
    public native int TestLED(int iType,int iOnOff );
    public native int QreReg(Bitmap srcBmp,Bitmap desBmpA,Bitmap desBmpB,Bitmap desBmpC,Bitmap desBmpD);
    public native int GetBackFlash(Bitmap srcBmp);
}
