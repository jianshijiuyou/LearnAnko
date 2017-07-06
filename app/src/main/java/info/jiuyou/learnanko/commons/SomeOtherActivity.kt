package info.jiuyou.learnanko.commons

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import info.jiuyou.learnanko.R
import org.jetbrains.anko.toast

class SomeOtherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_some_other)

        toast("id:"+intent.getIntExtra("id",-1))
    }
}
