package dev.gaphunter.feignfallbackconfigcompanion.detect

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import dev.gaphunter.feignfallbackconfigcompanion.config.CircuitBreakerConfigLocator
import dev.gaphunter.feignfallbackconfigcompanion.config.CircuitBreakerEnabledScanner
import dev.gaphunter.feignfallbackconfigcompanion.model.FeignFallbackHit
import org.jetbrains.kotlin.psi.KtAnnotated
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid

/** Kotlin counterpart of [JavaFeignFallbackFinder]. */
object KotlinFeignFallbackFinder {

    fun findAll(file: PsiFile): List<FeignFallbackHit> {
        if (file !is KtFile) return emptyList()
        val virtualFile = file.virtualFile ?: return emptyList()
        val hits = mutableListOf<FeignFallbackHit>()
        var configChecked = false
        var circuitBreakerEnabled = false

        file.accept(object : KtTreeVisitorVoid() {
            override fun visitClassOrObject(classOrObject: KtClassOrObject) {
                super.visitClassOrObject(classOrObject)
                val entry = findFallbackAnnotation(classOrObject) ?: return

                if (!configChecked) {
                    circuitBreakerEnabled = CircuitBreakerConfigLocator.findConfigTexts(virtualFile)
                        .any { CircuitBreakerEnabledScanner.isEnabledIn(it) }
                    configChecked = true
                }
                if (!circuitBreakerEnabled) {
                    hits += FeignFallbackHit(leafOf(entry))
                }
            }
        })
        return hits
    }

    private fun findFallbackAnnotation(owner: KtAnnotated): KtAnnotationEntry? =
        owner.annotationEntries.firstOrNull { entry ->
            entry.shortName?.asString() == "FeignClient" &&
                entry.valueArguments.any { it.getArgumentName()?.asName?.asString() in setOf("fallback", "fallbackFactory") }
        }

    /** Descends to a real leaf PSI element -- LineMarkerInfo must never anchor on a composite node (SDK_GOTCHAS.md SS20). */
    private fun leafOf(element: PsiElement): PsiElement {
        var current = element
        while (current.firstChild != null) current = current.firstChild
        return current
    }
}
