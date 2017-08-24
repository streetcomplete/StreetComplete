package de.westnordost.streetcomplete.view;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.westnordost.streetcomplete.R;

public class ImageSelectAdapter extends RecyclerView.Adapter<ImageSelectAdapter.ViewHolder>
{
	private ArrayList<ImageSelectAdapter.Item> items;
	private Set<Integer> selectedIndices;
	private int maxSelectableIndices;
	private int cellLayoutId = R.layout.labeled_image_select_cell;

	public interface OnItemSelectionListener
	{
		void onIndexSelected(int index);
		void onIndexDeselected(int index);
	}
	private ImageSelectAdapter.OnItemSelectionListener onItemSelectionListener;

	public ImageSelectAdapter()
	{
		selectedIndices = new HashSet<>();
		this.maxSelectableIndices = -1;
	}

	public ImageSelectAdapter(int maxSelectableIndices)
	{
		selectedIndices = new HashSet<>();
		this.maxSelectableIndices = maxSelectableIndices;
	}

	public void setOnItemSelectionListener(
			ImageSelectAdapter.OnItemSelectionListener onItemSelectionListener)
	{
		this.onItemSelectionListener = onItemSelectionListener;
	}

	public void setCellLayout(int cellLayoutId)
	{
		this.cellLayoutId = cellLayoutId;
	}

	public ArrayList<Integer> getSelectedIndices()
	{
		return new ArrayList<>(selectedIndices);
	}

	public void selectIndices(List<Integer> indices)
	{
		for(Integer index : indices)
		{
			selectIndex(index);
		}
	}

	public void setItems(List<ImageSelectAdapter.Item> items)
	{
		this.items = new ArrayList<>(items);
		notifyDataSetChanged();
	}

	public void addItems(Collection<ImageSelectAdapter.Item> items)
	{
		int len = this.items.size();
		this.items.addAll(items);
		notifyItemRangeInserted(len, items.size());
	}

	@Override public ImageSelectAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View view = LayoutInflater.from(parent.getContext()).
				inflate(cellLayoutId, parent, false);
		return new ImageSelectAdapter.ViewHolder(view);
	}

	public boolean isIndexSelected(int index)
	{
		return selectedIndices.contains(index);
	}

	public void selectIndex(int index)
	{
		checkIndexRange(index);
		// special case: toggle-behavior if only one index can be selected
		if(maxSelectableIndices == 1 && selectedIndices.size() == 1)
		{
			deselectIndex(selectedIndices.iterator().next());
		}
		else if(maxSelectableIndices > -1 && maxSelectableIndices <= selectedIndices.size())
		{
			return;
		}

		selectedIndices.add(index);

		notifyItemChanged(index);
		if(onItemSelectionListener != null)
		{
			onItemSelectionListener.onIndexSelected(index);
		}
	}

	public void deselectIndex(int index)
	{
		checkIndexRange(index);
		selectedIndices.remove(index);

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

	@Override public void onBindViewHolder(ImageSelectAdapter.ViewHolder holder, int position)
	{
		ImageSelectAdapter.Item item = items.get(position);
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
			imageView = itemView.findViewById(R.id.imageView);
			imageView.setOnClickListener(this);
			textView = itemView.findViewById(R.id.textView);
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
