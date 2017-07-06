你是否已经厌倦了用 Android cursors 解析 SQLite 查询的结果，每次必须编写大量的模板代码才能解析出查询结果，并将其封装在无数的 `try..finally` 块中以关闭打开的所有资源。

Anko 提供了大量的扩展功能，大大简化了 SQLite 的使用方式。

## 概要

* [在你的项目中添加依赖](#在你的项目中添加依赖)
* [访问数据库](#访问数据库)
* [Creating and dropping tables](#creating-and-dropping-tables)
* [Inserting data](#inserting-data)
* [Querying data](#querying-data)
* [Parsing query result](#parsing-query-result)
* [Custom row parsers](#custom-row-parsers)
* [Cursor streams](#cursor-streams)
* [Updating values](#updating-values)
* [Transactions](#transactions)


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
                "id" to INTEGER + PRIMARY_KEY + UNIQUE,
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

在 use 中 的语句块自动被套上 `try` 块，并且语句块执行完毕后自动调用 `close` 方法释放资源，在 use 语句块中包含了 SQLiteDatabase 的实例，也就是可以直接调用它的所有 public 属性和方法。

```kotlin
database.use {
    // `this` is a SQLiteDatabase instance
}
```

异步调用示例：

```kotlin
class SomeActivity : Activity() {
    private fun loadAsync() {
        async(UI) {
            val result = bg {
                database.use { ... }
            }
            loadComplete(result)
        }
    }
}
```

<table>
<tr><td width="50px" align="center">:penguin:</td>
<td>
<i>These and all methods mentioned below may throw <code>SQLiteException</code>. You have to handle it by yourself because it would be unreasonable for Anko to pretend that errors don't occur.</i>
</td>
</tr>
</table>

## Creating and dropping tables

With Anko you can easily create new tables and drop existing ones. The syntax is straightforward.

```kotlin
database.use {
    createTable("Customer", true,
        "id" to INTEGER + PRIMARY_KEY + UNIQUE,
        "name" to TEXT,
        "photo" to BLOB)
}
```

In SQLite, there are five main types: `NULL`, `INTEGER`, `REAL`, `TEXT` and `BLOB`. But each column may have some modifiers like `PRIMARY KEY` or `UNIQUE`. You can append such modifiers with "adding" them to the primary type name.

To drop a table, use the `dropTable` function:

```kotlin
dropTable("User", true)
```

## Inserting data

Usually, you need a `ContentValues` instance to insert a row into the table. Here is an example:

```kotlin
val values = ContentValues()
values.put("id", 5)
values.put("name", "John Smith")
values.put("email", "user@domain.org")
db.insert("User", null, values)
```

Anko lets you eliminate such ceremonies by passing values directly as arguments for the `insert()` function:

```kotlin
db.insert("User",
    "id" to 42,
    "name" to "John",
    "email" to "user@domain.org"
)
```

or from within `database.use` as:

```kotlin
database.use {
    insert("User",
        "id" to 42,
        "name" to "John",
        "email" to "user@domain.org"
}
```

Functions `insertOrThrow()`, `replace()`, `replaceOrThrow()` also exist and have the similar semantics.

## Querying data

Anko provides a convenient query builder. It may be created with
`db.select(tableName, vararg columns)` where `db` is an instance of `SQLiteDatabase`.

Method                                | Description
--------------------------------------|----------
`column(String)`                      | Add a column to select query
`distinct(Boolean)`                   | Distinct query
`whereArgs(String)`                   | Specify raw String `where` query
`whereArgs(String, args)` :star:      | Specify a `where` query with arguments
`whereSimple(String, args)`           | Specify a `where` query with `?` mark arguments
`orderBy(String, [ASC/DESC])`         | Order by this column
`groupBy(String)`                     | Group by this column
`limit(count: Int)`                   | Limit query result row count
`limit(offset: Int, count: Int)`      | Limit query result row count with an offset
`having(String)`                      | Specify raw `having` expression
`having(String, args)` :star:         | Specify a `having` expression with arguments

Functions marked with :star: parse its arguments in a special way. They allow you to provide values in any order and support escaping seamlessly.

```kotlin
db.select("User", "name")
    .whereArgs("(_id > {userId}) and (name = {userName})",
        "userName" to "John",
        "userId" to 42)
```

Here, `{userId}` part will be replaced with `42` and `{userName}` — with `'John'`. The value will be escaped if its type is not numeric (`Int`, `Float` etc.) or `Boolean`. For any other types, `toString()` representation will be used.

`whereSimple` function accepts arguments of `String` type. It works the same as [`query()`](http://developer.android.com/reference/android/database/sqlite/SQLiteDatabase.html#query(java.lang.String,%20java.lang.String[],%20java.lang.String,%20java.lang.String[],%20java.lang.String,%20java.lang.String,%20java.lang.String)) from `SQLiteDatabase` (question marks `?` will be replaced with actual values from arguments).

How can we execute the query? Using the `exec()` function. It accepts an extension function with the type of `Cursor.() -> T`. It simply launches the received extension function and then closes `Cursor` so you don't need to do it by yourself:

```kotlin
db.select("User", "email").exec {
	// Doing some stuff with emails
}
```

## Parsing query results

So we have some `Cursor`, and how can we parse it into regular classes? Anko provides functions `parseSingle`, `parseOpt` and `parseList` to do it much more easily.

Method                                | Description
--------------------------------------|----------
`parseSingle(rowParser): T`           | Parse exactly one row
`parseOpt(rowParser): T?`             | Parse zero or one row
`parseList(rowParser): List<T>`       | Parse zero or more rows

Note that `parseSingle()` and `parseOpt()` will throw an exception if the received Cursor contains more than one row.

Now the question is: what is `rowParser`? Well, each function supports two different types of parsers: `RowParser` and `MapRowParser`:

```kotlin
interface RowParser<T> {
    fun parseRow(columns: Array<Any>): T
}

interface MapRowParser<T> {
    fun parseRow(columns: Map<String, Any>): T
}
```

If you want to write your query in a very efficient way, use RowParser (but then you must know the index of each column). `parseRow` accepts a list of `Any` (the type of `Any` could practically be nothing but `Long`, `Double`, `String` or `ByteArray`). `MapRowParser`, on the other hand, lets you get row values by using column names.

Anko already has parsers for simple single-column rows:

* `ShortParser`
* `IntParser`
* `LongParser`
* `FloatParser`
* `DoubleParser`
* `StringParser`
* `BlobParser`

Also, you can create a row parser from the class constructor. Assuming you have a class:

```kotlin
class Person(val firstName: String, val lastName: String, val age: Int)
```

The parser will be as simple as:

```kotlin
val rowParser = classParser<Person>()
```

For now, Anko **does not support** creating such parsers if the primary constructor has optional parameters. Also, note that constructor will be invoked using Java Reflection so writing a custom `RowParser` is more reasonable for huge data sets.

If you are using Anko `db.select()` builder, you can directly call `parseSingle`, `parseOpt` or `parseList` on it and pass an appropriate parser.

## Custom row parsers

For instance, let's make a new parser for columns `(Int, String, String)`. The most naive way to do so is:

```kotlin
class MyRowParser : RowParser<Triple<Int, String, String>> {
    override fun parseRow(columns: Array<Any>): Triple<Int, String, String> {
        return Triple(columns[0] as Int, columns[1] as String, columns[2] as String)
    }
}
```

Well, now we have three explicit casts in our code. Let's get rid of them by using the `rowParser` function:

```kotlin
val parser = rowParser { id: Int, name: String, email: String ->
    Triple(id, name, email)
}
```

And that's it! `rowParser` makes all casts under the hood and you can name the lambda parameters as you want.

## Cursor streams

Anko provides a way to access SQLite `Cursor` in a functional way. Just call `cursor.asSequence()` or `cursor.asMapSequence()` extension functions to get a sequence of rows. Do not forget to close the `Cursor` :)

## Updating values

Let's give a new name to one of our users:

```kotlin
update("User", "name" to "Alice")
    .where("_id = {userId}", "userId" to 42)
    .exec()
```

Update also has a `whereSimple()` method in case you want to provide the query in a traditional way:

```kotlin
update("User", "name" to "Alice")
    .`whereSimple`("_id = ?", 42)
    .exec()
```

## Transactions

There is a special function called `transaction()` which allows you to enclose several database operations in a single SQLite transaction.

```kotlin
transaction {
    // Your transaction code
}
```

The transaction will be marked as successful if no exception was thrown inside the `{}` block.

<table>
<tr><td width="50px" align="center">:penguin:</td>
<td>
<i>If you want to abort a transaction for some reason, just throw <code>TransactionAbortException</code>. You don't need to handle this exception by yourself in this case.</i>
</td>
</tr>
</table>
