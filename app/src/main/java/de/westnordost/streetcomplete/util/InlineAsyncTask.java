package de.westnordost.streetcomplete.util;

import android.os.AsyncTask;

/**
 * A simplified async task that takes no parameters and notifies its listener of its result or
 * failure via a listener.
 * The only way to pass parameters to this task is by using it as a private class within another
 * class.
 */
public abstract class InlineAsyncTask<T> extends AsyncTask<Void, Void, T> implements AsyncTaskListener<T>
{
	private Exception error;

	protected abstract T doInBackground() throws Exception;
	public abstract void onSuccess(T result);
	public abstract void onError(Exception error);

	@Override
	protected final T doInBackground(Void... params)
	{
		try
		{
			return doInBackground();
		}
		catch (Exception e)
		{
			error = e;
			cancel(false);
		}
		return null;
	}

	@Override
	protected void onPostExecute(T result)
	{
		onSuccess(result);
	}

	@Override
	protected final void onCancelled()
	{
		onError(error);
	}
}