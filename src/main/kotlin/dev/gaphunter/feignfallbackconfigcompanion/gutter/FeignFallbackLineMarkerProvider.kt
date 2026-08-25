package dev.gaphunter.feignfallbackconfigcompanion.gutter

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import dev.gaphunter.feignfallbackconfigcompanion.detect.JavaFeignFallbackFinder
import dev.gaphunter.feignfallbackconfigcompanion.detect.KotlinFeignFallbackFinder
import dev.gaphunter.feignfallbackconfigcompanion.model.FeignFallbackHit
import dev.gaphunter.feignfallbackconfigcompanion.review.ReviewPrompt

class FeignFallbackLineMarkerProvider : LineMarkerProviderDescriptor(), DumbAware {

    override fun getName(): String = "@FeignClient fallback without circuitbreaker.enabled"

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? = null

    override fun collectSlowLineMarkers(elements: MutableList<out PsiElement>, result: MutableCollection<in LineMarkerInfo<*>>) {
        val file = elements.firstOrNull()?.containingFile ?: return
        val hits = when (file.language.id) {
            "JAVA" -> JavaFeignFallbackFinder.findAll(file)
            "kotlin" -> KotlinFeignFallbackFinder.findAll(file)
            else -> emptyList()
        }
        if (hits.isEmpty()) return

        val hitsByElement = hits.associateBy { it.anchorElement }
        for (element in elements) {
            val hit = hitsByElement[element] ?: continue
            result.add(buildMarker(hit))

            val path = file.virtualFile?.path ?: continue
            val lineNumber = file.viewProvider.document?.getLineNumber(element.textRange.startOffset) ?: -1
            ReviewPrompt.recordHit(file.project, "$path:$lineNumber")
        }
    }

    private fun buildMarker(hit: FeignFallbackHit): LineMarkerInfo<PsiElement> {
        val tooltip = "This @FeignClient declares a fallback/fallbackFactory, but no " +
            "spring.cloud.openfeign.circuitbreaker.enabled=true (or the legacy " +
            "feign.circuitbreaker.enabled=true) was found in a reachable config file -- Spring Cloud " +
            "OpenFeign's own docs say the fallback does nothing at all without that property enabled"
        return LineMarkerInfo(
            hit.anchorElement,
            hit.anchorElement.textRange,
            FeignFallbackIcons.RISK,
            { _: PsiElement -> tooltip },
            null,
            GutterIconRenderer.Alignment.RIGHT,
            { tooltip },
        )
    }
}
