package de.westnordost.streetcomplete.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import de.westnordost.streetcomplete.util.DpUtil;

/** Mask the speech_bubble_none.9.png*/
public class MaskSpeechbubbleCornersFrameLayout extends FrameLayout
{
	public MaskSpeechbubbleCornersFrameLayout(Context context)
	{
		super(context);
	}

	public MaskSpeechbubbleCornersFrameLayout(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public MaskSpeechbubbleCornersFrameLayout(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP) public MaskSpeechbubbleCornersFrameLayout(
		Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
	{
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	@Override
	protected void dispatchDraw(Canvas canvas)
	{
		Path path = new Path();
		int corner = (int) DpUtil.toPx(10.5f, getContext());
		path.addRoundRect(new RectF(0,0,canvas.getWidth(),canvas.getHeight()), corner, corner, Path.Direction.CW);
		canvas.clipPath(path);
		super.dispatchDraw(canvas);
	}
}
