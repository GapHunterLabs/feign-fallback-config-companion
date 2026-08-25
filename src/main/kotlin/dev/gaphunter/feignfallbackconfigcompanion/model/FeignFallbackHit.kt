package dev.gaphunter.feignfallbackconfigcompanion.model

import com.intellij.psi.PsiElement

/** One @FeignClient(fallback = ...)/(fallbackFactory = ...) annotation whose project has no circuitbreaker.enabled=true found nearby. */
data class FeignFallbackHit(val anchorElement: PsiElement)
