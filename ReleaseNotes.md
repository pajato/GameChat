# Release b1.11

## Bug fixes

Fix the bug whereby a pawn on column position 7 (base 1) in any row was not able to take a piece to the right.

# Release b1.10

## Bug fixes

- Code shrinking bug

Fixed a code shrinking bug whereby a new Gradle plugin apparently surfaced or introduced a fatal exception with the following stack trace:

<pre><code>
 Process: com.pajato.android.gamechat, PID: 6168
   java.lang.AssertionError: impossible
       at java.lang.Enum$1.create(Enum.java:269)
       at java.lang.Enum$1.create(Enum.java:260)
       at libcore.util.BasicLruCache.get(BasicLruCache.java:58)
       at java.lang.Enum.getSharedConstants(Enum.java:286)
       at java.lang.Class.getEnumConstantsShared(Class.java:2291)
       at java.lang.JavaLangAccess.getEnumConstantsShared(JavaLangAccess.java:40)
       at java.util.EnumSet.getUniverse(EnumSet.java:389)
       at java.util.EnumSet.noneOf(EnumSet.java:107)
       at java.util.EnumSet.allOf(EnumSet.java:126)
       at com.facebook.internal.SmartLoginOption.<clinit>(SmartLoginOption.java:29)
       at com.facebook.internal.SmartLoginOption.parseOptions(SmartLoginOption.java:31)
       at com.facebook.internal.FetchedAppSettingsManager.parseAppSettingsFromJSON(FetchedAppSettingsManager.java:184)
       at com.facebook.internal.FetchedAppSettingsManager.access$000(FetchedAppSettingsManager.java:47)
       at com.facebook.internal.FetchedAppSettingsManager$1.run(FetchedAppSettingsManager.java:124)
       at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1133)
       at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:607)
       at java.lang.Thread.run(Thread.java:761)
    Caused by: java.lang.NoSuchMethodException: values []
       at java.lang.Class.getMethod(Class.java:1981)
       at java.lang.Class.getDeclaredMethod(Class.java:1960)
       at java.lang.Enum$1.create(Enum.java:265)
       at java.lang.Enum$1.create(Enum.java:260)
       at libcore.util.BasicLruCache.get(BasicLruCache.java:58)
       at java.lang.Enum.getSharedConstants(Enum.java:286)
       at java.lang.Class.getEnumConstantsShared(Class.java:2291)
       at java.lang.JavaLangAccess.getEnumConstantsShared(JavaLangAccess.java:40)
       at java.util.EnumSet.getUniverse(EnumSet.java:389)
       at java.util.EnumSet.noneOf(EnumSet.java:107)
       at java.util.EnumSet.allOf(EnumSet.java:126)
       at com.facebook.internal.SmartLoginOption.<clinit>(SmartLoginOption.java:29)
       at com.facebook.internal.SmartLoginOption.parseOptions(SmartLoginOption.java:31)
       at com.facebook.internal.FetchedAppSettingsManager.parseAppSettingsFromJSON(FetchedAppSettingsManager.java:184)
       at com.facebook.internal.FetchedAppSettingsManager.access$000(FetchedAppSettingsManager.java:47)
       at com.facebook.internal.FetchedAppSettingsManager$1.run(FetchedAppSettingsManager.java:124)
       at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1133)
       at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:607)
       at java.lang.Thread.run(Thread.java:761)
</code></pre>

The problem was resolved by using a ProGuard configuration recommended in the [developer documentation](https://developer.android.com/studio/build/shrink-code.html?utm_source=android-studio#shrink-resources).

- Samsung Chromebook Plus checkerboard truncation

Fixed a bug whereby the checkerboard is truncated on a Samsung Chromebook Plus device.

# Release b1.9

## Versioned database objects

Adds support for versioned objects within the database.  Gives the developer a chance to support upward migration.

## Smaller APK size

This version dramatically cuts the size by eliminating a few large image files.

# Release b1.8

Initial Beta release.
