%ANDROID_SDK_ROOT%\platform-tools\adb.exe shell run-as de.westnordost.streetcomplete chmod 777 /data/data/de.westnordost.streetcomplete/files/
%ANDROID_SDK_ROOT%\platform-tools\adb.exe shell run-as de.westnordost.streetcomplete chmod 777 /data/data/de.westnordost.streetcomplete/files/quests.png
%ANDROID_SDK_ROOT%\platform-tools\adb.exe shell run-as de.westnordost.streetcomplete chmod 777 /data/data/de.westnordost.streetcomplete/files/streetcomplete_quests.yaml
%ANDROID_SDK_ROOT%\platform-tools\adb.exe shell cp /data/data/de.westnordost.streetcomplete/files/quests.png /sdcard/
%ANDROID_SDK_ROOT%\platform-tools\adb.exe shell cp /data/data/de.westnordost.streetcomplete/files/streetcomplete_quests.yaml /sdcard/
%ANDROID_SDK_ROOT%\platform-tools\adb.exe pull /sdcard/quests.png ../app/src/main/assets/quests.png
%ANDROID_SDK_ROOT%\platform-tools\adb.exe pull /sdcard/streetcomplete_quests.yaml ../app/src/main/assets/streetcomplete_quests.yaml