package de.westnordost.streetcomplete.util;

import android.support.annotation.NonNull;
import android.text.Editable;

public class TextChangedWatcher extends DefaultTextWatcher
{
	public interface Listener { void onTextChanged(); }
	private final Listener listener;

	public TextChangedWatcher(@NonNull Listener listener) { this.listener = listener;}

	@Override public void beforeTextChanged(CharSequence s, int start, int count, int after){}
	@Override public void onTextChanged(CharSequence s, int start, int before, int count){}
	@Override public void afterTextChanged(Editable s){ listener.onTextChanged(); }
}
