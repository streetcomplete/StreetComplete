package de.westnordost.osmagent.util;

import android.util.Log;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;

public class SerialExecutorService
{
	private static final String TAG = "SerialExecutorService";

	private Thread executorThread;

	private LinkedBlockingQueue<Runnable> tasks;
	private int initialQueueSize;
	private volatile boolean pause;

	public SerialExecutorService()
	{
		tasks = new LinkedBlockingQueue<>();
		executorThread = new Thread(new Worker());
	}

	public void init()
	{
		executorThread.start();
	}

	public void shutdown()
	{
		clear();
		executorThread.interrupt();
	}

	/** Replace current tasks with new tasks. The current task will finish its execution */
	public void replace(Collection<Runnable> newTasks)
	{
		clear();
		add(newTasks);
	}

	/** Add some more tasks to the execution queue */
	public synchronized void add(Collection<Runnable> newTasks)
	{
		tasks.addAll(newTasks);
		initialQueueSize = tasks.size();
	}

	/** Clears the queue and thus stops execution. The current task will finish its execution */
	public void clear()
	{
		tasks.clear();
	}

	/** Pause. The current task will finish its execution */
	public void pause()
	{
		pause = true;
	}

	/** Resume just where it left off before calling pause. */
	public void resume()
	{
		pause = false;
		synchronized (this)
		{
			notify();
		}
	}

	/* The loop */

	private class Worker implements Runnable
	{
		@Override public void run()
		{
			while (!Thread.interrupted())
			{
				try
				{
					while (pause)
					{
						synchronized (this)
						{
							wait();
						}
					}
				} catch (InterruptedException e)
				{
					// ok, finish thread
					break;
				}

				if(tasks.peek() == null)
				{
					dispatchDone();
				}

				Runnable task;

				try
				{
					task = tasks.take();
				}
				catch (InterruptedException e)
				{
					// ok, finish thread
					break;
				}

				try
				{
					task.run();
				}
				catch(Throwable e)
				{
					Log.e(TAG, "Error while executing " + task.toString() + ":", e);
				}

				dispatchProgress();
			}
		}
	}

	/* Progress */

	private ProgressListener progressListener;

	public void setProgressListener(ProgressListener progressListener)
	{
		this.progressListener = progressListener;
	}

	private synchronized void dispatchProgress()
	{
		if (progressListener == null) return;
		progressListener.onProgress(initialQueueSize - tasks.size(), initialQueueSize);
	}

	private void dispatchDone()
	{
		if (progressListener == null) return;
		progressListener.onDone();
	}
}
