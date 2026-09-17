-keepattributes Signature
-keep class com.google.gson.reflect.** { *; }
-keep class com.google.gson.internal.** { *; }

# If you are using Gson TypeAdapter you must keep the type adapter and its raw type.
-keep class com.google.gson.TypeAdapter
-keep class com.google.gson.TypeAdapterFactory

# Retain generic signatures of TypeToken and its subclasses with R8 version 3.0 and higher.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

-keep class com.ahmed.tsoup.TorrentItems { *; }
-keep class com.ahmed.tsoup.TorrentItems$* { *; }
