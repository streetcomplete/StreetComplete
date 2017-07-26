package de.westnordost.streetcomplete.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

public abstract class ListAdapter<T> extends RecyclerView.Adapter<ListAdapter.ViewHolder>
{
	private List<T> list;

	public ListAdapter(List<T> list)
	{
		this.list = list;
	}

	@Override public void onBindViewHolder(ListAdapter.ViewHolder holder, int position)
	{
		holder.update(list.get(position));
	}

	@Override public int getItemCount()
	{
		return list.size();
	}

	public abstract class ViewHolder extends RecyclerView.ViewHolder
	{
		public ViewHolder(View itemView)
		{
			super(itemView);
		}

		protected abstract void update(T with);
	}
}
