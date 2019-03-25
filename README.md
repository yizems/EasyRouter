

## EasyRouter

非组件化通讯路由,只是用来处理页面跳转等基本操作,最多也就支持fragment了,不会支持View等对象传递

- 如果你要强大的路由,请移步[ARouter](https://github.com/alibaba/ARouter)
- 如果你要组件化,请移步[CC](https://github.com/luckybilly/CC)

初衷:简单可控的路由组件库

## 更新日志


### 0.1.0

- 完成 Transform Api + ASM 配合 修改字节码文件,添加路由
- EasyRouter.java


## Future

- 序列化参数传递?
- `startActivityForResult`支持
- Requst 封装
- 拦截器和日志
- 自动降级?错误处理?
- 支持 Fragment/其他?

## 添加依赖

[![](https://jitpack.io/v/yizems/EasyRouter.svg)](https://jitpack.io/#yizems/EasyRouter)



```gradle
classpath "com.github.yizems.EasyRouter:easyrouter_register:$version"



//app

apply plugin: 'easyrouter'

implementation "com.github.yizems.EasyRouter:easyrouter:$version"

```


## 使用方式

用`Router`标记activity
```kotlin

@Router(path = "sec")
class SecActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sec)
        title = "我是它亲兄弟"
        tv.text = EasyRouter.getParams(intent)
    }
}
```

通过`EasyRouter`获取启动`Intent`
```kotlin
bt_1.setOnClickListener {
            startActivity(
                    EasyRouter.with(this@MainActivity)
                            .setPath("sec")
                            .setParamJson("假装是一个json格式数据 =.=")
                            .intent
            )
        }

```


## 原理简述

1. 通过Transform Api 添加编译过程
2. 自定义Transform并注册,获取字节码文件,通过`ASM` 访问并修改字节码文件
3. 用ASM扫描并记录被标记了`Router`的文件,并添加到`RouterManager`中
4. 通过EasyRouter 访问 Router


这部分最难的还是 `Transform Api`和`ASM`对字节码的处理

### 为什么是`Transform Api`

因为可以扫描 jar包,编译时注解只能扫描源文件

### 为什么是ASM

1. 因为都不会
2. 因为ASM是官方转换框架
3. javassist 的确方便,但是不知道是否也有坑=.=,主要是针对kotlin

## 参考

[CC](https://github.com/luckybilly/CC)

[AutoRegister](https://github.com/luckybilly/AutoRegister)

[AutoRegister 文章](https://juejin.im/post/5a2b95b96fb9a045284669a9)



## ASM

[官网](https://asm.ow2.io/)

[ASM-Tinker](https://blog.csdn.net/l2show/article/details/54846682)


## Javassist

对kotlin的支持还未知

https://www.jianshu.com/p/417589a561da




http://www.blogjava.net/ldd600/archive/2008/06/11/207162.html