package info.jiuyou.learnanko.coroutines

import android.os.Bundle
import android.os.SystemClock
import android.support.v7.app.AppCompatActivity
import info.jiuyou.learnanko.R
import kotlinx.android.synthetic.main.activity_basics.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import org.jetbrains.anko.coroutines.experimental.Ref
import org.jetbrains.anko.coroutines.experimental.asReference
import org.jetbrains.anko.coroutines.experimental.bg
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.toast


class CoroutinesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_basics)
        //持有 Activity 对象的 弱引用（WeakReference）
        val ref: Ref<CoroutinesActivity> = this.asReference()

        async(UI) {
            //耗时操作
            val data = getData()
            //用 ref 替换 this@Activity
            ref().showData(data)
        }


        btnBg.onClick {
            async(UI) {
                val data = bg {
                    //子线程
                    SystemClock.sleep(3000)
                    return@bg "网络数据"
                }
                //UI 线程
                ref().showData(data.await())
            }
        }
    }

    fun showData(data: String) {
        toast(data)
    }

    suspend fun getData(): String {
        delay(3000L)
        return "数据"
    }
}

