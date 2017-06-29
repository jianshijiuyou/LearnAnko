## Contents

* [Using Anko Dialogs in your project](#using-anko-dialogs-in-your-project)
* [Toasts](#toasts)
* [Alerts](#alerts)
* [Selectors](#selectors)
* [Progress dialogs](#progress-dialogs)

## Using Anko Dialogs in your project

Dialog helpers are inside the `anko-commons` artifact. Add it as a dependency to your `build.gradle`:

```groovy
dependencies {
    compile "org.jetbrains.anko:anko-commons:$anko_version"
}
```

## Toasts

Simply shows a [Toast](https://developer.android.com/guide/topics/ui/notifiers/toasts.html) message.

```kotlin
toast("Hi there!")
toast(R.string.message)
longToast("Wow, such a duration")
```

## Alerts

A small DSL for showing [alert dialogs](https://developer.android.com/guide/topics/ui/dialogs.html).

```kotlin
alert("Hi, I'm Roy", "Have you tried turning it off and on again?") {
    yesButton { toast("Oh…") }
    noButton {}
}.show()
```

The code above will show the default Android alert dialog. If you want to switch to the appcompat implementation, use the `Appcompat` dialog factory:

```kotlin
alert(Appcompat, "Some text message").show()
```

`Android` and `Appcompat` dialog factories are included by default, but you can create your custom factories by implementing the `AlertBuilderFactory` interface.

`alert()` functions seamlessly support Anko layouts as custom views:

```kotlin
alert {
    customView {
        editText()
    }
}.show()
```

## Selectors

`selector()` shows an alert dialog with a list of text items:

```kotlin
val countries = listOf("Russia", "USA", "Japan", "Australia")
selector("Where are you from?", countries) { dialogInterface, i ->
    toast("So you're living in ${countries[i]}, right?")
}
```

## Progress dialogs

`progressDialog()` creates and shows a [progress dialog](https://developer.android.com/reference/android/app/ProgressDialog.html).

```kotlin
val dialog = progressDialog(message = "Please wait a bit…", title = "Fetching data")
```

An indeterminate progress dialog is also available (see `indeterminateProgressDialog()`).