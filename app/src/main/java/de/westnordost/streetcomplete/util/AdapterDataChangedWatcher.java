package de.westnordost.streetcomplete.util;

import android.support.v7.widget.RecyclerView;

public class AdapterDataChangedWatcher extends RecyclerView.AdapterDataObserver
{
	public interface Listener { void onAdapterDataChanged(); }
	private final Listener listener;

	public AdapterDataChangedWatcher(Listener listener) { this.listener = listener; }

	@Override public void onChanged() { listener.onAdapterDataChanged(); }
	@Override public void onItemRangeChanged(int start, int count) { onChanged(); }
	@Override public void onItemRangeInserted(int start, int count) { onChanged(); }
	@Override public void onItemRangeRemoved(int start, int count) { onChanged(); }
	@Override public void onItemRangeMoved(int from, int to, int count) { onChanged(); }
}
