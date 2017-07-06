package info.jiuyou.learnanko.commons

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup
import android.widget.EditText
import info.jiuyou.learnanko.R
import kotlinx.android.synthetic.main.activity_commons.*
import kotlinx.android.synthetic.main.activity_misc.*
import org.jetbrains.anko.*

class MiscActivity : AppCompatActivity(), AnkoLogger {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_misc)

        textView1.textColor = 0xff0000.opaque
        textView2.textColor = 0x99.gray.opaque

        textView1.textSize = 20f
        textView2.textSize = sp(20).toFloat()

        val res1 = dip(100)
        info("dip(100)=$res1")
        val res2 = sp(100)
        info("sp(100)=$res2")
        val res3 = px2dip(100)
        info("px2dip(100)=$res3")
        val res4 = px2sp(100)
        info("px2sp(100)=$res4")


        //以下方式会覆盖掉上面的 setContentView 方法。
        //创建一个竖向的线性布局，添加一个 textView 和 editText 。
        //applyRecursively() 遍历viewGroup中的所有view。
        verticalLayout {
            textView {
                text="textView"
                textSize=20f
            }
            editText {
                hint = "editText"
            }
        }.applyRecursively { view ->
            when (view) {
                is EditText -> view.textSize = 30f
            }
        }
    }
}
