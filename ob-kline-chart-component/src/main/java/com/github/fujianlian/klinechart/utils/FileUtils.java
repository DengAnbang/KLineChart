package com.github.fujianlian.klinechart.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtils {

    /**
     * 将String写到文件里面（覆盖写）
     *
     * @param absolutePath 文件的绝对路径
     * @param content      string内容
     */
    public static boolean writeString(String absolutePath, String content) {
        if (absolutePath == null || content == null) {
            return false;
        }
        File file = new File(absolutePath);
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(file), 1024);
            bufferedWriter.write(content);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.flush();
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 从文件里面读取String内容
     *
     * @param absolutePath 文件的绝对路径
     * @return
     */
    public static String readString(String absolutePath) {
        if (absolutePath == null) {
            return null;
        }
        File file = new File(absolutePath);
        BufferedReader bufferedReader = null;
        if (!file.exists()) {
            return null;
        }
        StringBuilder resultStringBuilder = new StringBuilder();
        try {
            String currentLine;
            bufferedReader = new BufferedReader(new FileReader(file));
            while ((currentLine = bufferedReader.readLine()) != null) {
                resultStringBuilder.append(currentLine);
            }
            return resultStringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static boolean exists(String absolutePath) {
        if (absolutePath == null) {
            return false;
        }
        File file = new File(absolutePath);
        return file.exists();
    }

}
