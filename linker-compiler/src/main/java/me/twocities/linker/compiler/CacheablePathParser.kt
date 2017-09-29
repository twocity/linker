package me.twocities.linker.compiler

/**
 * Parse paths of given link, and then cache the result.
 *
 * Given link: `link://product/detail/{id}/{sub_id}`, the result will be: [id, sub_id]
 */
object CacheablePathParser {
  private val cache = mutableMapOf<String, Set<String>>()

  fun getOrParse(link: String): Set<String> {
    return cache.getOrPut(link) {
      val uri = DeepLinkUri.parse(link)!!
      val matcher = ParamsParser.PATH_PATTERN_REGEX.matcher(uri.encodedHost() + uri.encodedPath())
      val patterns = mutableSetOf<String>()
      while (matcher.find()) {
        patterns.add(matcher.group(1))
      }
      return patterns.toSet()
    }
  }
}