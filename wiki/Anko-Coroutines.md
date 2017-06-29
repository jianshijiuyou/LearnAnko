## Contents

* [Using Anko Coroutines in your project](#using-anko-coroutines-in-your-project)
* [Listener helpers](#listener-helpers)
* [`asReference()`](#asreference)
* [`bg()`](#bg)

## Using Anko Coroutines in your project

Add the `anko-coroutines` dependency to your `build.gradle`:

```groovy
dependencies {
    compile "org.jetbrains.anko:anko-coroutines:$anko_version"
}
```

## Listener helpers

## `asReference()`

If your asynchronous API does not support cancellation, your coroutine may be suspended for an indefinite time period. As a coroutine holds the strong references to captured objects, capturing the instance of `Activity` or `Fragment` instance may cause a memory leak.

Use `asReference()` in such cases instead of the direct capturing:

```kotlin
suspend fun getData(): Data { ... }

class MyActivity : Activity() {
    fun loadAndShowData() {
	// Ref<T> uses the WeakReference under the hood
	val ref: Ref<MyActivity> = this.asReference()

	async(UI) {
	    val data = getData()
			
	    // Use ref() instead of this@MyActivity
	    ref().showData()
	}
    }

    fun showData(data: Data) { ... }
}
```

## `bg()`

You can easily execute your code on the background thread using `bg()`:

```kotlin
fun getData(): Data { ... }
fun showData(data: Data) { ... }

async(UI) {
    val data: Deferred<Data> = bg {
	// Runs in background
	getData()
    }

    // This code is executed on the UI thread
    showData(data.await())
}
```