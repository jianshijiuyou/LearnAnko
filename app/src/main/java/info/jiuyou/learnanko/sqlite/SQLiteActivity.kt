package info.jiuyou.learnanko.sqlite

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import info.jiuyou.learnanko.R
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.coroutines.experimental.bg
import org.jetbrains.anko.info

class SQLiteActivity : AppCompatActivity(),AnkoLogger {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sqlite)

        async(UI) {
            info("===async==="+Thread.currentThread().name)
            val result = bg {
                info("===bg==="+Thread.currentThread().name)
                database.use {
                    info("===use==="+Thread.currentThread().name)
                }
            }

        }
    }

}
