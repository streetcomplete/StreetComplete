package de.westnordost.streetcomplete.view;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.util.BitmapUtil;

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

	private ImageView leftSideImage, rightSideImage;
	private View leftSide, rightSide;
	private View strut;

	private OnClickSideListener listener;

	private int leftImageResId, rightImageResId;
	private boolean isLeftImageSet, isRightImageSet;
	private boolean onlyShowingOneSide;

	public interface OnClickSideListener
	{
		void onClick(boolean isRight);
	}

	private void init(Context context)
	{
		LayoutInflater.from(context).inflate(R.layout.side_select_puzzle, this, true);

		rotateContainer = findViewById(R.id.rotateContainer);
		rightSideImage = findViewById(R.id.rightSideImage);
		leftSideImage = findViewById(R.id.leftSideImage);
		strut = findViewById(R.id.strut);
		leftSide = findViewById(R.id.leftSide);
		rightSide = findViewById(R.id.rightSide);

		addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) ->
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

			int streetWidth = onlyShowingOneSide ? width : width / 2;
			if(!isLeftImageSet && leftImageResId != 0)
			{
				setStreetDrawable(leftImageResId, streetWidth, leftSideImage, true);
				isLeftImageSet = true;
			}
			if(!isRightImageSet && rightImageResId != 0)
			{
				setStreetDrawable(rightImageResId, streetWidth, rightSideImage, false);
				isRightImageSet = true;
			}
		});
	}

	private void onClick(boolean isRight)
	{
		if(listener != null) listener.onClick(isRight);
	}

	public void setListener(OnClickSideListener listener)
	{
		this.listener = listener;
		leftSide.setOnClickListener(view -> onClick(false));
		rightSide.setOnClickListener(view -> onClick(true));
	}

	public void setStreetRotation(float rotation)
	{
		rotateContainer.setRotation(rotation);
		float scale = (float) Math.abs(Math.cos(rotation * Math.PI / 180));
		rotateContainer.setScaleX(1 + scale*2/3f);
		rotateContainer.setScaleY(1 + scale*2/3f);
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
		leftImageResId = resId;
		replaceAnimated(resId, leftSideImage, true);
	}

	public void replaceRightSideImageResource(int resId)
	{
		rightImageResId = resId;
		replaceAnimated(resId, rightSideImage, false);
	}

	public void showOnlyRightSide()
	{
		isRightImageSet = false;
		onlyShowingOneSide = true;
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(0,0);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		strut.setLayoutParams(params);
	}

	public void showOnlyLeftSide()
	{
		isLeftImageSet = false;
		onlyShowingOneSide = true;
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(0,0);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		strut.setLayoutParams(params);
	}

	public void showBothSides()
	{
		isLeftImageSet = isRightImageSet = false;
		onlyShowingOneSide = false;
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(0,0);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		strut.setLayoutParams(params);
	}

	private void replaceAnimated(int resId, ImageView imgView, boolean flip180Degrees)
	{
		int width = onlyShowingOneSide ? rotateContainer.getWidth() : rotateContainer.getWidth() / 2;
		setStreetDrawable(resId, width, imgView, flip180Degrees);

		((View)imgView.getParent()).bringToFront();

		imgView.setScaleX(3);
		imgView.setScaleY(3);
		imgView.animate().scaleX(1).scaleY(1);
	}

	private void setStreetDrawable(int resId, int width, ImageView imageView, boolean flip180Degrees)
	{
		BitmapDrawable drawable = scaleToWidth(BitmapUtil.asBitmapDrawable(getResources(), resId), width, flip180Degrees);
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
}
