## Contents

* [Using `AnkoLogger` in your project](#using-ankologger-in-your-project)
* [Trait-like style](#trait-like-style)
* [Logger object style](#logger-object-style)

## Using `AnkoLogger` in your project

`AnkoLogger` is inside the `anko-commons` artifact. Add it as a dependency to your `build.gradle`:

```groovy
dependencies {
    compile "org.jetbrains.anko:anko-commons:$anko_version"
}
```

## Trait-like style

Android SDK provides [`android.util.Log`](https://developer.android.com/reference/android/util/Log.html) class with some logging methods. Usage is pretty straightforward though the methods require you to pass a `tag` argument. You can eliminate this with using `AnkoLogger` trait-like interface:

```kotlin
class SomeActivity : Activity(), AnkoLogger {
    private fun someMethod() {
        info("London is the capital of Great Britain")
        debug(5) // .toString() method will be executed
        warn(null) // "null" will be printed
    }
}
```

android.util.Log  | AnkoLogger
------------------|------------
`v()`             | `verbose()`
`d()`             | `debug()`
`i()`             | `info()`
`w()`             | `warn()`
`e()`             | `error()`
`wtf()`           | `wtf()`

The default tag name is a class name (`SomeActivity` in this case) but you can easily change it by overriding the `loggerTag` property.

Each method has two versions: plain and lazy (inlined):

```kotlin
info("String " + "concatenation")
info { "String " + "concatenation" }
```

Lambda result will be calculated only if `Log.isLoggable(tag, Log.INFO)` is `true`.

## Logger object style

You can also use `AnkoLogger` as a plain object.

```kotlin
class SomeActivity : Activity() {
    private val log = AnkoLogger<SomeActivity>(this)
    private val logWithASpecificTag = AnkoLogger("my_tag")

    private fun someMethod() {
        log.warning("Big brother is watching you!")
    }
}
```