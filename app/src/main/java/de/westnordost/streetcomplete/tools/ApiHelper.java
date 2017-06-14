package de.westnordost.streetcomplete.tools;

import android.os.Build;

public class ApiHelper {
    public static int SDK_INT = Build.VERSION.SDK_INT;

    /**
     * @return if >=21
     */
    public static boolean hasLolliPop() {
        return SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }
}