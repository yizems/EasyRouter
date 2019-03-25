package cn.yzl.android.easyrouter

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cn.yzl.android.easyrouter.annotation.Router

@Router(path = "main")
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
