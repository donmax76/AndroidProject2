package com.google.android.usbdebugger;

import android.util.Base64;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptDecryptStringWithAES {

    private static final String TAG = "AESCrypto";

    // ВАЖНО: Ключ должен быть определенной длины (16, 24 или 32 байта для AES)
    private static final String SECRET_KEY_STRING = "this-is-a-super-secret-key-32"; // 32 символа = 256 бит

    private static SecretKeySpec secretKey;
    private static IvParameterSpec ivParameterSpec;

    // Статический блок для инициализации ключа и вектора один раз
    static {
        try {
            // Создаем ключ из строки
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] key = sha.digest(SECRET_KEY_STRING.getBytes(StandardCharsets.UTF_8));
            key = Arrays.copyOf(key, 32); // Убедимся, что ключ ровно 32 байта (AES-256)
            secretKey = new SecretKeySpec(key, "AES");

            // Создаем вектор инициализации (IV). В реальном приложении его лучше генерировать
            // случайным образом, но для простоты и стабильности используем статический.
            // IV должен быть 16 байт.
            byte[] iv = new byte[16];
            // Здесь можно заполнить iv какими-то статичными байтами, например:
            // System.arraycopy("my-static-iv-16".getBytes(), 0, iv, 0, 16);
            ivParameterSpec = new IvParameterSpec(iv);

        } catch (Exception e) {
            Log.e(TAG, "Ошибка инициализации ключа шифрования", e);
        }
    }

    public static String encrypt(String strToEncrypt) {
        if (strToEncrypt == null || strToEncrypt.isEmpty()) return null;
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);

            byte[] encryptedBytes = cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP);

        } catch (Exception e) {
            Log.e(TAG, "Ошибка при шифровании: ", e);
        }
        return null;
    }

    public static String decrypt(String strToDecrypt) {
        if (strToDecrypt == null || strToDecrypt.isEmpty()) return null;
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);

            byte[] decryptedBytes = cipher.doFinal(Base64.decode(strToDecrypt, Base64.DEFAULT));
            String decryptedString = new String(decryptedBytes, StandardCharsets.UTF_8);
            return decryptedString;//new String(decryptedBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            // Эта ошибка больше не должна появляться
            Log.e(TAG, "Ошибка при дешифровании: ", e);
        }
        return null;
    }
}
