package net.createmod.pondergradle

import org.objectweb.asm.tree.ClassNode

interface IClassTransformer {
    fun transform(project: SubprojectType, node: ClassNode)
}
