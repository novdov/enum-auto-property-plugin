package com.github.novdov.enumisprops

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import com.jetbrains.python.psi.PyReferenceExpression
import com.jetbrains.python.psi.types.PyClassType
import com.jetbrains.python.psi.types.TypeEvalContext


class EnumIsPropsCompletionContributor : CompletionContributor() {
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

                    val classType = typeContext.getType(qualifier) as? PyClassType ?: return
                    if (EnumPropertyUtils.isEnumIsPropsClass(classType.pyClass, typeContext)) {
                        val enumMembers = EnumPropertyUtils.getEnumMembers(classType.pyClass)
                        for (member in enumMembers) {
                            val propName = "is_${EnumPropertyUtils.toSnakeCase(member)}"

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
