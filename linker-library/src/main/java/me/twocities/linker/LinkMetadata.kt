package me.twocities.linker

import me.twocities.linker.annotations.Link
import me.twocities.linker.annotations.LinkQuery
import java.util.regex.Pattern

/**
 * A link's metadata, contains the class of activity annotated with [Link],
 * paths defined in link, queries annotated with [LinkQuery].
 */
class LinkMetadata(val rawLink: String, val target: Class<*>,
    private val pathParams: Set<String> = setOf(),
    private val queryParams: Set<QueryParam> = setOf()) {
  private val rawUri = DeepLinkUri.parse(rawLink)
  private val regex by lazy {
    Pattern.compile(schemeHostAndPath(rawUri).replace(Regex(PARAM_REGEX), PARAM_VALUE))
  }

  init {
    requireNotNull(rawUri, { "Illegal rawLink: $rawLink" })
  }

  companion object {
    private val PARAM_VALUE = "([a-zA-Z0-9_#'!+%~,\\-\\.\\@\\$\\:]+)"
    private val PARAM = "[a-zA-Z][a-zA-Z0-9_-]*"
    private val PARAM_REGEX = "%7B($PARAM)%7D"

    private fun schemeHostAndPath(
        uri: DeepLinkUri) = "${uri.scheme()}://${uri.encodedHost()}${uri.encodedPath()}"
  }

  /**
   * Predicate `link` can matches with [rawLink]
   */
  fun matches(link: String): Boolean {
    println("====")
    println(regex)
    println("====")
    val inputUri = DeepLinkUri.parse(link)
    return inputUri != null && regex.matcher(schemeHostAndPath(inputUri)).matches()
  }

  /**
   * Parse given link's params to a map
   */
  @Throws(IllegalLinkException::class)
  fun parseParams(input: String): Map<String, String> {
    val inputUri = DeepLinkUri.parse(input)
    requireNotNull(inputUri, { "Unexpected error" })

    val map = mutableMapOf<String, String>()
    queryParams.forEach {
      parseQueryValue(it, inputUri.queryParameter(it.name), map, inputUri)
    }
    parsePathParams(inputUri, map)
    return map
  }

  @Throws(IllegalLinkException::class)
  private fun parseQueryValue(param: QueryParam, value: String?, map: MutableMap<String, String>,
      uri: DeepLinkUri) {
    if (value == null) {
      // validate queries, throw exception when required value is absent
      if (param.required) {
        throw IllegalLinkException("Missing ${param.name} in given link.($uri)")
      }
    } else {
      map[param.name] = value
    }
  }

  private fun parsePathParams(uri: DeepLinkUri, map: MutableMap<String, String>) {
    val matcher = regex.matcher(schemeHostAndPath(uri))
    if (matcher.matches()) {
      var i = 1
      pathParams.mapNotNull {
        val value = matcher.group(i++)
        if (value.isNotBlank()) it.to(value) else null
      }.forEach { map[it.first] = it.second }
    }
  }
}

data class QueryParam(val name: String, val required: Boolean)
