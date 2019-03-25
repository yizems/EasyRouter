package cn.yzl.android.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cn.yzl.android.easyrouter.EasyRouter
import cn.yzl.android.easyrouter.annotation.Router
import kotlinx.android.synthetic.main.activity_main.*

@Router(path = "main")
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bt_1.setOnClickListener {
            startActivity(
                    EasyRouter.with(this@MainActivity)
                            .setPath("sec")
                            .setParamJson("假装是一个json格式数据 =.=")
                            .intent
            )
        }

        bt_2.setOnClickListener {
            startActivity(
                    EasyRouter.with(this@MainActivity)
                            .setPath("library/main")
                            .intent
            )
        }

    }

}
