package de.westnordost.streetcomplete.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import de.westnordost.streetcomplete.R;

public class ItemViewHolder  extends RecyclerView.ViewHolder
{
	private ImageView imageView;
	private TextView textView;
	private TextView descriptionView;

	public interface OnClickListener
	{
		void onClick(int index);
	}

	public ItemViewHolder(View itemView)
	{
		super(itemView);
		imageView = itemView.findViewById(R.id.imageView);
		textView = itemView.findViewById(R.id.textView);
		descriptionView = itemView.findViewById(R.id.descriptionView);
	}

	public void bind(Item item)
	{
		imageView.setImageResource(item.drawableId);

		if(item.titleId != 0) textView.setText(item.titleId);
		else textView.setText(null);

		if(item.descriptionId != 0) descriptionView.setText(item.descriptionId);
		else descriptionView.setText(null);
	}

	public void setSelected(boolean selected)
	{
		itemView.setSelected(selected);
	}

	public void setOnClickListener(OnClickListener listener)
	{
		itemView.setOnClickListener(v -> {
			int index = getAdapterPosition();
			if(index != RecyclerView.NO_POSITION) listener.onClick(index);
		});
	}
}
