app=de.westnordost.streetcomplete.debug
%ANDROID_SDK_ROOT%\platform-tools\adb.exe shell run-as %app% chmod 777 /data/data/%app%/databases/
%ANDROID_SDK_ROOT%\platform-tools\adb.exe shell run-as %app% chmod 777 /data/data/%app%/databases/streetcomplete.db
%ANDROID_SDK_ROOT%\platform-tools\adb.exe shell cp /data/data/%app%/databases/streetcomplete.db /sdcard/
%ANDROID_SDK_ROOT%\platform-tools\adb.exe pull /sdcard/streetcomplete.db streetcomplete.db
"C:\Program Files\DB Browser for SQLite\DB Browser for SQLite.exe" streetcomplete.db
del streetcomplete.db