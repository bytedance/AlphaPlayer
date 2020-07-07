package com.ss.ugc.android.alphavideoplayer.utils;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.ss.ugc.android.alphavideoplayer.model.ConfigModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class JsonUtil {

    private static final String TAG = "JsonUtil";

    /**
     * 解析资源包，内部目录结构应如下：
     * ./
     *      config.json
     *      xxx.mp4
     *      xxx.mp4
     * @param resourcePath
     * @return
     */
    public static ConfigModel parseConfigModel(String resourcePath) {
        if (TextUtils.isEmpty(resourcePath)) {
            return null;
        }
        if (!(new File(resourcePath).exists())) {
            return null;
        }

        String configFilePath = resourcePath.endsWith(File.separator) ? resourcePath + "config.json"
                : resourcePath + File.separator + "config.json";
        if (!(new File(configFilePath).exists())) {
            return null;
        }

        FileInputStream fis = null;
        InputStreamReader isr = null;
        char input[] = null;
        try {
            fis = new FileInputStream(configFilePath);
            isr = new InputStreamReader(fis, "UTF-8");
            input = new char[fis.available()];
            isr.read(input);
            isr.close();
            fis.close();
        } catch (IOException e) {
            Log.e(TAG, "parse: " + e.toString());
        } finally {
            try {
                if (isr != null) {
                    isr.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "parse: " + e.toString());
            }
        }

        if (input == null) {
            return null;
        }

        String configStr = new String(input);
        Gson gson = new Gson();
        return gson.fromJson(configStr, ConfigModel.class);
    }
}
