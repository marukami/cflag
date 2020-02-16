@file:Suppress("UnstableApiUsage")

package dev.tilbrook.keki.gradle.cflag

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.BasePlugin
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputDirectory
import java.io.File

data class BuildFlag(
    val name: String,
    val value: Any
)

fun Project.`compiledFlags`(configure: CFlagPluginExtension.() -> Unit): Unit =
    (this as ExtensionAware).extensions.configure("compiledFlags", configure)

@Suppress("UnstableApiUsage")
open class CFlagPluginExtension(
    objectFactory: ObjectFactory
) {
    val flags: ListProperty<BuildFlag> = objectFactory.listProperty(BuildFlag::class.java)
    fun flags(vararg buildFlag: Pair<String, Any>) {
        flags.set(
            flags.get().apply {
                addAll(buildFlag.map { BuildFlag(it.first, it.second) })
            }
        )
    }
}

@Suppress("UnstableApiUsage")
open class CFlagPlugin : Plugin<Project> {

    override fun apply(rootProject: Project) {
        rootProject.subprojects { project ->

            val extension = project.extensions.create("compiledFlags", CFlagPluginExtension::class.java)

            project.plugins.whenPluginAdded { plugin ->
                if (plugin is BasePlugin) {
                    val android = project.extensions.getByName("android") as BaseExtension
                    android.buildOutputs.forEach { variantOutput ->
                        project.tasks.register(
                            "${variantOutput.name}-BuildFlagAssemble",
                            BuildFlagsTask::class.java,
                            android.defaultConfig.applicationId,
                            extension.flags.get(),
                            project.file("${variantOutput.dirName}/cflag")
                        )
                    }
                }
            }
        }
    }
}

@CacheableTask
class BuildFlagsTask(
    private val packageName: String,
    private vararg val flags: Pair<String, Any>,
    @get:OutputDirectory val outputDirectory: File
) : DefaultTask() {
    override fun getGroup(): String? = "Keki - Build Tools"
    override fun getName(): String = "BuildFlags"
    override fun getDescription(): String? = "Code Generated Build flags that are build and cache safe"

    override fun doLast(action: Action<in Task>): Task {
        val file = FileSpec.builder(packageName, CLASS_NAME)
            .addType(
                TypeSpec.objectBuilder(CLASS_NAME)
                    .apply {
                        addProperties(buildFlagsOf(*flags))
                    }
                    .build()
            )
            .build()

        file.writeTo(outputDirectory)
        return super.doLast(action)
    }

    companion object {
        const val CLASS_NAME = "BuildFlag"
    }
}

fun buildFlagsOf(vararg pairs: Pair<String, Any>): List<PropertySpec> =
    pairs.map { (key, value) ->
        PropertySpec
            .builder(
                name = key,
                type = value::class
            )
            .initializer("$value")
            .build()
    }