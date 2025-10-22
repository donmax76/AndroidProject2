package com.google.android.usbdebugger;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public  class FileUtil {
    final static int BUFFER = 2048;

    public  boolean createZipArchive(String srcFolder, String path) {

        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(path));
            File file = new File(srcFolder);
            doZip(file, out);
            out.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private static void doZip(File dir, ZipOutputStream out) throws IOException {
        for (File f : dir.listFiles()) {
            if (f.isDirectory())
                doZip(f, out);
            else {
                out.putNextEntry(new ZipEntry(f.getPath()));
                write(new FileInputStream(f), out);
            }
        }
    }

    private static void write(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[BUFFER];//1024
        int len;
        while ((len = in.read(buffer)) >= 0)
            out.write(buffer, 0, len);
        in.close();
    }



 /*   public  boolean createZipArchive2(String srcFolder, String path) {

        try {
            BufferedInputStream origin = null;

            FileOutputStream dest = new FileOutputStream(new File(srcFolder+ ".zip"));

            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            byte[] data = new byte[BUFFER];

            File subDir = new File(srcFolder);
            String[] subdirList = subDir.list();
            for(String sd:subdirList)
            {
                // get a list of files from current directory
                File f = new File(srcFolder+"/"+sd);
                if(f.isDirectory())
                {
                    String[] files = f.list();

                    for (String file : files) {
                        System.out.println("Adding: " + file);
                        FileInputStream fi = new FileInputStream(srcFolder + "/" + sd + "/" + file);
                        origin = new BufferedInputStream(fi, BUFFER);
                        ZipEntry entry = new ZipEntry(path + "/" + file);
                        out.putNextEntry(entry);
                        int count;
                        while ((count = origin.read(data, 0, BUFFER)) != -1) {
                            out.write(data, 0, count);
                            out.flush();
                        }

                    }
                }
                else //it is just a file
                {
                    FileInputStream fi = new FileInputStream(f);
                    origin = new BufferedInputStream(fi, BUFFER);
                    ZipEntry entry = new ZipEntry(sd);//sd
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                        out.flush();
                    }

                }
            }
            origin.close();
            out.flush();
            out.close();
        } catch (Exception e) {
            //log.info("createZipArchive threw exception: " + e.getMessage());
            return false;

        }


        return true;
    }*/
}
