import java.io.File

/**
 * Helper class to manage environment variables for Gradle builds.
 */
object EnvHelper {
    private val envMap: MutableMap<String, String> = mutableMapOf()

    init {
        envMap.putAll(System.getenv())

        val defaultDotenvFile = File(".env")
        if (defaultDotenvFile.exists()) {
            loadDotenvFile(defaultDotenvFile)
        }
    }

    /**
     * Load environment variables from a custom .env file.
     *
     * @param filePath Path to the custom .env file.
     */
    fun loadCustomDotenv(filePath: String) {
        val customDotenvFile = File(filePath)
        if (customDotenvFile.exists()) {
            loadDotenvFile(customDotenvFile)
        } else {
            throw IllegalArgumentException("Specified .env file not found at path: $filePath")
        }
    }

    /**
     * Load environment variables from a .env file.
     */
    private fun loadDotenvFile(file: File) {
        file.forEachLine { line ->
            val trimmed = line.trim()
            if (trimmed.isBlank() || trimmed.startsWith("#")) return@forEachLine
            val (key, value) = trimmed.split("=", limit = 2)
            envMap[key.trim()] = value.trim()
        }
    }

    /**
     * Retrieve an environment variable, with an optional default value.
     *
     * @param key The key of the environment variable.
     * @param defaultValue The default value to return if the key is not found.
     */
    fun getEnv(key: String, defaultValue: String? = null): String {
        return envMap[key] ?: defaultValue ?:
            throw IllegalArgumentException("Environment variable '$key' is not set and no default value provided.")
    }

    /**
     * Validate required environment variables.
     *
     * @param requiredKeys List of required environment variable keys.
     */
    fun validate(vararg requiredKeys: String) {
        val missingKeys = requiredKeys.filter { envMap[it] == null }
        check(missingKeys.isEmpty()) {
            "Missing required environment variables: ${missingKeys.joinToString(", ")}"
        }
    }
}

