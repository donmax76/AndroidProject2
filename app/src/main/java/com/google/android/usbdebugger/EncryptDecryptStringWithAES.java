package com.google.android.usbdebugger;

import android.os.Build;
import android.support.annotation.RequiresApi;

import com.sun.mail.util.BASE64DecoderStream;
import com.sun.mail.util.BASE64EncoderStream;

import java.nio.charset.StandardCharsets;

public class EncryptDecryptStringWithAES {

    //public static String strKey = "Bar12345Bar12345";
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String encrypt(String strClearText) {
        String strData="";

        try {
            //SecretKeySpec skeyspec=new SecretKeySpec(strKey.getBytes(), "AES");
            //Cipher cipher=Cipher.getInstance("AES");
            //cipher.init(Cipher.ENCRYPT_MODE, skeyspec);

            byte[] utf8 = strClearText.getBytes(StandardCharsets.UTF_8);

            byte[] enc = utf8;//cipher.doFinal(utf8);

            enc = BASE64EncoderStream.encode(enc);
            strData=new String(enc);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return strData;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String decrypt(String strEncrypted){
        String strData="";

        try {
            //SecretKeySpec skeyspec=new SecretKeySpec(strKey.getBytes(),"AES");
            //ipher cipher=Cipher.getInstance("AES");//Blowfish

            //cipher.init(Cipher.DECRYPT_MODE, skeyspec);
            byte[] dec = BASE64DecoderStream.decode(strEncrypted.getBytes());

            byte[] utf8 = dec;//cipher.doFinal(dec);
            strData=new String(utf8, StandardCharsets.UTF_8);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return strData;
    }
}
