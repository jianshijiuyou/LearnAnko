package info.jiuyou.learnanko.commons

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import info.jiuyou.learnanko.R
import kotlinx.android.synthetic.main.activity_logging.*
import org.jetbrains.anko.*

class LoggingActivity : AppCompatActivity(), AnkoLogger {
    private val log = AnkoLogger(this::class.java)
    private val logCustomTag = AnkoLogger("my_tag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logging)

        btnInherit.setOnClickListener {
            info("info log")
            warn(null) // 打印 "null"
            //这里debug打印不出来，因为内部调用了Log.isLoggable方法，具体自行google
            debug("debug log")
        }

        btnObj.setOnClickListener {
            log.info{"info log"}
            log.warn(null)
            logCustomTag.info("info log")
            logCustomTag.warn(null)
        }
    }
}
