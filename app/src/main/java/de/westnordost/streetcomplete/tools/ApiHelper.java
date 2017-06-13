package de.westnordost.streetcomplete.tools;

import android.os.Build;

public class ApiHelper {
    public static int SDK_INT = Build.VERSION.SDK_INT;

    /**
     * @return if >=17
     */
    public static boolean hasJellyBeanMR1() {
        return SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    }

    /**
     * @return if >=18
     */
    public static boolean hasJellyBeanMR2() {
        return SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    /**
     * @return if ==19
     */
    public static boolean isKitKat() {
        return SDK_INT == Build.VERSION_CODES.KITKAT;
    }

    /**
     * @return if >=19
     */
    public static boolean hasKitKat() {
        return SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    /**
     * @return if >=21
     */
    public static boolean hasLolliPop() {
        return SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    /**
     * @return if >=22
     */
    public static boolean hasLolliPopMR1() {
        return SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;
    }

    /**
     * Android 6.0
     *
     * @return if >=23
     */
    public static boolean hasMarshmallow() {
        return SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * @return if >=24
     */
    public static boolean hasAndroidN() {
        return SDK_INT >= Build.VERSION_CODES.N || "N".equals(Build.VERSION.CODENAME);
    }

    /**
     * @return if >=25
     */
    public static boolean hasAndroidN_MR1() {
        return SDK_INT >= Build.VERSION_CODES.N_MR1;
    }

    public static boolean hasAndroidO() {
        return Build.VERSION.RELEASE.equals("O") || SDK_INT >= 26;
    }
}