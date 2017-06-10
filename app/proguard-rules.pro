# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/pmr/Projects/android/sdk/mac/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
#-dontshrink
#-dontoptimize
-dontpreverify
#-repackageclasses ''
#-allowaccessmodification
#-optimizations !code/simplification/arithmetic
-keepattributes *Annotation*
-keep class com.pajato.** {
    *;
}
-keeppackagenames com.pajato.**
-dontnote android.net.http.*
-dontnote android.support.**
-dontnote bolts.**
-dontnote com.bumptech.glide.**
-dontnote com.fasterxml.jackson.**
-dontnote com.firebase.**
-dontnote com.google.**
-dontnote com.squareup.**
-dontnote com.twitter.**
-dontnote com.facebook.**
-dontnote io.fabric.sdk.**
-dontnote okhttp3.**
-dontnote org.apache.http.**
-dontnote retrofit2.**
-dontwarn com.fasterxml.jackson.**
