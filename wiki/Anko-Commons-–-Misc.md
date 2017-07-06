## 概要

* [在你的项目中添加依赖](#在你的项目中添加依赖)
* [Colors](#colors)
* [Dimensions](#dimensions)
* [`applyRecursively()`](#apply-recursively)
* [本篇文章相关代码传送门](#本篇文章相关代码传送门)

## 在你的项目中添加依赖

```groovy
dependencies {
    compile "org.jetbrains.anko:anko-commons:$anko_version"
}
```

## Colors

两个简单的扩展功能让代码更加容易阅读。

Function             | Result
---------------------|---------
`0xff0000.opaque`    | <span style="color:#ff0000">不透明 红色</span>
`0x99.gray.opaque`   | <span style="color:#999">不透明 #999999 灰色</span>

具体用法
```kotlin
textView1.textColor=0xff0000.opaque
textView2.textColor=0x99.gray.opaque
```

## Dimensions

Anko 自带 dip to px, sp to px, px to dip,px to sp 工具
例如：
```kotlin
val res1=dip(100)
info("dip(100)=$res1")
val res2=sp(100)
info("sp(100)=$res2")
val res3=px2dip(100)
info("px2dip(100)=$res3")
val res4=px2sp(100)
info("px2sp(100)=$res4")

//================打印结果====================
//  I/MiscActivity: dip(100)=200
//  I/MiscActivity: sp(100)=200
//  I/MiscActivity: px2dip(100)=50.0
//  I/MiscActivity: px2sp(100)=50.0
```


## `applyRecursively()`

`applyRecursively()` 方法会遍历 `ViewGroup` 中的所有 `view`

```kotlin
//创建一个竖向的线性布局，添加一个 textView 和 editText 。
//applyRecursively() 遍历viewGroup中的所有view。
verticalLayout {
    textView {
        text="textView"
        textSize=20f
    }
    editText {
        hint = "editText"
    }
}.applyRecursively { view ->
    when (view) {
        is EditText -> view.textSize = 30f
    }
}
```
### 本篇文章相关代码传送门
[MiscActivity.kt](https://github.com/jianshijiuyou/LearnAnko/blob/master/app/src/main/java/info/jiuyou/learnanko/commons/MiscActivity.kt)
