## Contents

* [Using Anko `Intent` helpers in your project](#using-anko-intent-helpers-in-your-project)
* [`Intent` builder functions](#intent-builder-functions)
* [Useful `Intent` callers](#useful-intent-callers)

## Using Anko `Intent` helpers in your project

Intent helpers are inside the `anko-commons` artifact. Add it as a dependency to your `build.gradle`:

```groovy
dependencies {
    compile "org.jetbrains.anko:anko-commons:$anko_version"
}
```
## `Intent` builder functions

In general, you have to write a couple of lines to start a new `Activity`. And it requires you to write an additional line for each value you pass as an extra. For example, this is a code for starting an `Activity` with extra `("id", 5)` and a special flag:

```kotlin
val intent = Intent(this, SomeOtherActivity::class.java)
intent.putExtra("id", 5)
intent.setFlag(Intent.FLAG_ACTIVITY_SINGLE_TOP)
startActivity(intent)
```

Four lines is too much for this. Anko offers you an easier way:

```kotlin
startActivity(intentFor<SomeOtherActivity>("id" to 5).singleTop())
```

If you don't need to pass any flags, the solution is even easier:

```kotlin
startActivity<SomeOtherActivity>("id" to 5)
```

## Useful `Intent` callers

Anko has call wrappers for some widely used `Intents`:

Goal                | Solution
--------------------|--------- 
Make a call         | `makeCall(number)` without **tel:**
Send a text         | `sendSMS(number, [text])` without **sms:**
Browse the web      | `browse(url)`
Share some text     | `share(text, [subject])`
Send a email        | `email(email, [subject], [text])`

Arguments in square brackets (`[]`) are optional. Methods return true if the intent was sent.