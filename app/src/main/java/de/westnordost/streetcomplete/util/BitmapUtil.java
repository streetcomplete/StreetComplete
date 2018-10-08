package de.westnordost.streetcomplete.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;

public class BitmapUtil
{
	public static Bitmap createBitmapFrom(Drawable drawable)
	{
		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
			drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);
		return bitmap;
	}

	public static BitmapDrawable createBitmapDrawableFrom(Resources res, Drawable drawable)
	{
		return new BitmapDrawable(res, createBitmapFrom(drawable));
	}

	public static BitmapDrawable createBitmapDrawableFrom(Resources res, @DrawableRes int resId)
	{
		return createBitmapDrawableFrom(res, res.getDrawable(resId));
	}

	public static BitmapDrawable asBitmapDrawable(Resources res, Drawable drawable)
	{
		if(drawable instanceof BitmapDrawable)
		{
			return (BitmapDrawable) drawable;
		}
		return createBitmapDrawableFrom(res, drawable);
	}

	public static BitmapDrawable asBitmapDrawable(Resources res, @DrawableRes int resId)
	{
		return asBitmapDrawable(res, res.getDrawable(resId));
	}
}
