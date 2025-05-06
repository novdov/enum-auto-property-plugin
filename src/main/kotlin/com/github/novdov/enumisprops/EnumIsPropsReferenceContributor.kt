package com.github.novdov.enumisprops

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import com.jetbrains.python.psi.PyReferenceExpression
import com.jetbrains.python.psi.impl.references.PyReferenceImpl
import com.jetbrains.python.psi.resolve.PyResolveContext
import com.jetbrains.python.psi.types.PyClassType
import com.jetbrains.python.psi.types.TypeEvalContext
import org.jetbrains.annotations.NotNull

class EnumIsPropsReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(PyReferenceExpression::class.java),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    @NotNull element: PsiElement,
                    @NotNull context: ProcessingContext
                ): Array<PsiReference> {
                    val refExpr = element as PyReferenceExpression
                    val refName = refExpr.name ?: return PsiReference.EMPTY_ARRAY

                    if (!refName.startsWith("is_")) {
                        return PsiReference.EMPTY_ARRAY
                    }

                    val qualifier = refExpr.qualifier ?: return PsiReference.EMPTY_ARRAY

                    val typeContext = TypeEvalContext.codeAnalysis(
                        element.project, element.containingFile
                    )
                    val classType = typeContext.getType(qualifier) as? PyClassType
                        ?: return PsiReference.EMPTY_ARRAY
                    val pyClass = classType.pyClass

                    if (!EnumPropertyUtils.isEnumIsPropsClass(pyClass, typeContext)) {
                        return PsiReference.EMPTY_ARRAY
                    }

                    val enumMemberName = refName.substringAfterLast("_")
                    val enumMember = EnumPropertyUtils.findEnumMember(pyClass, enumMemberName)

                    if (enumMember == null) {
                        return PsiReference.EMPTY_ARRAY
                    }

                    return arrayOf(
                        PyReferenceImpl(
                            refExpr,
                            PyResolveContext.defaultContext(typeContext)
                        )
                    )
                }
            }
        )
    }
}