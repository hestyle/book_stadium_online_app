package cn.edu.hestyle.bookstadiumonline.util;

import android.content.Context;

/**
 * ApplicationContext工具类
 */
public class ApplicationContextUtil {
    private static Context applicationContext;

    public static void setApplicationContext(Context applicationContext) {
        ApplicationContextUtil.applicationContext = applicationContext;
    }

    public static Context getApplicationContext() {
        return applicationContext;
    }
}
