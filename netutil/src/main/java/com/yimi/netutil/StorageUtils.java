package com.yimi.netutil;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

/**
 * Created by liuniu on 2017/12/14.
 */

public class StorageUtils {
    public static File getCacheStorage() {
        File rootDir = getRootDirectory();
        File reserveCacheDir = new File(rootDir, "Cache");
        if (!reserveCacheDir.exists()) {
            if (!reserveCacheDir.mkdir()) {
                reserveCacheDir = rootDir;
            }
        }
        return reserveCacheDir;
    }

    /**
     * 获取存储路径
     *
     * @return
     */
    private static File getRootDirectory() {
        File rootDir = null;
        if (verifyUnmountedSDCard()) {
            rootDir = getExternalCacheDir();
        }

        if (rootDir == null) {
            Context context = NetUtils.getContext();
            if (context != null) {
                rootDir = context.getCacheDir();
            }
        }

        return rootDir;
    }

    /**
     * 获取外部存储介质(SD卡)上的缓存路径
     *
     * @return
     */
    private synchronized static File getExternalCacheDir() {
        try {
            File appCacheDir = new File(Environment.getExternalStorageDirectory(),
                    "yimi");
            if (!appCacheDir.exists()) {
                if (!appCacheDir.mkdirs()) {
                    return null;
                }
            }
            return appCacheDir;
        } catch (SecurityException e) {
            return null;
        }
    }

    /**
     * 读取指定目录下文件中的数据
     *
     * @param context
     * @param dirName  目录名
     * @param fileName 文件名
     * @return 文件中的数据
     */
    public static String readDataFromSDCard(Context context, String dirName, String fileName) {
        if (!verifyUnmountedSDCard()) {
            return null;
        }

        String target = Environment.getExternalStorageDirectory().getPath() + "/" + dirName + "/"
                + fileName;
        File cacheFile = new File(target);
        if (!cacheFile.exists()) {
            return null;
        }
        InputStreamReader reader = null;
        StringWriter writer = new StringWriter();
        try {
            reader = new InputStreamReader(new FileInputStream(cacheFile));
            char[] buffer = new char[2 * 1024];// 将输入流写入输出流
            int n = 0;
            while (-1 != (n = reader.read(buffer))) {
                writer.write(buffer, 0, n);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return writer.toString();
    }

    /**
     * 在SD卡下创建目录生成文件并写入数据
     *
     * @param context
     * @param data     需要写入的数据
     * @param dirName  目录名
     * @param fileName 文件名
     */
    public static void saveDataToSDCard(Context context, String data, String dirName, String fileName) {
        if (!verifyUnmountedSDCard()) {
            return;
        }

        File sdDir = Environment.getExternalStorageDirectory();
        String filePath = sdDir.getPath() + "/" + dirName + "/";
        sdDir = new File(filePath);
        if (!sdDir.exists()) {
            sdDir.mkdirs();
        }

        File file = new File(filePath + fileName);
        BufferedWriter out = null;

        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            out = new BufferedWriter(new FileWriter(file));
            out.write(data);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 检测是否安装SD卡
     *
     * @return true:有SD卡 false:有SD卡
     */
    public static boolean verifyUnmountedSDCard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED); // 判断sd卡是否存在;
    }


}
