-dontobfuscate

-dontwarn org.xmlpull.**
-keep class org.xmlpull.** { *; }

# JTS
-dontwarn java.awt.**

# kryo
-dontwarn java.beans.**
-dontwarn sun.nio.ch.**
-dontwarn sun.misc.**
-dontwarn java.lang.invoke.SerializedLambda

-keep class com.esotericsoftware.kryo.** { *; }

# tangram
-keep class com.mapzen.tangram.** { *; }