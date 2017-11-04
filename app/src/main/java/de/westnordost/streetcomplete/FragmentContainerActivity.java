package de.westnordost.streetcomplete;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class FragmentContainerActivity extends AppCompatActivity
{
	public static final String EXTRA_FRAGMENT_CLASS = "de.westnordost.streetcomplete.fragment_class";

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fragment_container);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override protected void onPostCreate(@Nullable Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		if(savedInstanceState == null)
		{
			String fragmentClass = getIntent().getStringExtra(EXTRA_FRAGMENT_CLASS);
			if (fragmentClass != null)
			{
				try
				{
					Class c = Class.forName(fragmentClass);
					Fragment f = (Fragment) c.newInstance();
					f.setArguments(getIntent().getExtras());
					setCurrentFragment(f, false);
				} catch (Exception e)
				{
					throw new RuntimeException(e);
				}
			}
		}
	}

	public void setCurrentFragment(Fragment fragment)
	{
		setCurrentFragment(fragment, true);
	}

	private void setCurrentFragment(Fragment fragment, boolean addToBackStack)
	{
		FragmentTransaction tr = getSupportFragmentManager().beginTransaction();
		tr.setCustomAnimations(
				R.anim.enter_from_right, R.anim.exit_to_left,
				R.anim.enter_from_left, R.anim.exit_to_right);
		tr.replace(R.id.fragment_container, fragment);
		if(addToBackStack) tr.addToBackStack(null);
		tr.commit();
	}

	private Fragment getCurrentFragment()
	{
		return getSupportFragmentManager().findFragmentById(R.id.fragment_container);
	}

	@Override public void onBackPressed() {
		if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
			getSupportFragmentManager().popBackStack();
		} else {
			super.onBackPressed();
		}
	}

	@Override public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);

		Fragment f = getCurrentFragment();
		if(f instanceof IntentListener)
		{
			((IntentListener)f).onNewIntent(intent);
		}
	}
}
