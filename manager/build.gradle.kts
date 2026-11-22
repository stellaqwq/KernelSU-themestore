plugins {
    alias(libs.plugins.agp.app) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.compose.compiler) apply false
}

extra["androidMinSdkVersion"] = 31
extra["androidTargetSdkVersion"] = 37
extra["androidCompileSdkVersion"] = 37
extra["androidCompileSdkVersionMinor"] = 0
extra["androidBuildToolsVersion"] = "37.0.0"
extra["androidCompileNdkVersion"] = libs.versions.ndk.get()
extra["androidSourceCompatibility"] = JavaVersion.VERSION_21
extra["androidTargetCompatibility"] = JavaVersion.VERSION_21
extra["managerVersionCode"] = getVersionCode()
extra["managerVersionName"] = getVersionName()

fun getGitCommitCount(): Int {
    val process = Runtime.getRuntime().exec(arrayOf("git", "rev-list", "--count", "HEAD"))
    return process.inputStream.bufferedReader().use { it.readText().trim().toInt() }
}

fun getGitDescribe(): String {
    val process = Runtime.getRuntime().exec(arrayOf("git", "describe", "--tags", "--always"))
    return process.inputStream.bufferedReader().use { it.readText().trim() }
}

fun getVersionCode(): Int {
    val commitCount = runCatching { getGitCommitCount() }.getOrDefault(0)
    val fallback = project.findProperty("KSU_VERSION_CODE_FALLBACK")?.toString()?.toIntOrNull()
    // A shallow clone (depth 1) counts as one commit, which would report 30001 for a
    // tag whose upstream code is much higher. Prefer the explicit fallback in that case.
    if (commitCount <= 1 && fallback != null) {
        logger.warn(
            "KernelSU: shallow git clone (${commitCount} commit) — using versionCode $fallback. " +
                "Run 'git fetch --unshallow' to derive it from the real commit count."
        )
        return fallback
    }
    return 30000 + commitCount
}

fun getVersionName(): String {
    return getGitDescribe()
}
