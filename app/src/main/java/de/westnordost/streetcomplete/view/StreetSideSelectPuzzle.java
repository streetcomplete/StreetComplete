package de.westnordost.streetcomplete.view;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import de.westnordost.streetcomplete.R;

public class StreetSideSelectPuzzle extends FrameLayout
{
	public StreetSideSelectPuzzle(@NonNull Context context)
	{
		super(context);
		init(context);
	}

	public StreetSideSelectPuzzle(@NonNull Context context, @Nullable AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}

	public StreetSideSelectPuzzle(@NonNull Context context, @Nullable AttributeSet attrs,
								  @AttrRes int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		init(context);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public StreetSideSelectPuzzle(@NonNull Context context, @Nullable AttributeSet attrs,
								  @AttrRes int defStyleAttr, @StyleRes int defStyleRes)
	{
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}

	private View rotateContainer;

	private ImageView rightSideImage;
	private ImageView leftSideImage;
	private ImageView oldRightSideImage;
	private ImageView oldLeftSideImage;

	private OnClickSideListener listener;

	private int defaultImageResId, leftImageResId, rightImageResId;

	public interface OnClickSideListener
	{
		void onClick(boolean isRight);
	}

	private void init(Context context)
	{
		LayoutInflater.from(context).inflate(R.layout.side_select_puzzle, this, true);

		rotateContainer = findViewById(R.id.rotateContainer);

		oldRightSideImage = findViewById(R.id.oldRightSideImage);
		oldLeftSideImage = findViewById(R.id.oldLeftSideImage);
		rightSideImage = findViewById(R.id.rightSideImage);
		leftSideImage = findViewById(R.id.leftSideImage);

		findViewById(R.id.leftSide).setOnClickListener(new OnClickListener()
		{
			@Override public void onClick(View view)
			{
				if(listener != null) listener.onClick(false);
			}
		});
		findViewById(R.id.rightSide).setOnClickListener(new OnClickListener()
		{
			@Override public void onClick(View view)
			{
				if(listener != null) listener.onClick(true);
			}
		});

		addOnLayoutChangeListener(new OnLayoutChangeListener()
		{
			@Override
			public void onLayoutChange(View v, int left, int top, int right, int bottom,
									   int oldLeft, int oldTop, int oldRight, int oldBottom)
			{
				int height = Math.max(bottom - top, right - left);
				int width = Math.min(bottom - top, right - left);
				ViewGroup.LayoutParams params = rotateContainer.getLayoutParams();
				if(params.width != width || params.height != height)
				{
					params.width = width;
					params.height = height;
					rotateContainer.setLayoutParams(params);
				}

				int streetWidth = width / 2;
				if(defaultImageResId != 0)
				{
					setStreetDrawable(defaultImageResId, streetWidth, oldLeftSideImage, true);
					setStreetDrawable(defaultImageResId, streetWidth, oldRightSideImage, false);
					defaultImageResId = 0;
				}
				if(leftImageResId != 0)
				{
					setStreetDrawable(leftImageResId, streetWidth, leftSideImage, true);
					leftImageResId = 0;
				}
				if(rightImageResId != 0)
				{
					setStreetDrawable(rightImageResId, streetWidth, rightSideImage, false);
					rightImageResId = 0;
				}
			}
		});
	}

	public void setListener(OnClickSideListener listener)
	{
		this.listener = listener;
	}

	public void setStreetRotation(float rotation)
	{
		rotateContainer.setRotation(rotation);
	}

	public void setDefaultStreetSideImageResource(int resId)
	{
		defaultImageResId = resId;
	}

	public void setLeftSideImageResource(int resId)
	{
		leftImageResId = resId;
	}

	public void setRightSideImageResource(int resId)
	{
		rightImageResId = resId;
	}

	public void replaceLeftSideImageResource(int resId)
	{
		replaceAnimated(resId, leftSideImage, oldLeftSideImage, true);
	}

	public void replaceRightSideImageResource(int resId)
	{
		replaceAnimated(resId, rightSideImage, oldRightSideImage, false);
	}

	private void replaceAnimated(int resId, ImageView imgView, ImageView oldImgView, boolean flip180Degrees)
	{
		oldImgView.setImageDrawable(imgView.getDrawable());
		int width = rotateContainer.getWidth() / 2;
		setStreetDrawable(resId, width, imgView, flip180Degrees);

		int animationDir = flip180Degrees ? -1 : 1;
		ObjectAnimator.ofFloat(imgView, "translationX", animationDir * imgView.getWidth(), 0).start();
		ObjectAnimator.ofFloat(oldImgView, "alpha", 1, 0).start();
	}

	private void setStreetDrawable(int resId, int width, ImageView imageView, boolean flip180Degrees)
	{
		BitmapDrawable drawable = scaleToWidth(asBitmapDrawable(resId), width, flip180Degrees);
		drawable.setTileModeY(Shader.TileMode.REPEAT);
		imageView.setImageDrawable(drawable);
	}

	private BitmapDrawable scaleToWidth(BitmapDrawable drawable, int width, boolean flip180Degrees)
	{
		Matrix m = new Matrix();
		float scale = (float) width / drawable.getIntrinsicWidth();
		m.postScale(scale, scale);
		if(flip180Degrees) m.postRotate(180);
		Bitmap bitmap = Bitmap.createBitmap(drawable.getBitmap(), 0, 0,
				drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), m, true);
		return new BitmapDrawable(getResources(), bitmap);
	}

	private BitmapDrawable asBitmapDrawable(int resId)
	{
		Drawable drawable = getResources().getDrawable(resId);
		if(drawable instanceof BitmapDrawable)
		{
			return (BitmapDrawable) drawable;
		}
		return createBitmapDrawableFrom(drawable);
	}

	private BitmapDrawable createBitmapDrawableFrom(Drawable drawable)
	{
		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);
		return new BitmapDrawable(getResources(), bitmap);
	}
}
