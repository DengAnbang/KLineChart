package com.github.fujianlian.klinechart.utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.fujianlian.klinechart.entity.DrawingBean;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DrawDataUtil {
    private static final String DRAW_DATA_DIR = "chart_draw";
    public static final String MODULE_NAME_CONTRACT = "contract";
    public static final String MODULE_NAME_SPOT = "spot";

    private static File getRootDir(Context context, String module) {
        File dir = new File(context.getFilesDir(), DRAW_DATA_DIR + "_" + module);
        dir.mkdir();
        return dir;
    }

    private static String getFilePath(Context context, String name, String module) {
        File rootDir = getRootDir(context, module);
        File file = new File(rootDir, name);
        return file.getAbsolutePath();
    }

    @NonNull
    public static List<DrawingBean> getDrawingBeans(Context context, String module, String symbol) {
        List<DrawingBean> beans = getDrawingBeans2(context, module, symbol);
        if (beans == null) {
            return new ArrayList<>();
        }
        return beans;
    }

    @Nullable
    private static List<DrawingBean> getDrawingBeans2(Context context, String module, String symbol) {
        String result = FileUtils.readString(getFilePath(context, symbol, module));
        if (TextUtils.isEmpty(result)) {
            return null;
        }
        Type type = new TypeToken<ArrayList<DrawingBean>>() {
        }.getType();
        try {
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            return gson.fromJson(result, type);
        } catch (Exception e) {
            e.printStackTrace();
            return readOldVersionDrawingBeans(result);
        }
    }

    @Nullable
    private static List<DrawingBean> readOldVersionDrawingBeans(String result) {
        Type type = new TypeToken<HashMap<String, List<DrawingBean>>>() {
        }.getType();
        try {
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            Map<String, List<DrawingBean>> map = gson.fromJson(result, type);
            if (map == null) {
                return null;
            }
            List<DrawingBean> beans = new ArrayList<>();
            for (Map.Entry<String, List<DrawingBean>> entry : map.entrySet()) {
                beans.addAll(entry.getValue());
            }
            return beans;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveDrawingBeans(Context context, String module, String symbol, List<DrawingBean> beans) {
        if (beans == null || beans.isEmpty()) {
            deleteFile(getFilePath(context, symbol, module));
            return;
        }
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String json = gson.toJson(beans);
        FileUtils.writeString(getFilePath(context, symbol, module), json);
    }

    private static void deleteFile(String path) {
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            file.delete();
        }
    }

}
