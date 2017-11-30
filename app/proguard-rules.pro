-dontobfuscate

-dontwarn org.xmlpull.**
-dontnote org.xmlpull.**

# https://issuetracker.google.com/issues/37070898
-dontnote android.net.http.*
-dontnote org.apache.commons.codec.**
-dontnote org.apache.http.**

# JTS
# (we don't use the AWT part of JTS)
-dontwarn java.awt.**

# tangram
# let's just keep everything
-keep class com.mapzen.tangram.** { *; }

# kryo
-dontwarn java.beans.**
-dontwarn sun.nio.ch.**
-dontwarn sun.misc.**
-dontwarn java.lang.invoke.SerializedLambda
# let's just keep everything
-keep class com.esotericsoftware.kryo.** { *; }

# evernote/android-job
-dontwarn com.evernote.android.job.gcm.**
-dontwarn com.evernote.android.job.GcmAvailableHelper

-keep public class com.evernote.android.job.v21.PlatformJobService
-keep public class com.evernote.android.job.v14.PlatformAlarmService
-keep public class com.evernote.android.job.v14.PlatformAlarmReceiver
-keep public class com.evernote.android.job.JobBootReceiver
-keep public class com.evernote.android.job.JobRescheduleService