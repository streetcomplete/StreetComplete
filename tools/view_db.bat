%ANDROID_SDK_ROOT%\platform-tools\adb.exe shell run-as de.westnordost.streetcomplete chmod 777 /data/data/de.westnordost.streetcomplete/databases/
%ANDROID_SDK_ROOT%\platform-tools\adb.exe shell run-as de.westnordost.streetcomplete chmod 777 /data/data/de.westnordost.streetcomplete/databases/streetcomplete.db
%ANDROID_SDK_ROOT%\platform-tools\adb.exe shell cp /data/data/de.westnordost.streetcomplete/databases/streetcomplete.db /sdcard/
%ANDROID_SDK_ROOT%\platform-tools\adb.exe pull /sdcard/streetcomplete.db streetcomplete.db
"C:\Program Files\DB Browser for SQLite\DB Browser for SQLite.exe" streetcomplete.db
del streetcomplete.db