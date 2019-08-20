SET app=de.westnordost.streetcomplete.debug
SET ANDROID_SDK_ROOT=C:\Android\sdk
%ANDROID_SDK_ROOT%\platform-tools\adb.exe shell run-as %app% chmod 777 /data/data/%app%/databases/
%ANDROID_SDK_ROOT%\platform-tools\adb.exe shell run-as %app% chmod 777 /data/data/%app%/databases/streetcomplete_test.db
%ANDROID_SDK_ROOT%\platform-tools\adb.exe shell cp /data/data/%app%/databases/streetcomplete_test.db /sdcard/
%ANDROID_SDK_ROOT%\platform-tools\adb.exe pull /sdcard/streetcomplete_test.db streetcomplete_test.db
"C:\Program Files\DB Browser for SQLite\DB Browser for SQLite.exe" streetcomplete_test.db
del streetcomplete_test.db