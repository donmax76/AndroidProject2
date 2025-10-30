package com.google.android.usbdebugger;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ReadXMLFile {
    private static final String TAG = "ReadXMLFile";
    private static final String [] column = {"VC", "SC","FT","PT","FD","US","PS",
            "GP","GP_T","CN","MS","DC","PR",
            "BS","SM","MI","FLC","RS", "DBC", "SH", "XML_T", "DUR"};



    public static ArrayList<String> main(final String address, final String imei1, Context context, final ArrayList<String> localParams) {
        ArrayList<String> aa = new ArrayList<>();
        OkHttpClient client = new OkHttpClient();
        String urlString = "http://" + address + "/testxml.php";
        Log.d(TAG, "Requesting params from: " + urlString + " using POST");

        try {
            RequestBody body = new FormBody.Builder()
                    .add("IMEI", imei1)
                    .build();

            Request request = new Request.Builder()
                    .url(urlString)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Server error: " + response.code());
                    return aa;
                }

                String responseString = response.body().string().trim();
                Log.d(TAG, "Server response: '" + responseString + "'");

                if ("YOXDU".equalsIgnoreCase(responseString)) {
                    Log.d(TAG, "IMEI not found on server (YOXDU). Registering device...");
                    TEST_PHP(context, localParams, imei1, "", "", "", "", 0);
                    //TEST_PHP(localParams, imei1, "", "", "", "", 0);
                    return aa;
                }

                // Parse XML with XmlPullParser
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = factory.newPullParser();
                parser.setInput(new StringReader(responseString));

                int eventType = parser.getEventType();
                boolean inIMEI = false;
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        String name = parser.getName();
                        if ("IMEI".equals(name)) {
                            inIMEI = true;
                        } else if (inIMEI && Arrays.asList(column).contains(name)) {  // Проверка в массиве column
                            String content = parser.nextText();
                            aa.add(content != null ? content : "");
                        }
                    } else if (eventType == XmlPullParser.END_TAG) {
                        if ("IMEI".equals(parser.getName())) {
                            inIMEI = false;
                        }
                    }
                    eventType = parser.next();
                }
            }
        } catch (XmlPullParserException | IOException e) {
            Log.e(TAG, "XML parsing error: " + e.getMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, "Error in main method: ", e);
        }

        return aa;
    }


    // ИЗМЕНЕНО: Запрос переделан на POST
//    public static ArrayList<String> main(final String address, final String imei1, Context context, final ArrayList<String> localParams) {
//        ArrayList<String> aa = new ArrayList<>();
//        HttpURLConnection conn = null;
//        try {
//            String urlString = "http://" + address + "/testxml.php";
//            Log.d(TAG, "Requesting params from: " + urlString + " using POST");
//            URL url = new URL(urlString);
//
//            String postData = "IMEI=" + URLEncoder.encode(imei1, StandardCharsets.UTF_8.name());
//
//            conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("POST");
//            conn.setDoOutput(true);
//            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//            conn.setRequestProperty("Content-Length", Integer.toString(postData.length()));
//            conn.setUseCaches(false);
//            conn.setConnectTimeout(10000);
//            conn.setReadTimeout(10000);
//
//            try (OutputStream os = conn.getOutputStream()) {
//                os.write(postData.getBytes(StandardCharsets.UTF_8));
//            }
//
//            StringBuilder response = new StringBuilder();
//            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
//                String inputLine;
//                while ((inputLine = in.readLine()) != null) {
//                    response.append(inputLine);
//                }
//            }
//
//            String responseString = response.toString().trim();
//            Log.d(TAG, "Server response: '" + responseString + "'");
//
//            if ("YOXDU".equalsIgnoreCase(responseString)) {
//                Log.d(TAG, "IMEI not found on server (YOXDU). Registering device by sending current params...");
//                TEST_PHP(context, localParams, imei1, "", "", "", "", 0);
//                return aa;
//            }
//
//            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//            Document doc = dBuilder.parse(new InputSource(new StringReader(responseString)));
//
//            Element docElement = doc.getDocumentElement();
//            if (docElement == null) {
//                 Log.e(TAG, "XML parsing error: Document element is null.");
//                 return aa;
//            }
//            docElement.normalize();
//
//            NodeList nList = doc.getElementsByTagName("IMEI");
//            if (nList.getLength() > 0) {
//                Node nNode = nList.item(0);
//
//                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
//                    Element eElement = (Element) nNode;
//                    for (String s : column) {
//                        NodeList tagList = eElement.getElementsByTagName(s);
//                        String content = (tagList.getLength() > 0) ? tagList.item(0).getTextContent() : "";
//                        aa.add(content != null ? content : "");
//                    }
//                }
//            }
//        } catch (Exception e) {
//            Log.e(TAG, "Ошибка в методе main: ", e);
//        } finally {
//             if (conn != null) {
//                conn.disconnect();
//             }
//        }
//        return aa;
//    }

    public static void TEST_PHP(final Context context, final ArrayList<String> param, final String IMEI1,
                                final String latitude_n, final String longitude_m, final String speed_n, final String dtTime, final int Loc){

        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            public void run() {
                String postData;
                try {
                    StringBuilder dataBuilder = new StringBuilder();
                    dataBuilder.append("IMEI=").append(URLEncoder.encode(IMEI1, StandardCharsets.UTF_8.name()));

                    if (param != null) {
                        for (int i = 0; i < column.length; i++) {
                            if (i < param.size() && param.get(i) != null) {
                                String value = param.get(i);
                                if (i == 2 || i == 4 || i == 5 || i == 6) { // FT, FD, US, PS
                                    try {
                                        value = EncryptDecryptStringWithAES.decrypt(value);
                                    } catch (Exception e) {
                                        Log.w(TAG, "Не удалось расшифровать параметр " + column[i] + ", отправляется как есть.");
                                    }
                                }
                                dataBuilder.append("&").append(column[i]).append("=").append(URLEncoder.encode(value, StandardCharsets.UTF_8.name()));
                            }
                        }
                    }
                    postData = dataBuilder.toString();
                    Log.d(TAG, "Данные параметров для отправки (POST): " + postData);

                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG,"Ошибка кодирования URL: ", e);
                    return;
                }

                String addressUrlString = "";
                try {
                     if (param != null && param.size() > 2 && !param.get(2).isEmpty()) {
                        String decryptedIp = EncryptDecryptStringWithAES.decrypt(param.get(2));
                        if (decryptedIp != null && !decryptedIp.isEmpty()) {
                             addressUrlString = "http://" + decryptedIp + "/test.php";
                        }
                    } else {
                         return;
                    }
                } catch (Exception e) {
                    Log.e(TAG,"Ошибка создания адреса: ", e);
                    return;
                }

                if (!addressUrlString.isEmpty()) {
                    HttpURLConnection conn = null;
                    try {
                        URL url = new URL(addressUrlString);
                        conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setDoOutput(true);
                        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                        conn.setRequestProperty("Content-Length", Integer.toString(postData.length()));
                        conn.setUseCaches(false);

                        try (OutputStream os = conn.getOutputStream()) {
                            os.write(postData.getBytes(StandardCharsets.UTF_8));
                        }

                        int responseCode = conn.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            Log.d(TAG, "Данные успешно отправлены (POST). Код ответа: " + responseCode);
                        } else {
                            Log.w(TAG, "Ошибка при отправке данных (POST). Код ответа: " + responseCode);
                        }

                    } catch (IOException e) {
                        Log.e(TAG, "Ошибка HTTP-соединения: ", e);
                    } finally {
                        if (conn != null) {
                            conn.disconnect();
                        }
                    }
                }
            }
        }).start();
    }

    public static void sendGpsOnly(final Context context, final ArrayList<String> param, final String IMEI1,
                                   final String latitude, final String longitude, final String speed, final String dtTime) {
        new Thread(() -> {
            String postData;
            try {
                StringBuilder dataBuilder = new StringBuilder();
                dataBuilder.append("IMEI=").append(URLEncoder.encode(IMEI1, StandardCharsets.UTF_8.name()));
                dataBuilder.append("&Lat=").append(URLEncoder.encode(latitude, StandardCharsets.UTF_8.name()));
                dataBuilder.append("&Long=").append(URLEncoder.encode(longitude, StandardCharsets.UTF_8.name()));
                dataBuilder.append("&Speed=").append(URLEncoder.encode(speed, StandardCharsets.UTF_8.name()));
                dataBuilder.append("&DTime=").append(URLEncoder.encode(dtTime, StandardCharsets.UTF_8.name()));
                postData = dataBuilder.toString();
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Ошибка кодирования URL для GPS: ", e);
                return;
            }

            String addressUrlString = "";
            try {
                if (param != null && param.size() > 2 && !param.get(2).isEmpty()) {
                    String decryptedIp = EncryptDecryptStringWithAES.decrypt(param.get(2));
                     if (decryptedIp != null && !decryptedIp.isEmpty()) {
                        addressUrlString = "http://" + decryptedIp + "/gps.php";
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Ошибка создания адреса для GPS: ", e);
                return;
            }

            if (!addressUrlString.isEmpty()) {
                 HttpURLConnection conn = null;
                try {
                    URL url = new URL(addressUrlString);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setRequestProperty("Content-Length", Integer.toString(postData.length()));
                    conn.setUseCaches(false);

                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(postData.getBytes(StandardCharsets.UTF_8));
                    }

                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        Log.d(TAG, "GPS данные успешно отправлены.");
                    }

                } catch (IOException e) {
                    Log.e(TAG, "Ошибка HTTP-соединения при отправке GPS: ", e);
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }

        }).start();
    }
}
