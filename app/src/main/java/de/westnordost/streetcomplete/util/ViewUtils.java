package de.westnordost.streetcomplete.util;

import androidx.annotation.NonNull;
import android.view.View;
import android.view.ViewTreeObserver;

public class ViewUtils
{
	public static void postOnLayout(@NonNull View view, @NonNull Runnable runnable)
	{
		ViewTreeObserver vto = view.getViewTreeObserver();
		if (vto.isAlive())
		{
			vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
			{
				@Override public void onGlobalLayout()
				{
					if(vto.isAlive()) vto.removeOnGlobalLayoutListener(this);
					runnable.run();
				}
			});
		}
	}

	public static void postOnPreDraw(@NonNull View view, @NonNull Runnable runnable)
	{
		ViewTreeObserver vto = view.getViewTreeObserver();
		if (vto.isAlive())
		{
			vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
			{
				@Override public boolean onPreDraw()
				{
					if(vto.isAlive()) vto.removeOnPreDrawListener(this);
					runnable.run();
					return true;
				}
			});
		}
	}
}
