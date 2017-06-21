D:\Android\sdk\platform-tools\adb.exe shell run-as de.westnordost.streetcomplete chmod 777 /data/data/de.westnordost.streetcomplete/databases/
D:\Android\sdk\platform-tools\adb.exe shell run-as de.westnordost.streetcomplete chmod 777 /data/data/de.westnordost.streetcomplete/databases/test_streetcomplete.db
D:\Android\sdk\platform-tools\adb.exe shell cp /data/data/de.westnordost.streetcomplete/databases/test_streetcomplete.db /sdcard/
D:\Android\sdk\platform-tools\adb.exe pull /sdcard/test_streetcomplete.db test_streetcomplete.db
"C:\Program Files\DB Browser for SQLite\3.9.0\bin\DB Browser for SQLite.exe" test_streetcomplete.db
del test_streetcomplete.db