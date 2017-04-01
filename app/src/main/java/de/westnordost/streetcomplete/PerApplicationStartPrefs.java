package de.westnordost.streetcomplete;

import android.os.Bundle;

/* Wraps bundle so there will be no injection misunderstandings (since bundle is quite a class) */
public class PerApplicationStartPrefs
{
	private Bundle bundle = new Bundle();
	public Bundle get()
	{
		return bundle;
	}
}
