%ANDROID_SDK_ROOT%\platform-tools\adb.exe shell run-as de.westnordost.streetcomplete chmod 777 /data/data/de.westnordost.streetcomplete/files/
%ANDROID_SDK_ROOT%\platform-tools\adb.exe shell run-as de.westnordost.streetcomplete chmod 777 /data/data/de.westnordost.streetcomplete/files/quests.png
%ANDROID_SDK_ROOT%\platform-tools\adb.exe shell cp /data/data/de.westnordost.streetcomplete/files/quests.png /sdcard/
%ANDROID_SDK_ROOT%\platform-tools\adb.exe pull /sdcard/quests.png quests.png