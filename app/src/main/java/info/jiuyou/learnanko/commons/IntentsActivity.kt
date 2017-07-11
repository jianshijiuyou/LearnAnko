package info.jiuyou.learnanko.commons

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import info.jiuyou.learnanko.R
import kotlinx.android.synthetic.main.activity_intents.*
import org.jetbrains.anko.*

class IntentsActivity : AppCompatActivity(), AnkoLogger {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intents)

        btnSA.setOnClickListener {
            //            一般方法
//            val intent = Intent(this, SomeOtherActivity::class.java)
//            intent.putExtra("id", 5)
//            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
//            startActivity(intent)

            startActivity(intentFor<SomeOtherActivity>("id" to 5).singleTop())

        }

        btnCall.setOnClickListener {
            val res = makeCall("10086")
            info("res:$res")
        }

        btnSMS.setOnClickListener {
            val res = sendSMS("10086", "test")
            info("res:$res")
        }

        btnURL.setOnClickListener {
            val res = browse("https://www.github.com")
            info("res:$res")
        }

        btnShare.setOnClickListener {
            val res = share("share test", "is subject")
            info("res:$res")
        }

        btnEmail.setOnClickListener {
            val res = email("xxxxx@xxx.com", "is subject", "is content")
            info("res:$res")
        }
    }
}
