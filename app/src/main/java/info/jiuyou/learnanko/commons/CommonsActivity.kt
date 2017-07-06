package info.jiuyou.learnanko.commons

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import info.jiuyou.learnanko.R
import kotlinx.android.synthetic.main.activity_commons.*
import org.jetbrains.anko.startActivity

class CommonsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_commons)

        btnIntents.setOnClickListener {
            startActivity<IntentsActivity>()
        }

        btnDialogs.setOnClickListener {
            startActivity<DialogsActivity>()
        }

        btnLog.setOnClickListener {
            startActivity<LoggingActivity>()
        }

        btnRes.setOnClickListener {
            startActivity<MiscActivity>()
        }
    }
}
