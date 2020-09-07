package com.littt.androidztdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.ZTCardReaderLib;

public class MainActivity extends AppCompatActivity {
    private static final String[] REQUESTED_PERMISSIONS = {Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int PERMISSION_REQ_ID = 22;
ZTCardReaderLib ztCardReaderLib=new ZTCardReaderLib();
Button btndevInit,BtnBeep;

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


        btndevInit=findViewById(R.id.btnInit);
        btndevInit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ztCardReaderLib.InitAllDev();
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
