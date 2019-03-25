package cn.yzl.android.samplelibrary

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cn.yzl.android.easyrouter.annotation.Router

@Router(path = "library/main")
class LibraryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)
        title = "我来自library"
    }
}
