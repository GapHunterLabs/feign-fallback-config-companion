package dev.gaphunter.feignfallbackconfigcompanion.detect

import com.intellij.psi.JavaRecursiveElementWalkingVisitor
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import dev.gaphunter.feignfallbackconfigcompanion.config.CircuitBreakerConfigLocator
import dev.gaphunter.feignfallbackconfigcompanion.config.CircuitBreakerEnabledScanner
import dev.gaphunter.feignfallbackconfigcompanion.model.FeignFallbackHit

/**
 * Finds a `@FeignClient(..., fallback = X.class)` or
 * `(..., fallbackFactory = X.class)` annotation whose project has no
 * `spring.cloud.openfeign.circuitbreaker.enabled=true` (or the legacy
 * `feign.circuitbreaker.enabled=true`) anywhere in a config file
 * reachable by a bounded upward directory walk from the annotated
 * file -- Spring Cloud OpenFeign's own reference docs are explicit
 * that the fallback/fallbackFactory attributes do nothing at all
 * without that property enabled.
 *
 * **v0.1 scope, stated honestly:** matches by simple annotation name
 * (`FeignClient`), not real type resolution -- an unrelated
 * `@FeignClient` from a different library sharing the same simple
 * name is a possible (rare) false positive. Config lookup is a
 * bounded upward walk from the annotated file only (see
 * `CircuitBreakerConfigLocator`) -- a multi-module project where
 * config lives in an unrelated sibling module can produce a false
 * positive here; documented honestly, not silently swept under.
 */
object JavaFeignFallbackFinder {

    fun findAll(file: PsiFile): List<FeignFallbackHit> {
        val virtualFile = file.virtualFile ?: return emptyList()
        val hits = mutableListOf<FeignFallbackHit>()
        var configChecked = false
        var circuitBreakerEnabled = false

        file.accept(object : JavaRecursiveElementWalkingVisitor() {
            override fun visitAnnotation(annotation: PsiAnnotation) {
                super.visitAnnotation(annotation)
                if (annotation.nameReferenceElement?.referenceName != "FeignClient") return
                val hasFallback = annotation.findAttributeValue("fallback") != null ||
                    annotation.findAttributeValue("fallbackFactory") != null
                if (!hasFallback) return

                if (!configChecked) {
                    circuitBreakerEnabled = CircuitBreakerConfigLocator.findConfigTexts(virtualFile)
                        .any { CircuitBreakerEnabledScanner.isEnabledIn(it) }
                    configChecked = true
                }
                if (!circuitBreakerEnabled) {
                    hits += FeignFallbackHit(leafOf(annotation))
                }
            }
        })
        return hits
    }

    /** Descends to a real leaf PSI element -- LineMarkerInfo must never anchor on a composite node (SDK_GOTCHAS.md SS20). */
    private fun leafOf(element: PsiElement): PsiElement {
        var current = element
        while (current.firstChild != null) current = current.firstChild
        return current
    }
}
