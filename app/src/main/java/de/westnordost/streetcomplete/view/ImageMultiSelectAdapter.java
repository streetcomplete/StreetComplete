package de.westnordost.streetcomplete.view;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Adapter to select several images from a list of images */
public class ImageMultiSelectAdapter extends AbstractImageSelectAdapter
{
	private Set<Integer> selectedIndices;
	private int maxSelectableIndices;

	public ImageMultiSelectAdapter()
	{
		selectedIndices = new HashSet<>();
	}

	public ImageMultiSelectAdapter(int maxSelectableIndices)
	{
		selectedIndices = new HashSet<>(maxSelectableIndices);
		this.maxSelectableIndices = maxSelectableIndices;
	}

	public List<Integer> getSelectedIndices()
	{
		return new ArrayList<>(selectedIndices);
	}

	@Override public boolean isIndexSelected(int index)
	{
		return selectedIndices.contains(index);
	}

	@Override protected boolean doSelectIndex(int index)
	{
		if(maxSelectableIndices > 0 && maxSelectableIndices >= selectedIndices.size()) return false;

		selectedIndices.add(index);
		return true;
	}

	@Override protected boolean doDeselectIndex(int index)
	{
		selectedIndices.remove(index);
		return true;
	}
}
