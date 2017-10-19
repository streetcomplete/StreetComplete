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

public class NoteImageAdapter extends BaseAdapter
{
	private Context context;
	private List<Bitmap> images = new ArrayList<>();

	public NoteImageAdapter(Context context, List<Bitmap> images){this.context = context; this.images = images;}

	@Override public int getCount(){ return images.size(); }
	@Override public Object getItem(int i){ return images.get(i); }
	@Override public long getItemId(int i){ return 0; }

	@Override public View getView(int i, View convertView, ViewGroup parent)
	{
		ImageView imageView = new ImageView(context);
		imageView.setImageBitmap(images.get(i));
		imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		imageView.setLayoutParams(new GridView.LayoutParams(150, 150));
		return imageView;
	}
}
