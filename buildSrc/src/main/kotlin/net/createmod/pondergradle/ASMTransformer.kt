package net.createmod.pondergradle

import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.util.CheckClassAdapter

class ASMTransformer private constructor() {
    companion object {
        private val transformers = mutableSetOf<IClassTransformer>()

        internal fun transformClass(project: Project, bytes: ByteArray): ByteArray {
            // Get project type
            val projectType = SubprojectType.getProjectType(project)

            val node = ClassNode()
            ClassReader(bytes).accept(node, 0)

            for (transformer in transformers)
                transformer.transform(projectType, node)

            // Verify the bytecode is valid
            val byteArray = ClassWriter(0).also { node.accept(it) }.toByteArray()
            ClassReader(byteArray).accept(CheckClassAdapter(null), 0)
            return byteArray
        }

        /**
         * Adds a transformer that extends {@see IClassTransformer}, Will never add the same transformer twice
         * as this is backed by a set of IClassTransformer's.
         */
        fun addTransformer(instance: IClassTransformer) {
            transformers.add(instance)
        }
    }
}
