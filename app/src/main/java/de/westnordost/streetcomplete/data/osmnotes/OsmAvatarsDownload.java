package de.westnordost.streetcomplete.data.osmnotes;

import android.annotation.SuppressLint;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import de.westnordost.osmapi.user.UserDao;
import de.westnordost.osmapi.user.UserInfo;

public class OsmAvatarsDownload
{
	private final UserDao userDao;
	private final File cacheDir;

	@Inject public OsmAvatarsDownload(UserDao userDao, File cacheDir)
	{
		this.userDao = userDao;
		this.cacheDir = cacheDir;
	}

	public void download(Collection<Long> userIds)
	{
		final Map<Long,String> userAvatars = downloadUserAvatarUrls(userIds);

		downloadUserAvatars(userAvatars);
	}

	private void downloadUserAvatars(Map<Long,String> userAvatars)
	{
		if(!cacheDir.exists() && !cacheDir.mkdirs())
		{
			Log.w("OsmAvatarsDownload", "Unable to create directories for avatars");
			return;
		}

		for (Map.Entry<Long, String> userAvatar : userAvatars.entrySet())
		{
			long userId = userAvatar.getKey();
			String avatarUrl = userAvatar.getValue();
			try(InputStream is = new BufferedInputStream(new URL(avatarUrl).openStream()))
			{
				File avatarFile = new File(cacheDir, "" + userId);
				try(FileOutputStream fout = new FileOutputStream(avatarFile))
				{
					int count;
					final byte data[] = new byte[1024];
					while ((count = is.read(data, 0, 1024)) != -1) fout.write(data, 0, count);
				}
				Log.i("OsmAvatarsDownload", "Saved file: "+avatarFile.getPath());
			}
			catch (IOException e)
			{
				Log.w("OsmAvatarsDownload", "Unable to download avatar for user id " + userId);
			}
		}
	}

	private Map<Long,String> downloadUserAvatarUrls(Collection<Long> userIds)
	{
		@SuppressLint("UseSparseArrays")
		final Map<Long,String> userAvatars = new HashMap<>();
		for (Long userId : userIds)
		{
			UserInfo userInfo = userDao.get(userId);
			if(userInfo != null && userInfo.profileImageUrl != null)
			{
				userAvatars.put(userId, userInfo.profileImageUrl);
			}
		}
		return userAvatars;
	}
}
