## Contents

* [Why Anko Layouts?](#why-anko-layouts)
	* [Why DSL?](#why-dsl)
	* [Supporting existing code](#supporting-existing-code)
	* [How it works](#how-it-works)
	* [Is it extensible?](#is-it-extensible)
* [Using Anko Layouts in your project](#using-anko-layouts-in-your-project)
* [Understanding Anko](#understanding-anko)
	* [Basics](#basics)
    * [AnkoComponent](#ankocomponent)
	* [Helper blocks](#helper-blocks)
    * [Themed blocks](#themed-blocks)
	* [Layouts and LayoutParams](#layouts-and-layoutparams)
	* [Listeners](#listeners)
    * [Custom coroutine context](#custom-coroutine-context)
	* [Using resource identifiers](#using-resource-identifiers)
	* [Instance shorthand notation](#instance-shorthand-notation)
	* [UI wrapper](#ui-wrapper)
	* [Include tag](#include-tag)
* [Anko Support Plugin](#anko-support-plugin)
    * [Installing Anko Support plugin](#installing-anko-support-plugin)
    * [Using the plugin](#using-the-plugin)
    * [XML to DSL Converter](#xml-to-dsl-converter)

## Why Anko Layouts?

### Why DSL?

By default, UI in Android is written using XML. That is inconvenient in the following ways:

* It is not typesafe;
* It is not null-safe;
* It forces you to write almost *the same code* for every layout you make;
* XML is parsed on the device wasting CPU time and battery;
* Most of all, it allows no code reuse.

While you can create UI programmatically, it's hardly done because it's somewhat ugly and hard to maintain. Here's a plain Kotlin version (one in Java is even longer):

```kotlin
val act = this
val layout = LinearLayout(act)
layout.orientation = LinearLayout.VERTICAL
val name = EditText(act)
val button = Button(act)
button.text = "Say Hello"
button.setOnClickListener {
    Toast.makeText(act, "Hello, ${name.text}!", Toast.LENGTH_SHORT).show()
}
layout.addView(name)
layout.addView(button)
```

A DSL makes the same logic easy to read, easy to write and there is no runtime overhead. Here it is again:

```kotlin
verticalLayout {
    val name = editText()
    button("Say Hello") {
        onClick { toast("Hello, ${name.text}!") }
    }
}
```

Note that `onClick()` supports coroutines (accepts suspending lambda) so you can write your asynchronous code without explicit `async(UI)` call.

### Supporting existing code

You don't have to rewrite all your UI with Anko. You can keep your old classes written in Java.
Moreover, if you still want (or have) to write a Kotlin activity class and inflate an XML layout for some reason, you can use View properties, which would make things easier:

```kotlin
// Same as findViewById() but simpler to use
val name = find<TextView>(R.id.name)
name.hint = "Enter your name"
name.onClick { /*do something*/ }
```

You can make your code even more compact by using [Kotlin Android Extensions](https://kotlinlang.org/docs/tutorials/android-plugin.html).

### How it works

There is no :tophat:. Anko consists of some Kotlin [extension functions and properties](http://kotlinlang.org/docs/reference/extensions.html) arranged into *type-safe builders*, as described [under Type Safe Builders](http://kotlinlang.org/docs/reference/type-safe-builders.html).

Since it's somewhat tedious to write all these extensions by hand, they're generated automatically using *android.jar* files from Android SDK as sources.

### Is it extensible?

Short answer: **yes**.

For example, you might want to use a `MapView` in the DSL. Then just write this in any Kotlin file from where you could import it:

```kotlin
inline fun ViewManager.mapView() = mapView(theme = 0) {}

inline fun ViewManager.mapView(init: MapView.() -> Unit): MapView {
    return ankoView({ MapView(it) }, theme = 0, init)
}
```

``{ MapView(it) }`` is a factory function for your custom `View`. It accepts a `Context` instance.

So now you can write this:

```kotlin
frameLayout {
    val mapView = mapView().lparams(width = matchParent)
}
```

If you want your users to be able to apply a custom theme, write also this:

```kotlin
inline fun ViewManager.mapView(theme: Int = 0) = mapView(theme) {}

inline fun ViewManager.mapView(theme: Int = 0, init: MapView.() -> Unit): MapView {
    return ankoView({ MapView(it) }, theme, init)
}
```

## Using Anko Layouts in your project

Include these library dependencies:

```gradle
dependencies {
    // Anko Layouts
    compile "org.jetbrains.anko:anko-sdk25:$anko_version" // sdk15, sdk19, sdk21, sdk23 are also available
    compile "org.jetbrains.anko:anko-appcompat-v7:$anko_version"

    // Coroutine listeners for Anko Layouts
    compile "org.jetbrains.anko:anko-sdk25-coroutines:$anko_version"
    compile "org.jetbrains.anko:anko-appcompat-v7-coroutines:$anko_version"
}
```

Please read the [Gradle-based project](https://github.com/Kotlin/anko#gradle-based-project) section for the detailed information.

## Understanding Anko

### Basics

In Anko, you don't need to inherit from any special classes: just use standard `Activity`, `Fragment`, `FragmentActivity` or whatever you want.

First of all, import `org.jetbrains.anko.*` to use Anko Layouts DSL in your classes.

DSL is available in `onCreate()`:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    verticalLayout {
        padding = dip(30)
        editText {
            hint = "Name"
            textSize = 24f
        }
        editText {
            hint = "Password"
            textSize = 24f
        }
        button("Login") {
            textSize = 26f
        }
    }
}
```

<table>
<tr><td width="50px" align="center">:penguin:</td>
<td>
<i>There's no explicit call to <code>setContentView(R.layout.something)</code>: Anko sets content views automatically for <code>Activities</code> (but only for them).</i>
</td>
</tr>
</table>

`hint` and `textSize` are [synthetic extension properties](https://kotlinlang.org/docs/reference/java-interop.html#getters-and-setters) bound to JavaBean-style getters and setters, `padding` is an [extension property](http://kotlinlang.org/docs/reference/extensions.html#extension-properties) from Anko. Either of these exists for almost all `View` attributes allowing you to write `text = "Some text"` instead of `setText("Some text")`.

`verticalLayout` (a `LinearLayout` but already with a `LinearLayout.VERTICAL` orientation), `editText` and `button` are [extension functions](http://kotlinlang.org/docs/reference/extensions.html) that construct the new `View` instances and add them to the parent. We will reference such functions as *blocks*.

Blocks exist for almost every `View` in Android framework, and they work in `Activities`, `Fragments` (both default and that from `android.support` package) and even for `Context`. For example, if you have an `AnkoContext` instance, you can write blocks like this:

```kotlin
val name: EditText = with(ankoContext) {
    editText {
        hint = "Name"
    }
}
```

### AnkoComponent

Although you can use the DSL directly (in `onCreate()` or everywhere else), without creating any extra classes, it is often convenient to have UI in the separate class. If you use the provided `AnkoComponent` interface, you also you get a DSL [layout preview](doc/PREVIEW.md) feature for free.

```kotlin
class MyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        MyActivityUI().setContentView(this)
    }
}

class MyActivityUI : AnkoComponent<MyActivity> {
    override fun createView(ui: AnkoContext<MyActivity>) = with(ui) {
        verticalLayout {
            val name = editText()
            button("Say Hello") {
                onClick { ctx.toast("Hello, ${name.text}!") }
            }
        }
    }
}
```

### Helper blocks

As you probably noticed earlier, the `button()` function in the previous section accepts a `String` parameter. Such helper blocks exist for the frequently used views such as `TextView`, `EditText`, `Button` or `ImageView`.

If you don't need to set any properties for some particular `View`, you can omit `{}` and write `button("Ok")` or even just `button()`:

```kotlin
verticalLayout {
    button("Ok")
    button(R.string.cancel)
}
```

### Themed blocks

Anko provides "themeable" versions of blocks, including helper blocks:

```kotlin
verticalLayout {
    themedButton("Ok", theme = R.style.myTheme)
}
```

### Layouts and `LayoutParams`

The positioning of widgets inside parent containers can be tuned using `LayoutParams`. In XML it looks like this:

```xml
<ImageView 
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_marginLeft="5dip"
    android:layout_marginTop="10dip"
    android:src="@drawable/something" />
```

In Anko, you specify `LayoutParams` right after a `View` description using `lparams()`:

```kotlin
linearLayout {
    button("Login") {
        textSize = 26f
    }.lparams(width = wrapContent) {
        horizontalMargin = dip(5)
        topMargin = dip(10)
    }
}
```

If you specify `lparams()`, but omit `width` and/or `height`, their default values are both `wrapContent`. But you always can pass them explicitly: use [named arguments](http://kotlinlang.org/docs/reference/functions.html#named-arguments).

Some convenient helper properties to notice:

- `horizontalMargin` sets both left and right margins, 
- `verticalMargin` set top and bottom ones, and 
- `margin` sets all four margins simultaneously.

Note that `lparams()` are different for different layouts, for example, in the case of `RelativeLayout`:

```kotlin
val ID_OK = 1

relativeLayout {
    button("Ok") {
        id = ID_OK
    }.lparams { alignParentTop() }
  
    button("Cancel").lparams { below(ID_OK) }
}
```

### Listeners

Anko has listener helpers that seamlessly support coroutines. You can write asynchronous code right inside your listeners!

```kotlin
button("Login") {
    onClick {
    	val user = myRetrofitService.getUser().await()
        showUser(user)
    }
}
```

It is nearly the same as this:

```kotlin
button.setOnClickListener(object : OnClickListener {
    override fun onClick(v: View) {
    	launch(UI) {
    	    val user = myRetrofitService.getUser().await()
            showUser(user)
    	}
    }
})
```

Anko is very helpful when you have listeners with lots of methods. Consider the following code written without using Anko:

```kotlin
seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        // Something
    }
    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        // Just an empty method
    }
    override fun onStopTrackingTouch(seekBar: SeekBar) {
        // Another empty method
    }
})
```

And now with Anko:

```kotlin
seekBar {
    onSeekBarChangeListener {
        onProgressChanged { seekBar, progress, fromUser ->
            // Something
        }
    }
}
```

If you set `onProgressChanged()` and `onStartTrackingTouch()` for the same `View`, these two "partially defined" listeners will be merged. For the same listener method, the last one wins.

### Custom coroutine context

You can pass a custom coroutine context to the listener helpers:

```kotlin
button("Login") {
    onClick(yourContext) {
    	val user = myRetrofitService.getUser().await()
        showUser(user)
    }
}
```

### Using resource identifiers

All examples in the previous chapters used raw Java strings, but it is hardly a good practice. Typically you put all your string data into `res/values/` directory and access it at runtime calling, for example, `getString(R.string.login)`.

Fortunately, in Anko you can pass resource identifiers both to helper blocks (`button(R.string.login)`) and to extension properties (`button { textResource = R.string.login }`).

Note that the property name is not the same: instead of `text`, `hint`, `image`, we now use `textResource`, `hintResource` and `imageResource`.

<table>
<tr><td width="50px" align="center">:penguin:</td>
<td>
<i>Resource properties always throw <code>AnkoException</code> when read.</i>
</td>
</tr>
</table>

### Instance shorthand notation

Sometimes you need to pass a `Context` instance to some Android SDK method from your `Activity` code.
Usually, you can just use `this`, but what if you're inside an inner class? You would probably write `SomeActivity.this` in case of Java
and `this@SomeActivity` if you're writing in Kotlin.

With Anko you can just write `ctx`. It is an extension property which works both inside `Activity` and `Service` and is even
accessible from `Fragment` (it uses `getActivity()` method under the hood). You can also get an `Activity` instance using `act` extension property.

### UI wrapper

Before the Beginning of Time Anko always used `UI` tag as a top-level DSL element:

```kotlin
UI {
    editText {
        hint = "Name"
    }
}
```

You can still use this tag if you want. And it would be much easier to extend DSL as you have to declare only one `ViewManager.customView` function.
See [Extending Anko](doc/ADVANCED.md#extending-anko) for more information.

### Include tag

It is easy to insert an XML layout into DSL. Use the `include()` function:

```kotlin
include<View>(R.layout.something) {
    backgroundColor = Color.RED
}.lparams(width = matchParent) { margin = dip(12) }
```

You can use `lparams()` as usual, and if you provide a specific type instead of `View`, you can also use this type inside `{}`:

```kotlin
include<TextView>(R.layout.textfield) {
    text = "Hello, world!"
}
```

## Anko Support plugin

Anko Support plugin is available for IntelliJ IDEA and Android Studio. It allows you to preview `AnkoComponent` classes written with Anko directly in the IDE tool window.

<table>
<tr><td width="50px" align="center">:warning:</td>
<td>
<i>The Anko Support plugin is currently supported only in Android Studio 2.4+.</i>
</td>
</tr>
</table>

### Installing Anko Support plugin

You can download the Anko Support plugin [here](https://plugins.jetbrains.com/update/index?pr=&updateId=19242).

### Using the plugin

Suppose you have these classes written with Anko:

```kotlin
class MyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        MyActivityUI().setContentView(this)
    }
}

class MyActivityUI : AnkoComponent<MyActivity> {
    override fun createView(ui: AnkoContext<MyActivity>) = ui.apply {
        verticalLayout {
            val name = editText()
            button("Say Hello") {
                onClick { ctx.toast("Hello, ${name.text}!") }
            }
        }
    }.view
}
```

Put the cursor somewhere inside the `MyActivityUI` declaration, open the *Anko Layout Preview* tool window ("View" → "Tool Windows" → "Anko Layout Preview") and press *Refresh*.

This requires building the project, so it could take some time before the image is actually shown.

### XML to DSL Converter

The plugin also supports converting layouts from the XML format to Anko Layouts code. Open an XML file and select "Code" → "Convert to Anko Layouts DSL". You can convert several XML layout files simultaneously.