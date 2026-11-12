package net.createmod.pondergradle

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import net.createmod.pondergradle.transformers.EnvironmentAnnotationTransformer
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.Deflater

class PonderGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Register Transformers
        ASMTransformer.addTransformer(EnvironmentAnnotationTransformer())

        project.tasks.named("jar").configure {
            doLast {
                val jar = outputs.files.singleFile

                var architecturyInjectableName = project.name
                if (project.rootProject != project)
                    architecturyInjectableName = project.rootProject.name + "_" + architecturyInjectableName

                val contents = linkedMapOf<String, ByteArray>()
                JarFile(jar).use {
                    it.entries().asIterator().forEach { entry ->
                        if (!entry.isDirectory) {
                            contents[entry.name] = it.getInputStream(entry).readAllBytes()
                        }
                    }
                }

                jar.delete()

                JarOutputStream(jar.outputStream()).use { out ->
                    out.setLevel(Deflater.BEST_COMPRESSION)

                    contents.forEach { var (name, data) = it
                        if (name.contains("architectury_inject_${architecturyInjectableName}_common"))
                            return@forEach

                        if (name.endsWith(".json") || name.endsWith(".mcmeta")) {
                            data = (JsonOutput.toJson(JsonSlurper().parse(data)).toByteArray())
                        } else if (name.endsWith(".class")) {
                            data = ASMTransformer.transformClass(project, data)
                        }

                        out.putNextEntry(JarEntry(name))
                        out.write(data)
                        out.closeEntry()
                    }

                    out.finish()
                    out.close()
                }
            }
        }
    }
}
