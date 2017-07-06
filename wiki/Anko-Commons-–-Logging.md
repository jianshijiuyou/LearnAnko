## 概要

* [在你的项目中添加依赖](#在你的项目中添加依赖)
* [Trait-like style（继承模式）](#trait-like-style)
* [Logger object style （对象模式）](#logger-object-style)
* [本篇文章相关代码传送门](#本篇文章相关代码传送门)

## 在你的项目中添加依赖

```groovy
dependencies {
    compile "org.jetbrains.anko:anko-commons:$anko_version"
}
```

## Trait-like style

让需要 log 的 class 继承 AnkoLogger ，这样在 class 中就能方便的使用 log 了，这种方式下默认的 TAG 为 "LoggingActivity",也就是 AnkoLogger 所继承的类的类名。

```kotlin
class LoggingActivity : AppCompatActivity(), AnkoLogger {
    override fun onCreate(savedInstanceState: Bundle?) {

        、、、、、、

        info("info log")
        warn(null) // 打印 "null"
        //这里debug打印不出来，因为内部调用了Log.isLoggable方法，具体自行google
        debug("debug log")

    }
}

//=============输出结果================
//  I/LoggingActivity: info log
//  W/LoggingActivity: null
```

#### 方法对照表

android.util.Log  | AnkoLogger
------------------|------------
`v()`             | `verbose()`
`d()`             | `debug()`
`i()`             | `info()`
`w()`             | `warn()`
`e()`             | `error()`
`wtf()`           | `wtf()`

使用方式有两种: plain 和 lazy (inlined):

```kotlin
info("String " + "concatenation")
info { "String " + "concatenation" }
```

AnkoLogger 的 log 方法 内部都会去调用  `Log.isLoggable()` ，当返回 `true` 的时候 log 才会被打印，这和以前直接使用 `Log.d()` 等方法打印不同。

## Logger object style

把 AnkoLogger 当作一个普通对象使用。

```kotlin
class LoggingActivity : AppCompatActivity(), AnkoLogger {
    private val log = AnkoLogger(this::class.java)
    private val logCustomTag = AnkoLogger("my_tag")
    override fun onCreate(savedInstanceState: Bundle?) {

        、、、、、

        log.info{"info log"}
        log.warn(null)
        logCustomTag.info("info log")
        logCustomTag.warn(null)
    }
}
//===============输出结果=================
//  I/LoggingActivity: info log
//  W/LoggingActivity: null
//  I/my_tag: info log
//  W/my_tag: null
```
### 本篇文章相关代码传送门
[LoggingActivity.kt](https://github.com/jianshijiuyou/LearnAnko/blob/master/app/src/main/java/info/jiuyou/learnanko/commons/LoggingActivity.kt)
