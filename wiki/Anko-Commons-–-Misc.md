Below are the helpers that are not part of any specific Anko subsystem.  

## Contents

* [Using helpers in your project](#using-helpers-in-your-project)
* [Colors](#colors)
* [Dimensions](#dimensions)
* [`applyRecursively()`](#apply-recursively)

## Using helpers in your project

All helpers are inside the `anko-commons` artifact. Add it as a dependency to your `build.gradle`:

```groovy
dependencies {
    compile "org.jetbrains.anko:anko-commons:$anko_version"
}
```

## Colors

Two simple extension functions to make the code more readable.

Function             | Result
---------------------|--------- 
`0xff0000.opaque`    | <span style="color:#ff0000">non-transparent red</span>
`0x99.gray.opaque`   | <span style="color:#999">non-transparent #999999 gray</span>

## Dimensions

You can specify dimension values in **dip** (density-independent pixels) or in **sp** (scale-independent pixels): `dip(dipValue)` or `sp(spValue)`. Note that the `textSize`
property already accepts **sp** (`textSize = 16f`). Use `px2dip` and `px2sp` to convert backwards.


## `applyRecursively()`

`applyRecursively()` applies the lambda expression to the passed `View` itself, and then recursively to every child of a `View` if it is a `ViewGroup`:

```kotlin
verticalLayout {
    editText {
        hint = "Name"
    }
    editText {
        hint = "Password"
    }
}.applyRecursively { view -> when(view) {
    is EditText -> view.textSize = 20f
}}
```