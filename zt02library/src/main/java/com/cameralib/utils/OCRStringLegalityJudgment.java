package com.cameralib.utils;


import android.util.Log;



import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by littt0 on 2018/6/24.
 */

public class OCRStringLegalityJudgment {




    private boolean IsNotFirst_E(String s)//瀵绘眰涓嶅尮閰�
    {
        boolean b=false;



        b=  IsNotLetter(s);


        // if(s.indexOf("E")!=0) b=true;//绗竴涓瓧绗︿笉鏄�E





        return b;
    }
    private  boolean IsNotDigital(String s)//鍒ゆ柇  s 鏄惁鏄暟瀛�
    {
        boolean b=false;
        Pattern p = Pattern.compile("[0-9]*");
        Matcher m = p.matcher(s);
        if(!m.matches()) b=true;
        return b;
    }
    private boolean IsNotLetter(String s)//鍒ゆ柇  s 鏄惁鏄暟瀛�
    {
        boolean b=false;
        Pattern p = Pattern.compile("[A-Z]*");
        Matcher m = p.matcher(s);
        if(!m.matches()) b=true;
        return b;
    }
    private  int SecondString(String s)
    {

        int n=0;
        int len=s.length();
        boolean b=false;
        if(len!=9)return 9;//涓嶅尮閰嶆槸 9 锛屽垯鎺ユ敹鍒板悗鐩存帴涓㈡帀

        for(int i=0;i<9;i++)
        {
            b=IsNotDigital(s.substring(i,i+1));
            if(b)n++;
            if(n>4) break;
        }
        return n;
    }
    private   int ThirdString(String s)
    {
        int n=0;
        boolean b=false;
        for(int i=0;i<3;i++)
        {
            b=IsNotLetter(s.substring(i,i+1));
            if(b)n++;
        }
        return n;
    }
    private  int FourString(String s)
    {
        int n=0;
        int len=s.length();
        boolean b=false;
        for(int i=0;i<6;i++)
        {
            b=IsNotDigital(s.substring(i,i+1));
            if(b)n++;
            if(n>4) break;
        }
        return n;
    }
    private  int FiveString(String s)
    {
        int n=0;
        boolean b=false;
        b=IsNotDigital(s);
        if(b)n++;
        return n;
    }
    private  int SixString(String s)
    {
        int n=0;
        boolean b=false;
        b=IsNotLetter(s);
        if(b)n++;
        return n;
    }
    private  int SeverString(String s)
    {
        int n=0;
        boolean b=false;
        for(int i=0;i<6;i++)
        {
            b=IsNotDigital(s.substring(i,i+1));
            if(b)n++;
            if(n>4) break;
        }
        return n;
    }
    public  int IsNotMatches(String s)
    {
        //E079857O06CHN7703241F240388......
        // .
        int n=0;
        int m=0;
        boolean b=false;
        if(s.length()<27) return  20;//len < 20 jiu value
        String s1=s.substring(n,1);//E
        b=IsNotFirst_E(s1);
        if(b)m++;
        n+=1;

        s1=s.substring(n,n+9);//079857006
        m=m+SecondString(s1);
        if(m>4) return m;//涓嶅尮閰嶅ぇ浜�4 灏变涪鎺夎繖涓瓧绗︿覆
        n+=9;

        s1=s.substring(n,n+3);
        m=m+ThirdString(s1);//CHN
        if(m>4)return m;
        n+=3;

        s1=s.substring(n,n+6);//770324
        m=m+FourString(s1);
        if(m>4)return m;
        n+=6;

        s1=s.substring(n,n+1);// 1
        m=m+FiveString(s1);
        if(m>4)return m;
        n+=1;

        s1=s.substring(n,n+1);// F,M
        m=m+SixString(s1);
        if(m>4)return m;
        n+=1;
        s1=s.substring(n,n+6);// 240388
        m=m+SeverString(s1);
        if(m>4)return m;
        //n+=1;
        return m;
    }

    private  byte Passport_checkSumSubString(byte[]src,int len)
    {
        int i,chk=0;
        byte wmap[]={10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35};
        byte xmap[]={0,1,2,3,4,5,6,7,8,9};
        byte cmap[]={7,3,1};
        int cmapindex = 0;
        for(i=0;i<len;i++)
        {
            if((src[i] >= 'A')&&(src[i] <= 'Z'))
            {
                chk += wmap[src[i] - 'A']*cmap[cmapindex++];
            }
            else if((src[i] >= '0')&&(src[i] <= '9'))
            {
                chk += xmap[src[i] - '0']*cmap[cmapindex++];
            }
            else
            {
                continue;
            }
            cmapindex = cmapindex%3;
        }

        return (byte)(chk%10 + '0');
    }
    public boolean PassPortCheckCRC(String s) //护照校验
    {
        int len=s.length();
        if(len<28)return false;
        Log.d("PASSA","R-Begin-"+s);
        s=s.substring(0,28);
        byte Buff[]=s.getBytes();
        //---第一串校验  护照号
        //E006621742CHN0811220H1706192MNPEHPKGHILLA076

        //E006621742
        byte Bu[]=new byte[20];
        System.arraycopy(Buff,0,Bu,0,12);
        byte bCrc=  Passport_checkSumSubString(Bu,9);  // 0--8  共 9个字节校验 护照号码
        if(bCrc!=Bu[9])return  false;
        //0811220
        System.arraycopy(Buff,13,Bu,0,7);  //生日
        bCrc=  Passport_checkSumSubString(Bu,6);
        if(bCrc!=Bu[6])return  false;
        //1706192
        System.arraycopy(Buff,21,Bu,0,7);
        bCrc=  Passport_checkSumSubString(Bu,6);   //过期日期
        if(bCrc!=Bu[6])return  false;
        Log.d("PASSA","R-end");
//        OCRMyApplication.mVibrator.vibrate(1000);
        return true;
    }



    public  boolean GangaoPassCRC(String s)//港澳通行证
    {


        int len=s.length();
        if(len<27)return false;

        // C62490207
        byte Buff[]=s.getBytes();
        byte Bu[]=new byte[30];
        int n=2;
        System.arraycopy(Buff,n,Bu,0,10);
        byte bCrc=  Passport_checkSumSubString(Bu,9);
        if(bCrc!=Bu[9])return  false;

        n+=10;

        System.arraycopy(Buff,n,Bu,0,7);//2208028
        bCrc=  Passport_checkSumSubString(Bu,6);
        if(bCrc!=Bu[6])return  false;
        n+=7;
        System.arraycopy(Buff,n,Bu,0,7);//0712040
        bCrc=  Passport_checkSumSubString(Bu,6);
        if(bCrc!=Bu[6])return  false;


        n=2;
        System.arraycopy(Buff,n,Bu,0,25);
        bCrc=  Passport_checkSumSubString(Bu,24);
        if(bCrc!=Bu[24])return  false;
        return true;
    }
    public   boolean Cell_syndromeCRC(String s) //台胞证、回乡证 校验
    {
        byte Buff[]=s.getBytes();
        byte Bu[]=new byte[30];
        int n=1;
        System.arraycopy(Buff,2,Bu,0,9);
        byte bCrc=  Passport_checkSumSubString(Bu,8);
        if(bCrc!=Bu[8])return  false;
        n=11;
        System.arraycopy(Buff,n,Bu,0,3);
        bCrc=  Passport_checkSumSubString(Bu,2);
        if(bCrc!=Bu[2])return  false;
        n=14;
        System.arraycopy(Buff,n,Bu,0,7);
        bCrc=  Passport_checkSumSubString(Bu,6);
        if(bCrc!=Bu[6])return  false;
        return true;
    }
    public String Passport_wordbyword(String s) //护照 字母处理前期
    {
        int n=s.length();
        if(n<28)return s;
        String s1=s.substring(0,10);//取护照号码
        String s2=s.substring(10,13);//国籍
        String s3=s.substring(13,20);//取 生日
        String s4=s.substring(20, 21);//性别
        String s5=s.substring(21,n-1);//到期日期
        s1=StrAllReplace(s1);
        s2=StrAllReplace(s2);
        s3=StrAllReplace(s3);
        s5=StrAllReplace(s5);

//        DisStr(s1); //E079857006
//        DisStr(s2); //CHN
//        DisStr(s3); //7703241
//        DisStr(s4);	//F
//        DisStr(s5);  //
        return s1+s2+s3+s4+s5;
    }
    private  String StrAllReplace(String s)  //这个函数只能纠正数字
    {
        s=s.replaceAll("k", "4");
        s=s.replaceAll("o", "0");
        s=s.replaceAll("O", "0");
        s=s.replaceAll("B", "8");
        s=s.replaceAll("D", "0");
        s=s.replaceAll("U", "0");
        s=s.replaceAll("s", "5");
        s=s.replaceAll("S", "5");
        s=s.replaceAll("Z", "2");
        s=s.replaceAll("z", "2");
        s=s.replaceAll("G", "0");
        return s;
    }
    private  String StrKReplace(String s)
    {
        s=s.replaceAll("k", "4");
        s=s.replaceAll("o", "0");
        return s;
    }
    public String Cell_syndrome_wordbyword(String s) //台胞证、回乡证的 字母处理前期
    {
        //CT08248515<50262101060M7201105
        s=s.replaceAll(" ","");//去除空格
        s=s.replaceAll("<","");//去除
        int n=s.length();
        s=s.substring(2,n);
        s=StrAllReplace(s);
        s=s.replaceAll("M","");
        s=s.replaceAll("F","");
        return "CT"+s;
    }
    public String GangaoPass_wordbyword(String s) //港澳通行证 字母处理前期
    {
        //CSC624902070 2208028 0712040 6
        s=s.replaceAll(" ","");//去除空格
        s=s.replaceAll("<","");//去除 <
        s=s.replaceAll("k","4");//由于 护照全部是 大写，所有 有  k 就不正确，直接替换为 4
        int n=s.length();
        //根据 2014版本 港澳通行证  CSC 是固定字符
        String s1=s.substring(3,12);//取通行证号码 8 个数字

        String s2=s.substring(12,19);//证件有效期
        String s3=s.substring(19,n);//生日
//        DisStr(s1); //624902070
//        DisStr(s2); //2208028
//        DisStr(s3); //0712040
        s1=StrAllReplace(s1);
        s2=StrAllReplace(s2);
        s3=StrAllReplace(s3);
        return "CSC"+s1+s2+s3;

    }
    private String digNumber(String s)
    {
        String s1=s;
        s1=s1.replaceAll("O","0");
        s1=s1.replaceAll("S","5");
        s1=s1.replaceAll("D","0");
        s1=s1.replaceAll("U","0");
        s1=s1.replaceAll("é","6");
        s1=s1.replaceAll("Z","2");
        return s1;
    }
    public String PassPortDigStr(String s)//护照数字处理
    {
        String s1,s2,s3,s4,s5,s6,s7;
        int len=s.length();
        if(len<28) return s;
        int n=0;
        s1=s.substring(n,1);
        n+=1;
        s2=s.substring(n,n+9);
        n+=9;
        s3=s.substring(n,n+3);
        n+=3;
        s4=s.substring(n,n+7);
        n+=7;
        s5=s.substring(n,n+1);
        n+=1;
        s6=s.substring(n,n+7);
        n+=7;
        s7=s.substring(n,len);

        s2=digNumber(s2);
        s4=digNumber(s4);
        s6=digNumber(s6);

        s1=s1+s2+s3+s4+s5+s6+s7;
        return s1;

    }
    public String HuiXiangDigStr(String s)//回乡证字处理
    {
        String s1,s2;
        int len=s.length();
        if(len<26) return s;
        s1=s.substring(0,4);
        s2=s.substring(4);
        s2=digNumber(s2);
        s1=s1+s2;
        return s1;

    }
    public String TaiWanDigStr(String s)//回乡证字处理
    {
        String s1,s2,s3,s4;

        int len=s.length();
        if(len<26) return s;
        int n=0;
        s1=s.substring(n,2);
        n+=2;
        s2=s.substring(n,n+19);
        n+=19;
        s3=s.substring(n,n+1);
        n+=1;
        s4=s.substring(n);
        s2=digNumber(s2);
        s4=digNumber(s4);
        s1=s1+s2+s3+s4;
        return s1;

    }
    public String GangaoPassStr(String s)//回乡证字处理
    {
        String s1;

        int len=s.length();
        if(len<26) return s;
        s1=s.replaceAll("<","");

        return s1;

    }
    public String GangaoStr(String s)//港澳通行证 字符串处理
    {
        String s1,s2,s3,s4;

        int len=s.length();
        if(len<26) return s;
        int n=0;
        s1=s.substring(n,2);
        n+=2;
        s2=s.substring(n,n+19);
        n+=19;
        s3=s.substring(n,n+1);
        n+=1;
        s4=s.substring(n);
        s2=digNumber(s2);
        s4=digNumber(s4);
        s1=s1+s2+s3+s4;
        return s1;

    }
}
