package de.westnordost.streetcomplete.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.westnordost.streetcomplete.R;

/** Select a number of items from a list of items */
public class ImageSelectAdapter extends RecyclerView.Adapter<ItemViewHolder>
{
	private ArrayList<Item> items = new ArrayList<>();
	private Set<Integer> selectedIndices;
	private int maxSelectableIndices;
	private int cellLayoutId = R.layout.cell_labeled_image_select;

	public interface OnItemSelectionListener
	{
		void onIndexSelected(int index);
		void onIndexDeselected(int index);
	}
	private final List<ImageSelectAdapter.OnItemSelectionListener> listeners = new ArrayList<>();

	public ImageSelectAdapter()
	{
		this(-1);
	}

	public ImageSelectAdapter(int maxSelectableIndices)
	{
		selectedIndices = new HashSet<>();
		this.maxSelectableIndices = maxSelectableIndices;
	}

	public void addOnItemSelectionListener(ImageSelectAdapter.OnItemSelectionListener listener)
	{
		listeners.add(listener);
	}

	public void setCellLayout(int cellLayoutId)
	{
		this.cellLayoutId = cellLayoutId;
	}

	public ArrayList<Integer> getSelectedIndices()
	{
		return new ArrayList<>(selectedIndices);
	}

	public List<Item> getSelectedItems()
	{
		List<Item> result = new ArrayList<>();
		for (Integer index : selectedIndices)
		{
			result.add(items.get(index));
		}
		return result;
	}

	public void select(List<Integer> indices)
	{
		for(Integer index : indices)
		{
			select(index);
		}
	}

	public void setItems(List<Item> items)
	{
		this.items = new ArrayList<>(items);
		notifyDataSetChanged();
	}

	@NonNull @Override
	public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
	{
		View view = LayoutInflater.from(parent.getContext()).inflate(cellLayoutId, parent, false);
		ItemViewHolder holder = new ItemViewHolder(view);
		holder.setOnClickListener(this::toggle);
		return holder;
	}

	public boolean isSelected(int index) { return selectedIndices.contains(index); }

	public void select(int index)
	{
		checkIndexRange(index);
		// special case: toggle-behavior if only one index can be selected
		if(maxSelectableIndices == 1 && selectedIndices.size() == 1)
		{
			deselect(selectedIndices.iterator().next());
		}
		else if(maxSelectableIndices > -1 && maxSelectableIndices <= selectedIndices.size())
		{
			return;
		}

		if(!selectedIndices.add(index)) return;

		notifyItemChanged(index);
		for (OnItemSelectionListener listener : listeners)
		{
			listener.onIndexSelected(index);
		}
	}

	public void deselect(int index)
	{
		checkIndexRange(index);
		if(!selectedIndices.remove(index)) return;

		notifyItemChanged(index);
		for (OnItemSelectionListener listener : listeners)
		{
			listener.onIndexDeselected(index);
		}
	}

	public void toggle(int index)
	{
		checkIndexRange(index);
		if(!isSelected(index))
		{
			select(index);
		}
		else
		{
			deselect(index);
		}
	}

	private void checkIndexRange(int index)
	{
		if(index < 0 || index >= items.size())
			throw new ArrayIndexOutOfBoundsException(index);
	}

	@Override public void onBindViewHolder(@NonNull ItemViewHolder holder, int position)
	{
		holder.bind(items.get(position));
		holder.setSelected(isSelected(position));
	}

	@Override public int getItemCount() { return items.size(); }
}
