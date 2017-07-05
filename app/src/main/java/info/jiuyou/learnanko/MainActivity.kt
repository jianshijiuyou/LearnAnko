package info.jiuyou.learnanko

import android.Manifest
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.PermissionListener
import info.jiuyou.learnanko.commons.IntentsActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnAnkoCommons.setOnClickListener {
            startActivity<IntentsActivity>()
        }

        btnAnkoLayouts.setOnClickListener { toast("anko layouts") }

        btnAnkoSQLite.setOnClickListener { toast("anko sqlite") }

        btnAnkoCoroutines.setOnClickListener { toast("anko coroutines") }



        AndPermission.with(this@MainActivity)
                .requestCode(100)
                .permission(Manifest.permission.CALL_PHONE,Manifest.permission.SEND_SMS)
                .callback(object : PermissionListener {
                    override fun onSucceed(requestCode: Int, grantPermissions: MutableList<String>) {

                    }

                    override fun onFailed(requestCode: Int, deniedPermissions: MutableList<String>) {

                    }

                })
                .start()


    }
}
