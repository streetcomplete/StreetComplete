package de.westnordost.streetcomplete.view;

/** Adapter to select one image from a list of images */
public class ImageSelectAdapter extends AbstractImageSelectAdapter
{
	private int selectedIndex = -1;

	/** @return -1 if nothing selected, otherwise the index */
	public int getSelectedIndex()
	{
		return selectedIndex;
	}

	@Override public boolean isIndexSelected(int index)
	{
		return index == selectedIndex;
	}

	@Override protected boolean doSelectIndex(int index)
	{
		if(selectedIndex != -1)
		{
			deselectIndex(selectedIndex);
		}
		selectedIndex = index;
		return true;
	}

	@Override protected boolean doDeselectIndex(int index)
	{
		selectedIndex = -1;
		return true;
	}
}
