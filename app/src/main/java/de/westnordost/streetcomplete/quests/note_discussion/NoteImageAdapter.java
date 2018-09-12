package de.westnordost.streetcomplete.quests.note_discussion;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.util.List;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osmnotes.AttachPhotoUtils;
import de.westnordost.streetcomplete.util.ViewUtils;
import de.westnordost.streetcomplete.view.ListAdapter;


public class NoteImageAdapter extends ListAdapter<String>
{
	private final Context context;

	public NoteImageAdapter(List<String> list, Context context)
	{
		super(list);
		this.context = context;
	}

	@Override public ViewHolder<String> onCreateViewHolder(ViewGroup parent, int viewType)
	{
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		return new NoteImageViewHolder(inflater.inflate(R.layout.cell_image_thumbnail, parent, false));
	}

	private class NoteImageViewHolder extends ViewHolder<String>
	{
		private ImageView imageView;

		public NoteImageViewHolder(View itemView)
		{
			super(itemView);
			imageView = itemView.findViewById(R.id.imageView);
			imageView.setOnClickListener(view ->
			{
				int index = getAdapterPosition();
				if(index > -1) onClickDelete(index);
			});
		}

		@Override protected void onBind(final String imagePath)
		{
			ViewUtils.postOnLayout(itemView, () -> onMeasured(imagePath));
		}

		private void onMeasured(String imagePath)
		{
			Bitmap bitmap = AttachPhotoUtils.resize(imagePath, imageView.getWidth());
			imageView.setImageBitmap(bitmap);
		}
	}

	private void onClickDelete(final int index)
	{
		new AlertDialog.Builder(context)
				.setMessage(R.string.quest_leave_new_note_photo_delete_title)
				.setNegativeButton(android.R.string.cancel, null)
				.setPositiveButton(android.R.string.ok, (dialog, which) -> delete(index))
				.show();
	}

	private void delete(int index)
	{
		String imagePath = getList().remove(index);

		if(imagePath != null)
		{
			File image = new File(imagePath);
			if (image.exists())
			{
				image.delete();
			}
		}
		notifyItemRemoved(index);
	}
}
