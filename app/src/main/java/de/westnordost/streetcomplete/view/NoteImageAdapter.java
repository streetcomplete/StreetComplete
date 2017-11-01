package de.westnordost.streetcomplete.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import de.westnordost.streetcomplete.R;

public class NoteImageAdapter extends BaseAdapter
{
	private Context context;
	private List<Bitmap> images = new ArrayList<>();

	public NoteImageAdapter(Context context, List<Bitmap> images)
	{
		this.context = context;
		this.images = images;
	}

	@Override public int getCount(){ return images.size(); }
	@Override public Object getItem(int i){ return images.get(i); }
	@Override public long getItemId(int i){ return i; }

	@Override public View getView(int i, View convertView, ViewGroup parent)
	{
		ImageView imageView = new ImageView(context);
		imageView.setImageBitmap(images.get(i));
		imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		imageView.setLayoutParams(new GridView.LayoutParams(
				parent.getResources().getDimensionPixelSize(R.dimen.photo_thumbnail_size),
				parent.getResources().getDimensionPixelSize(R.dimen.photo_thumbnail_size)));
		return imageView;
	}
}
