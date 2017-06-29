Have you ever been tired of parsing SQLite query results using Android cursors? You have to write lots of boilerplate code just to parse query result rows, and enclose it in countless `try..finally` blocks to properly close all opened resources.

Anko provides lots of extension functions to simplify working with SQLite databases.

## Contents

* [Using Anko SQLite in your project](#using-anko-sqlite-in-your-project)
* [Accessing database](#accessing-database)
* [Creating and dropping tables](#creating-and-dropping-tables)
* [Inserting data](#inserting-data)
* [Querying data](#querying-data)
* [Parsing query result](#parsing-query-result)
* [Custom row parsers](#custom-row-parsers)
* [Cursor streams](#cursor-streams)
* [Updating values](#updating-values)
* [Transactions](#transactions)


## Using Anko SQLite in your project

Add the `anko-sqlite` dependency to your `build.gradle`:

```groovy
dependencies {
    compile "org.jetbrains.anko:anko-sqlite:$anko_version"
}
```

## Accessing the database

If you use `SQLiteOpenHelper`, you generally call `getReadableDatabase()` or `getWritableDatabase()` (result is actually the same in production code), but then you must be sure to call the `close()` method on the received `SQLiteDatabase`. Also, you have to cache the helper class somewhere, and if you use it from several threads, you must be aware of the concurrent access. All this is pretty tough. That is why Android developers are not really keen on default SQLite API and prefer to use fairly expensive wrappers such as ORMs instead.

Anko provides a special class `ManagedSQLiteOpenHelper` that seamlessly replaces the default one. Here's how you can use it:

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
        // Here you create tables
        db?.createTable("Customer", ifNotExists = true, 
                    "id" to INTEGER + PRIMARY_KEY + UNIQUE,
                    "name" to TEXT,
                    "photo" to BLOB)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Here you can upgrade tables, as usual
        db?.dropTable("User", true)
    }
}

// Access property for Context
val Context.database: MyDatabaseOpenHelper
    get() = MyDatabaseOpenHelper.getInstance(getApplicationContext())
```

So what's the sense? Instead of enclosing your code into `try` blocks, now you can just write this:

```kotlin
database.use {
    // `this` is a SQLiteDatabase instance
}
```

The database will definitely be closed after executing all code inside `{}`.

Asynchronous call example:

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
