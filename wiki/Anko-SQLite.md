你是否已经厌倦了用 Android cursors 解析 SQLite 查询的结果，每次必须编写大量的模板代码才能解析出查询结果，并将其封装在无数的 `try..finally` 块中以保证能够释放所有的资源。

Anko 提供了大量的扩展功能，大大简化了 SQLite 的使用方式。

## 概要

* [在你的项目中添加依赖](#在你的项目中添加依赖)
* [访问数据库](#访问数据库)
* [表的创建和删除](#表的创建和删除)
* [添加数据](#添加数据)
* [查询数据](#查询数据)
* [解析查询结果](#解析查询结果)
* [自定义rowParsers](#自定义rowparsers)
* [Cursor streams](#cursor-streams)
* [修改数据](#修改数据s)
* [事务](#事务)
* [本篇文章相关代码传送门](#本篇文章相关代码传送门)

## 在你的项目中添加依赖

```groovy
dependencies {
    compile "org.jetbrains.anko:anko-sqlite:$anko_version"
}
```

## 访问数据库

如果使用 `SQLiteOpenHelper`, 一般是调用 `getReadableDatabase()` 或者 `getWritableDatabase()` , 但是必须记住在使用后调用 `close` 来释放资源 ，并且需要把 `SQLiteOpenHelper` 对象缓存起来，如果还想在多个线程中使用同一个 helper 对象，还要解决并发访问的问题 。 这一切都很难。这就是为什么开发者都不太喜欢默认的 SQLite API，而是更喜欢一些第三方的 ORM 库。

Anko 提供了一个特别的类 `ManagedSQLiteOpenHelper` ，可以无缝地替换掉默认的 API。下面就看看如何使用它：

```kotlin
class MyDatabaseOpenHelper(ctx: Context) : ManagedSQLiteOpenHelper(ctx, "MyDatabase", null, 1) {
    companion object {
        private var instance: MyDatabaseOpenHelper? = null

        @Synchronized
        fun getInstance(ctx: Context): MyDatabaseOpenHelper {
            if (instance == null) {
                instance = MyDatabaseOpenHelper(ctx.getApplicationContext())
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        //创建表
        db.createTable("User",true,
                "id" to INTEGER + PRIMARY_KEY + UNIQUE,  //（1）
                "name" to TEXT,
                "sex" to TEXT)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        //数据库版本变更后回调
    }
}

// 在 Context 下提供一个访问的变量
val Context.database: MyDatabaseOpenHelper
    get() = MyDatabaseOpenHelper.getInstance(getApplicationContext())
```
（1）我运行的时候这里会报错（报错的原因是第三个关键字无法正确的解析，不知道是不是 bug 0.0），这里如果创建不成功先改成两个关键字就行了（`"id" to INTEGER + PRIMARY_KEY`）。


在 use 中 的语句块自动被套上 `try` 块，并且语句块执行完毕后自动调用 `close` 方法释放资源，在 use 语句块中包含了 SQLiteDatabase 的实例，也就是可以直接在语句块中使用 SQLiteDatabase 实例的所有 public 属性和方法。

```kotlin
database.use {
    // `this` is a SQLiteDatabase instance
}
```

异步调用示例：

```kotlin
async(UI) {
    //在主线程中
    val result = bg {
        //在子线程中执行对数据库的操作
        database.use {
            //可以直接使用 SQLiteDatabase 对象的所有方法
            //insert()
            //query()
            //......
        }
    }
    //在主线程中对结果进行处理
    //loadComplete(result)
}
```

##### 注意
 - use 语句块中的代码可能会抛出 SQLiteException , 如果你认为代码可能出错，需要自己手动捕获处理异常，不然程序会崩溃，如果你确定代码不会有任何问题，可以不做任何处理。
 - bg 语句块会忽略其中的异常（至少我在本篇例子中操作数据库时是这样），所以如果你按照上面的 『异步调用示例』写代码，当 SQLiteException 异常发生时，不会有任何提示，这里要小心了，所以用 bg 语句块的时候最好手动捕获异常：
   ```kotlin
     val result = bg {
        try {
           database.use {
              ...
           }
        } catch (e: SQLiteException){
            e.printStackTrace()
        }
     }
   ```



## 表的创建和删除

使用 Anko，您可以轻松的创建新并删除现有的表。语法很简单。

```kotlin
database.use {
    createTable("Customer", true,
        "id" to INTEGER + PRIMARY_KEY,
        "name" to TEXT,
        "photo" to BLOB)
}
```

在 SQLite 中， 有 5 种主要类型: `NULL`, `INTEGER`, `REAL`, `TEXT` 和 `BLOB`. 但每列可能有一些修饰符，如 `PRIMARY KEY` 或 `UNIQUE`。 你可以通过 `+` 号在需要的字段上加上这些修饰符。

使用 `dropTable` 方法删除表：

```kotlin
dropTable("User", true)
```

## 添加数据

通常，您需要一个 `ContentValues` 实例来在表中插入一行。比如下面这样：

```kotlin
val values = ContentValues()
values.put("id", 5)
values.put("name", "John Smith")
values.put("email", "user@domain.org")
db.insert("User", null, values)
```

Anko 可以通过 `insert()` 方法直接传入参数，更加直观，方便。

```kotlin
db.insert("User",
    "id" to 42,
    "name" to "John",
    "email" to "user@domain.org"
)
```

或者

```kotlin
database.use {
    insert("User",
            "id" to 42,
            "name" to "John",
            "email" to "user@domain.org")
}
```

方法 `insertOrThrow()`, `replace()`, `replaceOrThrow()` 都存在并具有相同的功能。

通过查看源码就能知道，Anko 只是对 这些方法做了一层包装而已，所以功能并没有任何变化。

## 查询数据

Anko 提供了一个方便的查询生成器。 可以使用 `db.select(tableName, vararg columns)` 创建，其中 `db` 是 `SQLiteDatabase` 对象。

方法                                  | 描述
--------------------------------------|----------
`column(String)`                      | 添加查询的列
`distinct(Boolean)`                   | Distinct 查询
`whereArgs(String)`                   | where（需要自己拼接字符串）
`whereArgs(String, args)` :star:      | where，可自定义参数占位符
`whereSimple(String, args)`           | where，参数占位符是 `?`
`orderBy(String, [ASC/DESC])`         | 指定排序的列
`groupBy(String)`                     | 指定分组的列
`limit(count: Int)`                   | 设置查询结果的条目数
`limit(offset: Int, count: Int)`      | 设置查询结果的条目数（列：`limit(5,10)` 跳过查询结果前 5 条，然后取10条）
`having(String)`                      | 指定 `having` 表达式（需要自己拼接字符串）
`having(String, args)` :star:         | 指定 `having` 表达式和参数

用 :star: 标记的方法，参数是通过占位符一一对应的，所以参数的顺序可以打乱，下面是一个列子：

```kotlin
db.select("User", "name")
    .whereArgs("(_id > {userId}) and (name = {userName})",
        "userName" to "John",
        "userId" to 42)
```

这里， `{userId}` 将被替换成 `42` ， `{userName}` 被替换成 `'John'`， 如果传递的类型不是 (`Int`, `Float` ,`Boolean`) ，将会被 `toString()`。

`whereSimple` 方法接受 `String` 类型的参数。 它和 `SQLiteDatabase` 中的 query() 方法相同（`?` 号是占位符，被对应的参数替换）。

执行 `exec` 方法就能获取到查询结果了，`exec` 中扩展了 Cursor 对象(`Cursor.() -> T`)，并且 exec 执行完之后自动释放资源。

```kotlin
database.use {
    select("User")
            .column("email")
            .exec {
                while (moveToNext()){
                    info("email:"+getString(0))
                }
            }
}
```
虽然方便了不少，但是还要操作 Cursor，差评！  
说好的不使用 Cursor 呢，且往下看。


## 解析查询结果

为了彻底摆脱 `Cursor` 的阴影，Anko 提供了 `parseSingle`, `parseOpt` 和 `parseList` 三个方法。

方法                                | 描述
--------------------------------------|----------
`parseSingle(rowParser): T`           | 结果为一条数据
`parseOpt(rowParser): T?`             | 结果为一条数据或者没有数据
`parseList(rowParser): List<T>`       | 结果为多条数据

注意哦，如果使用 `parseSingle()` 或者 `parseOpt()` ，当结果是多条数据的时候会抛出异常哦。

那么问题来了，什么是 `rowParser` ？   
每个方法都支持两种 parsers: `RowParser` 和 `MapRowParser`：

```kotlin
interface RowParser<T> {
    fun parseRow(columns: Array<Any>): T
}

interface MapRowParser<T> {
    fun parseRow(columns: Map<String, Any>): T
}
```

如果追求效率，请使用 RowParser （如果是多列，你必须要知道每列的索引）。 `parseRow` 实际上只支持 `Long`, `Double`, `String` 和 `ByteArray` 。而 `MapRowParser` 可以通过列名来获取值。

Anko 提供了常用的单列解析器（结果只有一列的时候可用）：

* `ShortParser`
* `IntParser`
* `LongParser`
* `FloatParser`
* `DoubleParser`
* `StringParser`
* `BlobParser`

以 StringParser 为例，用法如下：
```kotlin
//查询所有用户的邮箱
database.use {
    select("User")
            .column("email")
            .whereArgs("name = {userName}","userName" to "jack")
            .parseList(StringParser)
            .forEach {
                info { "email:$it" }
            }
}
```
当查询结果为多列的时候可以用 MapRowParser。  
首先要定义个数据类。
```kotlin
data class  User(val id:Long,val name:String,val email:String)
```
然后自定义一个 MapRowParser。
```kotlin
class UserRowParser : MapRowParser<User> {
    override fun parseRow(columns: Map<String, Any?>): User {
        return User(columns["id"] as Long, columns["name"] as String, columns["email"] as String)
    }
}
```
最后使用
```kotlin
database.use {
    select("User")
            .parseList(UserRowParser())
            .forEach {
                info { it }
            }
}
```
虽然目的达到了，但是还是有点麻烦对不对。  
不用担心，Anko 早已看穿了这一切，使用 classParser 连 UserRowParser 都不需要了：

```kotlin
database.use {
    select("User")
            .parseList(classParser<User>())
            .forEach {
                info { it }
            }
}
```


## 自定义rowParsers

自定义 rowParsers 同自定义 MapRowParser（区别只在于前者需要知道数据的索引，后者需要知道列名）：

```kotlin
class MyRowParser : RowParser<Triple<Int, String, String>> {
    override fun parseRow(columns: Array<Any>): Triple<Int, String, String> {
        return Triple(columns[0] as Int, columns[1] as String, columns[2] as String)
    }
}
```

使用 lambda 表达式实现：

```kotlin
database.use {
    select("User")
            .parseList(rowParser { id: Long, name: String, email: String -> User(id, name, email) })
            .forEach {
                info { it }
            }
}
```

## Cursor streams

Anko 还提供了两种方式解析 Cursor ，转换成 数组列表 或者 map 列表（`cursor.asSequence()` 和 `cursor.asMapSequence()`）：
```kotlin
database.use {
    select("User")
            .exec {
                for (item in asSequence()) {
                    info("id=${item[0]},name=${item[1]},email=${item[2]}")
                }
            }
}

database.use {
    select("User")
            .exec {
                for (item in asMapSequence()) {
                    info("id=${item["id"]},name=${item["name"]},email=${item["email"]}")
                }
            }
}
```


## 修改数据

修改某一个用户的名字：

```kotlin
database.use {
    update("User", "name" to "zhangsan")
            .whereArgs("id = {userId}", "userId" to 123)
            .exec()
}
```

`whereSimple()` 方法用法：

```kotlin
database.use {
    update("User", "name" to "zhangsan")
            .whereSimple("id = ?",123.toString())
            .exec()
}
```

## 事务

`transaction()` 方法封装了事务，在其中发生错误时，方法中所有对数据库的操作都会回滚，如果没有发生任何错误，事务将被提交：

```kotlin
database.use {
    transaction {
        update("User", "name" to "lisi")
                .whereSimple("id = ?",123.toString())
                .exec()
        throw Exception("error")
        //抛出异常，程序崩溃，数据将修改失败
    }
}
```

如果要由于某种原因需要中止事务，只需抛出TransactionAbortException。这个异常只会终止事务，程序不会崩溃，当然所有事务中对数据库的操作也无效。
```kotlin
database.use {
    transaction {
        update("User", "name" to "lisi")
                .whereSimple("id = ?",123.toString())
                .exec()
        throw TransactionAbortException()
        //后面的代码都不会在执行了
        //....................
    }
}
```
### 本篇文章相关代码传送门
[info/jiuyou/learnanko/sqlite/](https://github.com/jianshijiuyou/LearnAnko/tree/master/app/src/main/java/info/jiuyou/learnanko/sqlite)
