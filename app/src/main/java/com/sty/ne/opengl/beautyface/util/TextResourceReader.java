package com.sty.ne.opengl.beautyface.util;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TextResourceReader {
    private static final String TAG = TextResourceReader.class.getSimpleName();

    /**
     * 用于读取GLSL文件中着色器代码
     * @param context
     * @param resourceID
     * @return
     */
    public static String readTextFileFromResource(Context context, int resourceID) {
        StringBuilder sb = new StringBuilder();

        try {
            InputStream inputStream = context.getResources().openRawResource(resourceID);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String nextLine;
            while ((nextLine = bufferedReader.readLine()) != null) {
                sb.append(nextLine);
                sb.append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not open resource: " + resourceID, e);
        }
        return sb.toString();
    }
}
