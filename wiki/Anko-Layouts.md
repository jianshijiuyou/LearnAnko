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
## 为什么要用Anko布局

### Why DSL?
### 为什么要用DSL

By default, UI in Android is written using XML. That is inconvenient in the following ways:

通常情况下，Android上的UI是通过XML来写的。这有以下几点不方便的地方：

* It is not typesafe;
* 它不是类型安全的；
* It is not null-safe;
* 它不是空指针安全的，可能存在空指针错误；
* It forces you to write almost *the same code* for every layout you make;
* 它迫使您为每一个布局编写**几乎相同**的代码；
* XML is parsed on the device wasting CPU time and battery;
* 解析XML太消耗CPU资源和电量了；
* Most of all, it allows no code reuse.
* 最重要的是，它不允许代码重用。

While you can create UI programmatically, it's hardly done because it's somewhat ugly and hard to maintain. Here's a plain Kotlin version (one in Java is even longer):

虽然您可以以代码的方式创建UI，但这样会使你的代码有点难看，也难以维护。下面是一个用Kotlin写的例子（用Java写的话代码会更长）

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

DSL使相同的逻辑易于阅读，易于编写，并且不存在运行时开销。再来看以下代码：

```kotlin
verticalLayout {
    val name = editText()
    button("Say Hello") {
        onClick { toast("Hello, ${name.text}!") }
    }
}
```

Note that `onClick()` supports coroutines (accepts suspending lambda) so you can write your asynchronous code without explicit `async(UI)` call.

注意，` onclick() `支持协程(支持lambda表达式)，因此你可以不用明确的调用`async(UI)`函数。

### Supporting existing code
### 支持现有的代码

You don't have to rewrite all your UI with Anko. You can keep your old classes written in Java.
Moreover, if you still want (or have) to write a Kotlin activity class and inflate an XML layout for some reason, you can use View properties, which would make things easier:

你不必使用Anko来重写你的UI，你可以保留你原有的Java类文件。此外，如果你出于某种原因想用或者不得不用Kotlin来编写Activity和引入XML布局文件，以下的方法可以更简单：

```kotlin
// Same as findViewById() but simpler to use
val name = find<TextView>(R.id.name)
name.hint = "Enter your name"
name.onClick { /*do something*/ }
```

You can make your code even more compact by using [Kotlin Android Extensions](https://kotlinlang.org/docs/tutorials/android-plugin.html).

使用[Kotlin Android Extensions](https://kotlinlang.org/docs/tutorials/android-plugin.html)可以使你的代码更加紧凑.

### How it works
###如何工作

There is no :tophat:. Anko consists of some Kotlin [extension functions and properties](http://kotlinlang.org/docs/reference/extensions.html) arranged into *type-safe builders*, as described [under Type Safe Builders](http://kotlinlang.org/docs/reference/type-safe-builders.html).

这里没有: tophat:，Anko由一些Kotlin的*类型安全构建器*中内置的[扩展函数和属性](http://kotlinlang.org/docs/reference/extensions.html)组成，详情请看[安全构建器](http://kotlinlang.org/docs/reference/type-safe-builders.html).

Since it's somewhat tedious to write all these extensions by hand, they're generated automatically using *android.jar* files from Android SDK as sources.

由于手工编写这些扩展函数是很乏味的，所以他们是使用了Android SDK中的* android.jar *文件来自动生成的。

### Is it extensible?
### 能否继承？

Short answer: **yes**.
答案当然是：**yes**

For example, you might want to use a `MapView` in the DSL. Then just write this in any Kotlin file from where you could import it:

例如，假设你想在DSL中使用`MapView`。你只需要把下面的代码粘贴到任何你想用的Kotlin文件中即可。

```kotlin
inline fun ViewManager.mapView() = mapView(theme = 0) {}

inline fun ViewManager.mapView(init: MapView.() -> Unit): MapView {
    return ankoView({ MapView(it) }, theme = 0, init)
}
```

``{ MapView(it) }`` is a factory function for your custom `View`. It accepts a `Context` instance.

``{ MapView(it) }``是一个工厂函数，它接收一个`Context`的实例对象

So now you can write this:
因此，你可以这样写：

```kotlin
frameLayout {
    val mapView = mapView().lparams(width = matchParent)
}
```

If you want your users to be able to apply a custom theme, write also this:

如果你想要你的用户能够自定义主题，你可以这样写：

```kotlin
inline fun ViewManager.mapView(theme: Int = 0) = mapView(theme) {}

inline fun ViewManager.mapView(theme: Int = 0, init: MapView.() -> Unit): MapView {
    return ankoView({ MapView(it) }, theme, init)
}
```

## Using Anko Layouts in your project
## 在你的项目中使用Anko布局

Include these library dependencies:
导入以下依赖库：

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

想获取更多详情，请阅读[Gradle-based project](https://github.com/Kotlin/anko#gradle-based-project)章节。

## Understanding Anko
## 理解Anko

### Basics
### 基础

In Anko, you don't need to inherit from any special classes: just use standard `Activity`, `Fragment`, `FragmentActivity` or whatever you want.

在Anko中，你不需要继承其他特殊的类，只需要继承自标准的`Activity`, `Fragment`, `FragmentActivity`或其他标准类

First of all, import `org.jetbrains.anko.*` to use Anko Layouts DSL in your classes.

首先，在你的class文件中导入`org.jetbrains.anko.*`来使用Anko的Layouts DSL。

DSL is available in `onCreate()`:
`onCreate()`中使用DSL

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

`hint`和`textSize`是绑定到getters和setters上的[合成扩展属性](https://kotlinlang.org/docs/reference/java-interop.html#getters-and-setters)，`padding`是一个Anko里的[继承属性](http://kotlinlang.org/docs/reference/extensions.html#extension-properties). 它们一直都存在于所有的`View`属性中，并且允许你使用`text = "Some text"`来代替`setText("Some text")`.

`verticalLayout` (a `LinearLayout` but already with a `LinearLayout.VERTICAL` orientation), `editText` and `button` are [extension functions](http://kotlinlang.org/docs/reference/extensions.html) that construct the new `View` instances and add them to the parent. We will reference such functions as *blocks*.

`verticalLayout`(一个已经设置了`LinearLayout.VERTICAL`属性的`LinearLayout`布局)，`editText`和`button`都[扩展功能](http://kotlinlang.org/docs/reference/extensions.html)，他们能够构建出一个新的`View`实例，并且把新的实例加入到父布局中。我们将这种功能称之为*blocks（块级视图）*

Blocks exist for almost every `View` in Android framework, and they work in `Activities`, `Fragments` (both default and that from `android.support` package) and even for `Context`. For example, if you have an `AnkoContext` instance, you can write blocks like this:

在Android框架中，几乎每一个视图都存在块，它们在`Activities`、`Fragments`（默认指`android.support`包下的）以及`Context`中工作。比如，如果你有一个` AnkoContext `实例，你可以这样写块：

```kotlin
val name: EditText = with(ankoContext) {
    editText {
        hint = "Name"
    }
}
```

### AnkoComponent
### Anko组件

Although you can use the DSL directly (in `onCreate()` or everywhere else), without creating any extra classes, it is often convenient to have UI in the separate class. If you use the provided `AnkoComponent` interface, you also you get a DSL [layout preview](doc/PREVIEW.md) feature for free.

虽然你可以不用创建其他多余的类而直接使用DSL(在`onCreate()`或者其他任何地方)，在单独的类中设置UI通常很方便。如果你使用提供的`AnkoComponent`接口，你可以很容易获得DSL[布局预览](doc/PREVIEW.md)特性

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

你可能早就注意到了，上一节中的`button()`函数接受一个`String`参数。对于经常使用的视图，如“TextView”，“EditText”，“Button”或“ImageView”，也存在这样的函数。

If you don't need to set any properties for some particular `View`, you can omit `{}` and write `button("Ok")` or even just `button()`:

如果您不需要为某些特定的“View”设置任何属性，则可以省略`{}`并写成`button(“Ok”)`或甚至只需`button()`：

```kotlin
verticalLayout {
    button("Ok")
    button(R.string.cancel)
}
```

### Themed blocks
### 主题块

Anko provides "themeable" versions of blocks, including helper blocks:
Anko提供了“主题化”的块，包括帮助块：

```kotlin
verticalLayout {
    themedButton("Ok", theme = R.style.myTheme)
}
```

### Layouts and `LayoutParams`
### 布局和`LayoutParams`

The positioning of widgets inside parent containers can be tuned using `LayoutParams`. In XML it looks like this:

可以使用“LayoutParams”调整小容器在父容器内的位置。在XML中，它看起来像这样：

```xml
<ImageView 
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_marginLeft="5dip"
    android:layout_marginTop="10dip"
    android:src="@drawable/something" />
```

In Anko, you specify `LayoutParams` right after a `View` description using `lparams()`:

在Anko中，如果你要指定`LayoutParams`，最好是在`View`描述之后使用`lparams()`：

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

如果你指定`lparams()`，但是省略了`width`或`height`，它们的默认值都是`wrapContent`。但你也可以使用[命名参数](http://kotlinlang.org/docs/reference/functions.html#named-arguments)来明确地设置它们。

Some convenient helper properties to notice:
以下是一些方便快捷的帮助属性：

- `horizontalMargin` sets both left and right margins, 
- `horuzontalMargin`可以设置左和右的margin
- `verticalMargin` set top and bottom ones, and 
- `verticalMargin`可以设置上和下的margin
- `margin` sets all four margins simultaneously.
- `margin`可以同时设置四个方向的margin
Note that `lparams()` are different for different layouts, for example, in the case of `RelativeLayout`:

请注意，“lparams()”对于不同的布局是不同的，例如，在“RelativeLayout”的情况下：

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
### 监听器

Anko has listener helpers that seamlessly support coroutines. You can write asynchronous code right inside your listeners!

Anko有无缝支持协同程序的监听器Helper。你可以在你的监听器里面写异步代码！

```kotlin
button("Login") {
    onClick {
    	val user = myRetrofitService.getUser().await()
        showUser(user)
    }
}
```

It is nearly the same as this:
等同于这样的：

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

当你的监听器有很多方法时，Anko是非常有用的。下面是不使用Anko的代码：

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
使用Anko的情况：

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

如果你为同一个“View”设置“onProgressChanged()”和“onStartTrackingTouch()”，则这两个“部分定义”的监听器将被合并。对于相同的侦听器方法，将使用最后一个的方法。

### Custom coroutine context
### 自定义协同上下文

You can pass a custom coroutine context to the listener helpers:

您可以将自定义协同上下文传递给监听器Helper:

```kotlin
button("Login") {
    onClick(yourContext) {
    	val user = myRetrofitService.getUser().await()
        showUser(user)
    }
}
```

### Using resource identifiers
### 使用资源标识符

All examples in the previous chapters used raw Java strings, but it is hardly a good practice. Typically you put all your string data into `res/values/` directory and access it at runtime calling, for example, `getString(R.string.login)`.

前面章节中的所有示例都使用了原始的Java字符串，这并不是一个好习惯。通常情况下，您将所有的字符串数据放入`res/values/`目录中，并在运行调用时访问它，例如`getString(R.string.login)`。

Fortunately, in Anko you can pass resource identifiers both to helper blocks (`button(R.string.login)`) and to extension properties (`button { textResource = R.string.login }`).

幸运的是，在Anko中，您可以将资源标识符传递给帮助块(`button(R.string.login)`)和扩展属性(`button {textResource = R.string.login}`)。

Note that the property name is not the same: instead of `text`, `hint`, `image`, we now use `textResource`, `hintResource` and `imageResource`.

请注意，属性名称不一样了：`我们现在使用`textResource`，`hintResource`和`imageResource`来代替`text`，`hint`，`image

<table>
<tr><td width="50px" align="center">:penguin:</td>
<td>
<i>Resource properties always throw <code>AnkoException</code> when read.</i>
</td>
</tr>
</table>

### Instance shorthand notation
### 实例速记符号

Sometimes you need to pass a `Context` instance to some Android SDK method from your `Activity` code.
Usually, you can just use `this`, but what if you're inside an inner class? You would probably write `SomeActivity.this` in case of Java
and `this@SomeActivity` if you're writing in Kotlin.

有时您需要从“Activity”中将“Context”实例传递给某些Android SDK里的方法。
通常情况下，您可以使用“this”，但是如果你在一个内部类中呢？在Java的情况下，你可能会写“SomeActivity.this”
如果你用Kotlin写，那么使用`this@ SomeActivity`。

With Anko you can just write `ctx`. It is an extension property which works both inside `Activity` and `Service` and is even
accessible from `Fragment` (it uses `getActivity()` method under the hood). You can also get an `Activity` instance using `act` extension property.

在Anko中，你可以写`ctx`来获取`Context`实例。它是`Activity`和`Service`中的一个扩展属性，也可以从`Fragment`获取到(使用`getActivity()`方法)。另外，您还可以使用`act`扩展属性来获取一个`Activity`实例。

### UI wrapper
### UI包装

Before the Beginning of Time Anko always used `UI` tag as a top-level DSL element:

在此之前，Anko使用“UI”标签作为顶级DSL元素：

```kotlin
UI {
    editText {
        hint = "Name"
    }
}
```

You can still use this tag if you want. And it would be much easier to extend DSL as you have to declare only one `ViewManager.customView` function.
See [Extending Anko](doc/ADVANCED.md#extending-anko) for more information.

如果需要，您仍然可以使用此标签。而扩展DSL将更容易，因为您只需要写一个`ViewManager.customView`函数。访问[Extending Anko](doc/ADVANCED.md#extending-anko)查看更多信息

### Include tag
### Include标签

It is easy to insert an XML layout into DSL. Use the `include()` function:

使用`include()`函数很容易就将XML布局引入到DSL中了：

```kotlin
include<View>(R.layout.something) {
    backgroundColor = Color.RED
}.lparams(width = matchParent) { margin = dip(12) }
```

You can use `lparams()` as usual, and if you provide a specific type instead of `View`, you can also use this type inside `{}`:

你可以照常使用`lparams()'，如果你使用一个特定类型的视图而不是`View`，你依然可以在`{}`里使用这个指定的视图：

```kotlin
include<TextView>(R.layout.textfield) {
    text = "Hello, world!"
}
```

## Anko Support plugin
## Anko支持插件

Anko Support plugin is available for IntelliJ IDEA and Android Studio. It allows you to preview `AnkoComponent` classes written with Anko directly in the IDE tool window.

Anko的支持插件可用于IntelliJ IDEA和Android Studio。它允许您直接在IDE工具窗口中预览用Anko编写的AnkoComponent类。

<table>
<tr><td width="50px" align="center">:warning:</td>
<td>
<i>The Anko Support plugin is currently supported only in Android Studio 2.4+.
目前Anko的支持插件只适用于Android Studio 2.4及以上版本</i>
</td>
</tr>
</table>

### Installing Anko Support plugin
### 安装Anko支持插件

You can download the Anko Support plugin [here](https://plugins.jetbrains.com/update/index?pr=&updateId=19242).

你可以到[这里](https://plugins.jetbrains.com/update/index?pr=&updateId=19242)下载Anko的支持插件

### Using the plugin
### 使用插件

Suppose you have these classes written with Anko:

假设以下是你用Anko编写的class类:

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

选中`MyActivityUI`类，然后打开* Anko Layout Preview *工具窗口(“View”→“Tool Windows”→“Anko Layout Preview”)，然后按*刷新*。

This requires building the project, so it could take some time before the image is actually shown.

这需要构建项目，所以可能需要一些时间才能显示出效果。

### XML to DSL Converter
### XML转换DSL

The plugin also supports converting layouts from the XML format to Anko Layouts code. Open an XML file and select "Code" → "Convert to Anko Layouts DSL". You can convert several XML layout files simultaneously.

该插件支持将XML转换为Anko Layouts代码。打开一个XML文件并选择“Code”→“Convert to Anko Layouts DSL”。您可以同时转换多个XML布局文件。