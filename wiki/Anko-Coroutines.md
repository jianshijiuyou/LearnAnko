## Contents

* [在你的项目中添加依赖](#在你的项目中添加依赖)
* [`asReference()`](#asreference)
* [`bg()`](#bg)
* [本篇文章相关代码传送门](#本篇文章相关代码传送门)

## 在你的项目中添加依赖

```groovy
dependencies {
    compile "org.jetbrains.anko:anko-coroutines:$anko_version"
}
```

## `asReference()`

如果你的异步 API 长时间不返回结果， 你的协同程序会一直处于『挂起状态』，作为协同程序持有对象的强引用，比如持有 activity 或者 fragment 的实例引用可能会导致内存泄漏（memory leak）。

在这种情况下应该使用 `asReference()` 而不是直接持有实例对象：

```kotlin
class CoroutinesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_basics)
        //持有 Activity 对象的 弱引用（WeakReference）
        val ref: Ref<CoroutinesActivity> = this.asReference()

        async(UI) {
            //耗时操作
            val data = getData()
            //用 ref 替换 this@Activity
            ref().showData(data)
        }
    }

    fun showData(data: String) {
        toast(data)
    }

    suspend fun getData(): String {
        delay(3000L)
        return "数据"
    }
}
```

## `bg()`

`bg()` 可以轻松的在后台执行并返回结果，看上去就像同步代码一样（远离回调地狱）：

```kotlin
async(UI) {
    val data = bg {
        //子线程
        SystemClock.sleep(3000)
        return@bg "网络数据"
    }
    //UI 线程
    ref().showData(data.await())
}
```

### 推荐阅读
 - [深入理解 Kotlin Coroutine](http://www.kotliner.cn/2017/01/30/%E6%B7%B1%E5%85%A5%E7%90%86%E8%A7%A3%20Kotlin%20Coroutine/)
 - [深入理解 Kotlin Coroutine (2)](http://www.kotliner.cn/2017/02/06/%E6%B7%B1%E5%85%A5%E7%90%86%E8%A7%A3%20Kotlin%20Coroutine%20(2)/)
 - [深入理解 Kotlin 协程 Coroutine（3）](http://www.kotliner.cn/2017/06/19/deep-in-coroutine-III/)

### 本篇文章相关代码传送门
[CoroutinesActivity.kt](https://github.com/jianshijiuyou/LearnAnko/blob/master/app/src/main/java/info/jiuyou/learnanko/coroutines/CoroutinesActivity.kt)
