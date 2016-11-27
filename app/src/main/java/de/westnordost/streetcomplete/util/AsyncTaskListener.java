package de.westnordost.streetcomplete.util;

/** Listener for the result or failure of an async operation */
public interface AsyncTaskListener<T>
{
	void onSuccess(T result);
	void onError(Exception e);
}
