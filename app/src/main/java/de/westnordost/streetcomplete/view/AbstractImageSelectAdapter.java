package de.westnordost.streetcomplete.view;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.westnordost.streetcomplete.R;

public abstract class AbstractImageSelectAdapter extends RecyclerView.Adapter<AbstractImageSelectAdapter.ViewHolder>
{
	private ArrayList<AbstractImageSelectAdapter.Item> items;

	public interface OnItemSelectionListener
	{
		void onIndexSelected(int index);
		void onIndexDeselected(int index);
	}
	private AbstractImageSelectAdapter.OnItemSelectionListener onItemSelectionListener;

	public void setOnItemSelectionListener(
			AbstractImageSelectAdapter.OnItemSelectionListener onItemSelectionListener)
	{
		this.onItemSelectionListener = onItemSelectionListener;
	}

	public void setItems(List<AbstractImageSelectAdapter.Item> items)
	{
		this.items = new ArrayList<>(items);
		notifyDataSetChanged();
	}

	public void addItems(Collection<AbstractImageSelectAdapter.Item> items)
	{
		int len = this.items.size();
		this.items.addAll(items);
		notifyItemRangeInserted(len, items.size());
	}

	@Override public AbstractImageSelectAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View view = LayoutInflater.from(parent.getContext()).
				inflate(R.layout.labeled_image_select_cell, parent, false);
		return new AbstractImageSelectAdapter.ViewHolder(view);
	}

	public abstract boolean isIndexSelected(int index);
	protected abstract boolean doSelectIndex(int index);
	protected abstract boolean doDeselectIndex(int index);

	public void selectIndex(int index)
	{
		checkIndexRange(index);
		if(!doSelectIndex(index)) return;

		notifyItemChanged(index);
		if(onItemSelectionListener != null)
		{
			onItemSelectionListener.onIndexSelected(index);
		}
	}

	public void deselectIndex(int index)
	{
		checkIndexRange(index);
		if(!doDeselectIndex(index)) return;

		notifyItemChanged(index);
		if(onItemSelectionListener != null)
		{
			onItemSelectionListener.onIndexDeselected(index);
		}
	}

	public void toggleIndex(int index)
	{
		checkIndexRange(index);
		if(!isIndexSelected(index))
		{
			selectIndex(index);
		}
		else
		{
			deselectIndex(index);
		}
	}

	private void checkIndexRange(int index)
	{
		if(index < 0 || index >= items.size())
			throw new ArrayIndexOutOfBoundsException(index);
	}

	@Override public void onBindViewHolder(AbstractImageSelectAdapter.ViewHolder holder, int position)
	{
		AbstractImageSelectAdapter.Item item = items.get(position);
		holder.imageView.setImageResource(item.drawableId);
		holder.itemView.setSelected(isIndexSelected(position));
		if(item.titleId > -1) holder.textView.setText(item.titleId);
		else holder.textView.setText("");
	}

	@Override public int getItemCount()
	{
		if(items == null) return 0;
		return items.size();
	}

	class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
	{
		ImageView imageView;
		TextView textView;

		public ViewHolder(View v)
		{
			super(v);
			imageView = (ImageView) itemView.findViewById(R.id.imageView);
			imageView.setOnClickListener(this);
			textView = (TextView) itemView.findViewById(R.id.textView);
		}

		@Override public void onClick(View v)
		{
			toggleIndex(getAdapterPosition());
		}
	}

	public static class Item
	{
		public final int titleId;
		public final int drawableId;

		public Item(int drawableId, int titleId)
		{
			this.drawableId = drawableId;
			this.titleId = titleId;
		}
	}
}
