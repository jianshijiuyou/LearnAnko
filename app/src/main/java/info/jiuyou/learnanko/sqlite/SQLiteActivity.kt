package info.jiuyou.learnanko.sqlite

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import info.jiuyou.learnanko.R
import kotlinx.android.synthetic.main.activity_sqlite.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.db.*
import org.jetbrains.anko.info

class SQLiteActivity : AppCompatActivity(), AnkoLogger {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sqlite)

        btnCreateTable.setOnClickListener {

            database.use {
                info("createTable start")
                createTable("Customer", true,
                        "id" to INTEGER + PRIMARY_KEY,
                        "name" to TEXT,
                        "photo" to BLOB)
                info("createTable end")
            }

        }

        btnDropTable.setOnClickListener {
            database.use {
                info("dropTable start")
                dropTable("Customer", true)
                info("dropTable end")
            }
        }

        btnAddData.setOnClickListener {
            database.use {
                insert("User",
                        "id" to 123,
                        "name" to "John",
                        "email" to "user@domain.org")
                insert("User",
                        "id" to 321,
                        "name" to "jack",
                        "email" to "jack@gmail.com")
            }
        }

        btnSelect.setOnClickListener {
            //            database.use {
//                select("User")
//                        .column("email")
//                        .whereArgs("name = {userName}","userName" to "jack")
//                        .parseList(StringParser)
//                        .forEach {
//                            info { "email:$it" }
//                        }
//            }

//            database.use {
//                select("User")
//                        .column("email")
//                        .exec {
//                            while (moveToNext()){
//                                info("email:"+getString(0))
//                            }
//                        }
//            }


//            database.use {
//                select("User")
//                        .parseList(rowParser { id: Long, name: String, email: String -> User(id, name, email) })
//                        .forEach {
//                            info { it }
//                        }
//            }

//            database.use {
//                select("User")
//                        .exec {
//                            for (item in asSequence()) {
//                                info("id=${item[0]},name=${item[1]},email=${item[2]}")
//                            }
//                        }
//            }

            database.use {
                select("User")
                        .exec {
                            for (item in asMapSequence()) {
                                info("id=${item["id"]},name=${item["name"]},email=${item["email"]}")
                            }
                        }
            }
        }

        btnUpdate.setOnClickListener {
//            database.use {
//                update("User", "name" to "zhangsan")
//                        .whereArgs("id = {userId}", "userId" to 123)
//                        .exec()
//            }

            database.use {
                update("User", "name" to "zhangsan")
                        .whereSimple("id = ?",123.toString())
                        .exec()
            }
        }



        btnTransactions.setOnClickListener {
            database.use {
                transaction {
                    update("User", "name" to "lisi")
                            .whereSimple("id = ?",123.toString())
                            .exec()

                    throw Exception("error")
                }
            }

//            database.use {
//                transaction {
//                    update("User", "name" to "lisi")
//                            .whereSimple("id = ?",123.toString())
//                            .exec()
//                    throw TransactionAbortException()
//
//                }
//            }
        }
    }

}
