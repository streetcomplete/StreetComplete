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
					try {
						vto.removeOnGlobalLayoutListener(this);
					} catch (IllegalStateException e) {
						// ignore: checking isAlive before is not sufficient (race condition)
					}
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
					try {
						vto.removeOnPreDrawListener(this);
					} catch (IllegalStateException e) {
						// ignore: checking isAlive before is not sufficient (race condition)
					}
					runnable.run();
					return true;
				}
			});
		}
	}
}
