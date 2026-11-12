package net.createmod.pondergradle.transformers

import net.createmod.pondergradle.IClassTransformer
import net.createmod.pondergradle.SubprojectType
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode

class EnvironmentAnnotationTransformer : IClassTransformer {
    companion object {
        private val annotations = mapOf(
            SubprojectType.FABRIC to Pair("Lnet/fabricmc/api/Environment;", "Lnet/fabricmc/api/EnvType;"),
            SubprojectType.FORGE to Pair("Lnet/minecraftforge/api/distmarker/OnlyIn;", "Lnet/minecraftforge/api/distmarker/Dist;"),
            SubprojectType.NEOFORGE to Pair("Lnet/neoforged/api/distmarker/OnlyIn;", "Lnet/neoforged/api/distmarker/Dist;"),
        )
    }

    override fun transform(project: SubprojectType, node: ClassNode) {
        if (project == SubprojectType.COMMON) return

        node.visibleAnnotations = node.visibleAnnotations ?: mutableListOf()
        node.invisibleAnnotations = node.invisibleAnnotations ?: mutableListOf()
        process(project, node.visibleAnnotations, node.invisibleAnnotations)

        for (fieldNode in node.fields) {
            fieldNode.visibleAnnotations = fieldNode.visibleAnnotations ?: mutableListOf()
            fieldNode.invisibleAnnotations = fieldNode.invisibleAnnotations ?: mutableListOf()
            process(project, fieldNode.visibleAnnotations, fieldNode.invisibleAnnotations)
        }

        for (methodNode in node.methods) {
            methodNode.visibleAnnotations = methodNode.visibleAnnotations ?: mutableListOf()
            methodNode.invisibleAnnotations = methodNode.invisibleAnnotations ?: mutableListOf()
            process(project, methodNode.visibleAnnotations, methodNode.invisibleAnnotations)
        }
    }

    private fun process(project: SubprojectType, visibleAnnotations: MutableList<AnnotationNode>, invisibleAnnotations: MutableList<AnnotationNode>) {
        val allAnnotationNodes = mutableListOf<AnnotationNode>()
        visibleAnnotations.let { allAnnotationNodes.addAll(it) }
        invisibleAnnotations.let { allAnnotationNodes.addAll(it) }

        val annotationsList = if (project == SubprojectType.FABRIC) invisibleAnnotations else visibleAnnotations
        val (descriptor, enumDesc) = annotations[project]!!

        for (annotation in allAnnotationNodes) {
            if (annotation.desc == "Lnet/createmod/catnip/annotations/ClientOnly;") {
                val newAnnotation = AnnotationNode(descriptor)
                newAnnotation.values = mutableListOf<Any>()
                newAnnotation.values.add("value")
                newAnnotation.values.add(arrayOf(enumDesc, "CLIENT"))

                annotationsList.add(newAnnotation)
                invisibleAnnotations.remove(annotation)
            }
        }
    }
}
