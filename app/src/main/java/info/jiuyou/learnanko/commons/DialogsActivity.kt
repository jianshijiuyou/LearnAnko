package info.jiuyou.learnanko.commons

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import info.jiuyou.learnanko.R
import kotlinx.android.synthetic.main.activity_dialogs.*
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.Appcompat

class DialogsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialogs)


        btnToast.setOnClickListener {
            //toast("Hi there!")
            //toast(R.string.message)
            longToast("长长的Toast")
        }

        btnDefDialog.setOnClickListener {
            //            alert("这是长长长的内容", "这是短短的标题") {
//                yesButton { toast("确定") }
//                noButton {}
//            }.show()

            alert("这是长长长的内容", "default dialog") {
                positiveButton("前进") {
                    toast("前进")
                }
                negativeButton("后退") {
                    toast("后退")
                }
            }.show()
        }

        btnAppDialog.setOnClickListener {
            alert(Appcompat, "这是长长长的内容", "Appcompat style dialog") {
                yesButton { toast("确定") }
                noButton {}
            }.show()

        }

        btnCustomDialog.setOnClickListener {
            alert {
                customView {
                    val et = editText()
                    et.hint = "custom dialog"
                }
            }.show()
        }

        btnSelectorsDialog.setOnClickListener {
            val countries = listOf("item1", "item2", "item3", "item4")
            selector("单项选择", countries) { dialogInterface, i ->
                toast("你选择了 ${countries[i]} ！！！")
            }
        }

        btnProgressDialog.setOnClickListener {
            val dialog = indeterminateProgressDialog(message = "正在加载中…", title = "提示")
            //设置进度
            dialog.progress = 10
            //不带进度条的（转圈）
            //indeterminateProgressDialog(message = "正在加载中…", title = "提示")
        }
    }
}
