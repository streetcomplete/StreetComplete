package de.westnordost.streetcomplete.util;

import android.content.Context;

public class DpUtil
{
	public static float toDp(float px, Context context) {
		return px / context.getResources().getDisplayMetrics().density;
	}

	public static float toPx(float dp, Context context) {
		return dp * context.getResources().getDisplayMetrics().density;
	}
}
