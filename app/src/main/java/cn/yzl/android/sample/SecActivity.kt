package cn.yzl.android.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cn.yzl.android.easyrouter.EasyRouter
import cn.yzl.android.easyrouter.annotation.Router
import kotlinx.android.synthetic.main.activity_sec.*

@Router(path = "sec")
class SecActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sec)
        title = "我是它亲兄弟"
        tv.text = EasyRouter.getParams(intent)
    }
}
