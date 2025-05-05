package com.github.novdov.enumautopropertyplugin

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyExpression
import com.jetbrains.python.psi.PyReferenceExpression
import com.jetbrains.python.psi.types.PyClassType
import com.jetbrains.python.psi.types.TypeEvalContext


class EnumAutoPropertyCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().afterLeaf("."),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    processingContext: ProcessingContext,
                    resultSet: CompletionResultSet
                ) {
                    val element = parameters.position
                    val refExpr = getParentReferenceExpression(element)

                    val typeContext =
                        TypeEvalContext.codeAnalysis(element.project, element.containingFile)
                    val qualifier = refExpr?.qualifier ?: return

                    val classType = getClassType(qualifier, typeContext) ?: return
                    if (isAutoEqualityEnumClass(classType.pyClass, typeContext)) {
                        val enumMembers = getEnumMembers(classType.pyClass)
                        for (member in enumMembers) {
                            val propName = "is_${toSnakeCase(member)}"

                            resultSet.addElement(
                                LookupElementBuilder.create(propName)
                                    .withTypeText("property")
                                    .withIcon(AllIcons.Nodes.Property)
                            )
                        }
                    }
                }
            }
        )
    }
}

private fun getParentReferenceExpression(element: PsiElement): PyReferenceExpression? {
    val parent = element.parent
    return parent as? PyReferenceExpression
}

private fun getClassType(expression: PyExpression, context: TypeEvalContext): PyClassType? {
    return context.getType(expression) as? PyClassType
}

private fun isAutoEqualityEnumClass(pyClass: PyClass, context: TypeEvalContext): Boolean {
    for (ancestor in pyClass.getAncestorClasses(context)) {
        if (ancestor.qualifiedName == "AutoEqualityProperty") {
            return true
        }
    }
    return false
}

private fun getEnumMembers(pyClass: PyClass): List<String> {
    val members = mutableListOf<String>()
    for (classAttr in pyClass.classAttributes) {
        if (classAttr.name != null && classAttr.name == classAttr.name!!.uppercase()) {
            members.add(classAttr.name!!)
        }
    }
    return members
}

private fun toSnakeCase(input: String): String {
    if (input == input.lowercase()) {
        return input
    }

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