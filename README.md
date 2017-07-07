[![](https://img.shields.io/badge/language-kotlin-blue.svg)](http://kotlinlang.org/) ![](https://img.shields.io/badge/kotlin_version-1.1.3-green.svg) ![](https://img.shields.io/badge/anko_version-0.10.1-green.svg)
## [官方链接](https://github.com/Kotlin/anko/wiki)

说明
==============================
因为笔者能力有限，所以本文并不是真正意义上的翻译，很多地方都是笔者根据自己的的理解所写，如有错误，欢迎指正，与君共勉。  

在浏览本文档及相关 demo 前，你应该对 kotlin 的基础用法和相关特性有所了解。  
如果还没有了解，请前往 [kotlin 中文社区](https://www.kotlincn.net/docs/reference/)。

###### 在项目中引入 Anko：
```groovy
dependencies {
    compile "org.jetbrains.anko:anko:$anko_version"
}
```
以上就引入了 Anko 的所有功能，如需单独引用部分功能，请查看对应文档。


开始
==============================
Anko 是一个 kotlin library ， 它能让你更轻松更快速的开发 android 应用程序，用它可以写出更简洁且易于阅读的代码，在 java 中那些让人诟病已久的冗余代码不会在这里出现。  

Anko 由以下几个部分组成:

* Anko Commons: 一个轻量级的组件帮助库：
	* [Intents](https://github.com/jianshijiuyou/LearnAnko/blob/master/wiki/Anko-Commons-%E2%80%93-Intents.md); （已完成）
	* [Dialogs and toasts](https://github.com/jianshijiuyou/LearnAnko/blob/master/wiki/Anko-Commons-–-Dialogs.md);（已完成）
	* [Logging](https://github.com/jianshijiuyou/LearnAnko/blob/master/wiki/Anko-Commons-–-Logging.md);（已完成）
	* [Resources and dimensions](https://github.com/jianshijiuyou/LearnAnko/blob/master/wiki/Anko-Commons-–-Misc.md);（已完成）
* [Anko Layouts](https://github.com/jianshijiuyou/LearnAnko/blob/master/wiki/Anko-Layouts.md): 用一种快速并且类型安全的方式动态的编写 android 布局
* [Anko SQLite](https://github.com/jianshijiuyou/LearnAnko/blob/master/wiki/Anko-SQLite.md): （已完成）更加简单的操作数据库
* [Anko Coroutines](https://github.com/jianshijiuyou/LearnAnko/blob/master/wiki/Anko-Coroutines.md): 协程
