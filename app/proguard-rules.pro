-dontobfuscate

-dontwarn org.xmlpull.**
-dontnote org.xmlpull.**

# https://issuetracker.google.com/issues/37070898
-dontnote android.net.http.*
-dontnote org.apache.commons.codec.**
-dontnote org.apache.http.**

# tangram
# let's just keep everything
-keep class com.mapzen.tangram.** { *; }
-keep class com.mapzen.tangram.* { *; }

# kryo
-dontwarn java.beans.**
-dontwarn sun.nio.ch.**
-dontwarn sun.misc.**
-dontwarn java.lang.invoke.SerializedLambda
# let's just keep everything
-keep class * implements java.io.Serializable { *; }
-keep class com.esotericsoftware.kryo.** { *; }
-keep class com.esotericsoftware.kryo.* { *; }

# Lifecycle
-keep public class androidx.lifecycle.* {
    public protected *;
}
-keepclassmembers class * {
    @androidx.lifecycle.OnLifecycleEvent public *;
}

# CountryInfo filled via reflection
-keep class de.westnordost.streetcomplete.data.meta.CountryInfo { *; }

# just leave my stuff alone
-keep class de.westnordost.* { *; }
-keep class de.westnordost.** { *; }

# see https://github.com/westnordost/StreetComplete/issues/2003
-keepclassmembers class * implements android.os.Parcelable {
        public static final ** CREATOR;
}
