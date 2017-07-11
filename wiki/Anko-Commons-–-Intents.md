## 概要

* [在你的项目中添加依赖](#在你的项目中添加依赖)
* [优雅的创建和使用Intent](#优雅的创建和使用intent)
* [自带的特效](#自带的特效)
* [本篇文章相关代码传送门](#本篇文章相关代码传送门)
## 在你的项目中添加依赖

```groovy
dependencies {
    compile "org.jetbrains.anko:anko-commons:$anko_version"
}
```
## 优雅的创建和使用Intent

一般来说，如果你需要在启动一个 intent 时携带参数，那么至少需要编写三行代码（初始化、添加参数、启动），多一个参数或多一个flag，就要多写一行代码，比如下面这样：

```kotlin
val intent = Intent(this, SomeOtherActivity::class.java)
intent.putExtra("id", 5)
intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
startActivity(intent)
```

居然要编写四行代码，太多了，Anko 提供了更加简单的方式：

```kotlin
startActivity(intentFor<SomeOtherActivity>("id" to 5).singleTop())
```

如果你不需要任何flag，那更加简单：

```kotlin
startActivity<SomeOtherActivity>("id" to 5)
```

## 自带的特效

Anko 已经考虑到了一些广泛的使用情况，并作了对应的封装：

目标                | 方法
--------------------|---------
拨打电话              | `makeCall(number)`
发短信                | `sendSMS(number, [text])`
使用自来浏览器打开网页  | `browse(url)`
文字分享              | `share(text, [subject])`
发送邮件              | `email(email, [subject], [text])`

（`[]`）中的参数是可选择的，操作成功，将返回 true。  
### 本篇文章相关代码传送门
[IntentsActivity.kt](https://github.com/jianshijiuyou/LearnAnko/blob/master/app/src/main/java/info/jiuyou/learnanko/commons/IntentsActivity.kt)
