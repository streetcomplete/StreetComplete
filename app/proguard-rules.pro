-dontobfuscate

# tangram start ------------------------------------------------------------------------------------

# let's just keep everything
-keep class com.mapzen.tangram.** { *; }
-keep class com.mapzen.tangram.* { *; }

# tangram end --------------------------------------------------------------------------------------

# Lifecycle
-keep public class androidx.lifecycle.* {
    public protected *;
}
-keepclassmembers class * {
    @androidx.lifecycle.OnLifecycleEvent public *;
}

# just leave my stuff alone
-keep class de.westnordost.* { *; }
-keep class de.westnordost.** { *; }

# see https://github.com/westnordost/StreetComplete/issues/2003
-keepclassmembers class * implements android.os.Parcelable {
        public static final ** CREATOR;
}

# kotlinx-serialization start ----------------------------------------------------------------------

-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt # core serialization annotations

# kotlinx-serialization-json specific. Add this if you have java.lang.NoClassDefFoundError kotlinx.serialization.json.JsonObjectSerializer
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Change here com.yourcompany.yourpackage
-keep,includedescriptorclasses class de.westnordost.streetcomplete.**$$serializer { *; }
-keepclassmembers class de.westnordost.streetcomplete.** {
    *** Companion;
}
-keepclasseswithmembers class de.westnordost.streetcomplete.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# kotlinx-serialization end ------------------------------------------------------------------------

# after removing tangram, this must be added or build will fail
# but it's just dontwarn, wtf?
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE
