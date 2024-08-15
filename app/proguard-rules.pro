-dontobfuscate

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

# crashes when selecting some quests from tag editor
-keep class androidx.core.app.CoreComponentFactory { *; }

# crashes on start after upgrading to gradle 8 (release version only for some reason, though same rules are used)
-keepclassmembers public class io.requery.android.database.sqlite.SQLiteConnection { *; }

# after upgrading to gradle 8, stack traces contain "unknown source", which is horribly bad making them rather useless
-keepattributes SourceFile,LineNumberTable

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

# ktor client, see https://youtrack.jetbrains.com/issue/KTOR-5528
-dontwarn org.slf4j.impl.StaticLoggerBinder

# Change here com.yourcompany.yourpackage
-keep,includedescriptorclasses class de.westnordost.streetcomplete.**$$serializer { *; }
-keepclassmembers class de.westnordost.streetcomplete.** {
    *** Companion;
}
-keepclasseswithmembers class de.westnordost.streetcomplete.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# kotlinx-serialization end ------------------------------------------------------------------------
