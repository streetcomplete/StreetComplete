package de.westnordost.streetcomplete.sound;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.provider.Settings;
import android.support.annotation.RawRes;
import android.util.SparseIntArray;

import javax.inject.Inject;

public class SoundFx
{
	private final Context context;
	private final SoundPool sounds;
	private final SparseIntArray soundIds;

	@Inject public SoundFx(Context context)
	{
		this.context = context;
		soundIds = new SparseIntArray();
		sounds = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
	}

	public void prepare(@RawRes int resId)
	{
		soundIds.put(resId, sounds.load(context, resId, 1));
	}

	public void play(@RawRes int resId)
	{
		boolean isTouchSoundsEnabled = Settings.System.getInt(context.getContentResolver(),
			Settings.System.SOUND_EFFECTS_ENABLED, 1) != 0;
		if(isTouchSoundsEnabled)
		{
			if (soundIds.get(resId) == 0) prepare(resId);
			sounds.play(soundIds.get(resId), 1, 1, 1, 0, 1);
		}
	}
}
