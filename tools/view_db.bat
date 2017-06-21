D:\Android\sdk\platform-tools\adb.exe shell run-as de.westnordost.streetcomplete chmod 777 /data/data/de.westnordost.streetcomplete/databases/
D:\Android\sdk\platform-tools\adb.exe shell run-as de.westnordost.streetcomplete chmod 777 /data/data/de.westnordost.streetcomplete/databases/streetcomplete.db
D:\Android\sdk\platform-tools\adb.exe shell cp /data/data/de.westnordost.streetcomplete/databases/streetcomplete.db /sdcard/
D:\Android\sdk\platform-tools\adb.exe pull /sdcard/streetcomplete.db streetcomplete.db
"C:\Program Files\DB Browser for SQLite\3.9.0\bin\DB Browser for SQLite.exe" streetcomplete.db
del streetcomplete.db