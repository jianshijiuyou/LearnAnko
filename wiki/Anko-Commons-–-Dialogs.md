## 概要

* [在你的项目中添加依赖](#在你的项目中添加依赖)
* [Toasts](#toasts)
* [Alerts](#alerts)
* [Selectors](#selectors)
* [Progress dialogs](#progress-dialogs)
* [本篇文章相关代码传送门](#本篇文章相关代码传送门)

## 在你的项目中添加依赖

 `build.gradle`:

```groovy
dependencies {
    compile "org.jetbrains.anko:anko-commons:$anko_version"
}
```

## Toasts

显示一个 toast 消息

```kotlin
toast("Hi there!")
toast(R.string.message)
longToast("Wow, such a duration")
```

## Alerts

显示一个对话框（alert dialogs）（android 默认 style）

```kotlin
alert("这是长长长的内容", "这是短短的标题") {
    yesButton { toast("确定") }
    noButton {}
}.show()
//自定义button文字
alert("这是长长长的内容", "这是短短的标题"){
    positiveButton("前进"){
        toast("前进")
    }
    negativeButton("后退"){
        toast("后退")
    }
} .show()
```

Appcompat style（就是 support:appcompat-v7 中的 dialogs）

```kotlin
alert(Appcompat,"这是长长长的内容", "Appcompat style dialog") {
    yesButton { toast("确定") }
    noButton {}
}.show()
```

自定义 dialog

```kotlin
alert {
    customView {
        val et = editText()
        et.hint="custom dialog"
    }
}.show()
```

## Selectors

```kotlin
val countries = listOf("item1", "item2", "item3", "item4")
selector("单项选择", countries) { dialogInterface, i ->
    toast("你选择了 ${countries[i]} ！！！")
}
```

## Progress dialogs


```kotlin
//带进度条的
val dialog = indeterminateProgressDialog(message = "正在加载中…", title = "提示")
//设置进度
dialog.progress=10
//不带进度条的（转圈）
indeterminateProgressDialog(message = "正在加载中…", title = "提示")
```

### 本篇文章相关代码传送门
[DialogsActivity.kt](https://github.com/jianshijiuyou/LearnAnko/blob/master/app/src/main/java/info/jiuyou/learnanko/commons/DialogsActivity.kt)
