package org.alex73.android.dzietkam.util;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class IO {
    public static String read(InputStream in, String charset) throws IOException{
        StringBuilder r=new StringBuilder();
        char[] buffer=new char[8192];
        InputStreamReader rd=new InputStreamReader(in,charset);
        while(true) {
            int len=rd.read(buffer);
            if (len<0) {
                break;
            }
            r.append(buffer,0,len);
        }
        return r.toString();
    }
    public static String readText(File file) throws IOException {
        try (FileInputStream is = new FileInputStream(file)) {
            return read(is, "UTF-8");
        }
    }
}
