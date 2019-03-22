package cn.yzl.android.easyrouter_register

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * 自动注册插件入口
 */
public class RegisterPlugin implements Plugin<Project> {

    public static final String PLUGIN_NAME = 'easy_router'
    public static final String EXT_NAME = 'easy_router'


    @Override
    void apply(Project project) {
        println "project(${project.name}) apply ${PLUGIN_NAME} plugin"
        def targetInfo = project.extensions.create(EXT_NAME, RegisterTargetInfo)

        def transformImpl = new RegisterTransform(project)
        transformImpl.targetInfo = targetInfo
        def android = project.extensions.getByType(AppExtension)
        android.registerTransform(transformImpl)
    }

//    /**
//     * 获取配置的 目标类
//     * @param project
//     * @param transformImpl
//     * @return
//     */
//    static RegisterTargetInfo init(Project project, RegisterTransform transformImpl) {
//        RegisterTargetInfo targetInfo = project.extensions.findByName(EXT_NAME) as RegisterTargetInfo
//        transformImpl.targetInfo = targetInfo
//        return targetInfo
//    }

}