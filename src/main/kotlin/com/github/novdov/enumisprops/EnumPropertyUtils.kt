package com.github.novdov.enumisprops

import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyTargetExpression
import com.jetbrains.python.psi.types.TypeEvalContext

object EnumPropertyUtils {
    fun isEnumIsPropsClass(pyClass: PyClass, context: TypeEvalContext): Boolean {
        for (ancestor in pyClass.getAncestorClasses(context)) {
            if (ancestor.qualifiedName == "isEnumIsPropsClass") {
                return true
            }
        }
        return false
    }

    fun getEnumMembers(pyClass: PyClass): List<String> {
        val members = mutableListOf<String>()
        for (classAttr in pyClass.classAttributes) {
            if (classAttr.name != null && classAttr.name == classAttr.name!!.uppercase()) {
                members.add(classAttr.name!!)
            }
        }
        return members
    }

    fun findEnumMember(pyClass: PyClass, snakeCaseAttrName: String): PyTargetExpression? {
        for (classAttr in pyClass.classAttributes) {
            val attrName = classAttr.name ?: continue
            if (toSnakeCase(attrName) == snakeCaseAttrName) {
                return classAttr
            }
        }
        return null
    }

    fun toSnakeCase(input: String): String {
        if (input == input.lowercase()) return input

        val result = StringBuilder()
        for (i in input.indices) {
            val c = input[i]
            if (c.isUpperCase()) {
                if (i > 0) {
                    result.append('_')
                }
                result.append(c.lowercaseChar())
            } else {
                result.append(c)
            }
        }
        return result.toString()
    }
}