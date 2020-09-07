package com.littt.androidztdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ZTCardReaderLib;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private static final String[] REQUESTED_PERMISSIONS = {Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int PERMISSION_REQ_ID = 22;
    ImageView imageView;
ZTCardReaderLib ztCardReaderLib=new ZTCardReaderLib();
Button btndevInit,BtnBeep;
TextView tvType,tvNum,tvCountry,tvChnname,tvName,tvBirth,tvNation,tvSex,tvValidDate,tvQrcode;
Timer timer=new Timer();
private String adjust(String s){

    String ss=" ";

   if(s!=null)
   {
       s=s.replaceAll("null"," ");
       //Log.v("ABC","s="+s);
       return s;
   }

   // Log.v("ABC","ss="+ss);
   return ss;
}
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what==1){
                String info = msg.obj.toString();
                try {
                    JSONObject jsonObject=new JSONObject(info);

                    String spic = (String) jsonObject.get("PICData");

                    String type = (String) jsonObject.get("Type");
                    String number = jsonObject.getString("Number");
                    number=adjust(number);



                    String signCountry = jsonObject.getString("SignCountry");
                    signCountry=adjust(signCountry);

                    String chnName = (String) jsonObject.get("CHNName");
                    chnName=adjust(chnName);
                    String tvname = jsonObject.getString("Name");
                    tvname=adjust(tvname);
                    String birth = jsonObject.getString("Birth");
                    birth=adjust(birth);
                    String nation = jsonObject.getString("Nation");
                    nation=adjust(nation);
                    String sex = jsonObject.getString("Sex");
                    sex=adjust(sex);
                    String validDate = jsonObject.getString("ValidDate");
                    validDate=adjust(validDate);
                    String qreData = jsonObject.getString("QREData");
                    qreData=adjust(qreData);
                    tvType.setText("证件类型:"+type);
                    tvNum.setText("证件号码:"+number);
                    tvCountry.setText("证件国籍:"+signCountry);
                    tvChnname.setText("中文姓名:"+chnName);
                    tvName.setText("证件姓名:"+tvname);
                    tvBirth.setText("出生年月:"+birth);
                    tvNation.setText("识读时间:"+nation);
                    tvSex.setText("持证性别:"+sex);
                    tvValidDate.setText("有效期限:"+validDate);
                    tvQrcode.setText("二维码:"+qreData);
                    Bitmap bitmap = convertStringToIcon(spic);
                    imageView.setImageBitmap(bitmap);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            super.handleMessage(msg);
        }
    };

    private Bitmap convertStringToIcon(String str) {
        // OutputStream out;
        Bitmap bitmap = null;
        try {
            // out = new FileOutputStream("/sdcard/aa.jpg");
            byte[] bitmapArray;
            bitmapArray = Base64.decode(str, Base64.DEFAULT);
            bitmap =
                    BitmapFactory.decodeByteArray(bitmapArray, 0,
                            bitmapArray.length);
            // bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) ||
                !checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID) ||
                !checkSelfPermission(REQUESTED_PERMISSIONS[2], PERMISSION_REQ_ID)) {
            //Toast.makeText(this, "请获取权限", Toast.LENGTH_LONG).show();
        }

        quxian();

        tvType=findViewById(R.id.tvtype);
        tvNum=findViewById(R.id.tvcode);
        tvChnname=findViewById(R.id.tvnamechn);
        tvCountry=findViewById(R.id.tvguoji);
        tvName=findViewById(R.id.tvname);
        tvBirth=findViewById(R.id.tvbirthday);
        tvSex=findViewById(R.id.tvsex);
        tvValidDate=findViewById(R.id.tvvalidate);
        tvQrcode=findViewById(R.id.tvqrcode);
        imageView=findViewById(R.id.img_header);
        tvNation=findViewById(R.id.tvregtime);
        btndevInit=findViewById(R.id.btnInit);
        btndevInit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int result = ztCardReaderLib.InitAllDev();
                if (result==0){
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            String info = ztCardReaderLib.ReadCardData(2);
                            if (!"".equals(info)){
                                Message message=new Message();
                                message.what=1;
                                message.obj=info;
                                handler.sendMessage(message);
                            }
                        }
                    },0,500);
                    
                }
            }
        });

        BtnBeep=findViewById(R.id.btnbeep);
        BtnBeep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ztCardReaderLib.BeenOn(50);
            }
        });

    }
    

    public boolean checkSelfPermission(String permission, int requestCode) {
       // Log.i("LOG_TAG", "checkSelfPermission " + permission + " " + ContextCompat.checkSelfPermission(this, permission));

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }
        return true;
    }
    public  void quxian()
    {
        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTACT = 101;
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            //验证是否许可权限
            for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                    return;
                }
            }
        }
    }

}
