package dev.gaphunter.feignfallbackconfigcompanion.config

import com.intellij.openapi.vfs.VirtualFile

/**
 * Looks for Spring Boot config files (`application.properties`/
 * `application.yml`/`application.yaml`, and their `-*` profile
 * variants) near a Java/Kotlin source file declaring `@FeignClient`
 * -- walks up from the source file's own directory to the content
 * root (bounded, never a full-disk/project-wide index scan), same
 * "MAX_DIRS_UP bounded walk" principle already proven in
 * `postman-openapi-drift-companion`'s `OpenApiSpecLocator`. A real
 * Spring Boot project's config almost always lives at
 * `src/main/resources/`, several directories up from a source file
 * deep in a package tree, so the bound is generous enough to reach it.
 *
 * **v0.1 scope, stated honestly:** only checks directories on the
 * path from the source file to the root, not the whole project tree
 * -- a config file under an unrelated sibling module isn't found (a
 * multi-module Gradle/Maven project where Feign clients and the
 * Spring Boot application/config live in different modules). Returns
 * an honestly empty list rather than a false positive in that case.
 */
object CircuitBreakerConfigLocator {

    private val CONFIG_NAMES = listOf(
        "application.properties", "application.yml", "application.yaml",
        "application-default.properties", "application-default.yml", "application-default.yaml",
    )
    private const val MAX_DIRS_UP = 8

    fun findConfigTexts(sourceFile: VirtualFile): List<String> {
        val texts = mutableListOf<String>()
        var dir = sourceFile.parent
        var hops = 0
        while (dir != null && hops < MAX_DIRS_UP) {
            for (name in CONFIG_NAMES) {
                val candidate = dir.findChild(name)
                if (candidate != null && !candidate.isDirectory) {
                    runCatching { String(candidate.contentsToByteArray(), Charsets.UTF_8) }
                        .getOrNull()
                        ?.let { texts += it }
                }
            }
            // Also check the conventional src/main/resources sibling from
            // any directory on the path up (covers the common case where
            // the source file's own ancestor chain is .../src/main/java/...
            // and config lives in the parallel .../src/main/resources/).
            dir.findChild("resources")?.takeIf { it.isDirectory }?.let { resourcesDir ->
                for (name in CONFIG_NAMES) {
                    resourcesDir.findChild(name)?.takeIf { !it.isDirectory }?.let { candidate ->
                        runCatching { String(candidate.contentsToByteArray(), Charsets.UTF_8) }
                            .getOrNull()
                            ?.let { texts += it }
                    }
                }
            }
            dir = dir.parent
            hops++
        }
        return texts
    }
}
