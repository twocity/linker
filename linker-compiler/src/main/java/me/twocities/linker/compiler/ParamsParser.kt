package me.twocities.linker.compiler

import me.twocities.linker.annotations.Link
import me.twocities.linker.annotations.LinkModule
import me.twocities.linker.annotations.LinkPath
import me.twocities.linker.annotations.LinkQuery
import java.util.regex.Pattern
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind.CLASS
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.STATIC
import javax.lang.model.element.TypeElement
import kotlin.reflect.KClass

/**
 * Parse elements annotated with [LinkPath], [LinkQuery]
 * The parsed map will be used to generate activity's binder function, and the [LinkModule]'s implementation.
 *
 * @see [ActivityInjectorGenerator]
 * @see [LinkBuilderProcessingStep]
 */
class ParamsParser(private val context: Context,
    private val linkMap: Map<TypeElement, String>) {

  companion object {
    private val PARAM = "[a-zA-Z][a-zA-Z0-9_-]*"
    private val PATH_PARAM = "%7B($PARAM)%7D"
    val PATH_PATTERN_REGEX = Pattern.compile(PATH_PARAM)
    private val PARAM_NAME_REGEX = Pattern.compile(PARAM)
    private val NULLABLE_ANNOTATION_NAME = "Nullable"
    private val STRING = "java.lang.String"
  }

  fun parse(pathElements: Iterable<Element>,
      queryElements: Iterable<Element>): Map<TypeElement, LinkParams> {
    val map = mutableMapOf<TypeElement, LinkParams>()

    val pathMap = parsePaths(pathElements)
    val queryMap = parseQueries(queryElements)
    linkMap.keys.forEach {
      map[it] = LinkParams(linkMap[it]!!, pathMap[it] ?: setOf(),
          queryMap[it] ?: setOf())
    }
    return map.toMap()
  }

  private fun parsePaths(elements: Iterable<Element>): Map<TypeElement, Set<PathAnnotation>> {
    return elements.filter { validatePathElement(it) }
        .map {
          val annotation = it.getAnnotation(LinkPath::class.java)
          it.annotationMirrors
          it.enclosingElement.asTypeElement().to(
              PathAnnotation(annotation.path,
                  it.simpleName.toString()))
        }.groupBy({ it.first }, { it.second })
        .mapValues { it.value.toSet() }
  }

  private fun parseQueries(elements: Iterable<Element>): Map<TypeElement, Set<QueryAnnotation>> {
    return elements.filter { validateQueryElement(it) }
        .map {
          val annotation = it.getAnnotation(LinkQuery::class.java)
          it.annotationMirrors
          it.enclosingElement.asTypeElement().to(
              QueryAnnotation(annotation.name,
                  it.simpleName.toString(),
                  !it.hasAnnotationWithName(
                      NULLABLE_ANNOTATION_NAME)))
        }.groupBy({ it.first }, { it.second })
        .mapValues { it.value.toSet() }
  }

  private fun validatePathElement(e: Element): Boolean {
    var valid = validateElement(e, LinkPath::class)

    val path = e.getAnnotation(LinkPath::class.java).path
    if (!PARAM_NAME_REGEX.matcher(path).matches()) {
      valid = false
      context.logger.error(
          "@${LinkPath::class.simpleName}'s path must match: ${PARAM}, found: $path", e)
    }

    if (valid) {
      val link = linkMap[e.enclosingElement.asTypeElement()]!!
      val pathsOfLink = CacheablePathParser.getOrParse(link)
      if (!pathsOfLink.contains(path)) {
        valid = false
        context.logger.error("${linkMap[e.enclosingElement]!!} does not contains: $path",
            e.enclosingElement)
      }
    }

    return valid
  }

  private fun validateQueryElement(e: Element): Boolean {
    var valid = validateElement(e, LinkQuery::class)
    val name = e.getAnnotation(LinkQuery::class.java).name
    if (!PARAM_NAME_REGEX.matcher(name).matches()) {
      valid = false
      context.logger.error(
          "@${LinkQuery::class.simpleName}'s query name must match: ${PARAM}, found: $name", e)
    }
    return valid
  }


  private fun validateElement(e: Element, clazz: KClass<out Annotation>): Boolean {
    val enclosingElement = e.enclosingElement
    var valid = true
    if (enclosingElement.kind != CLASS) {
      valid = false
      context.logger.error("@${clazz.simpleName} can only be used in class.",
          enclosingElement)
    }

    if (e.modifiers.hasAnyOf(listOf(PRIVATE, STATIC))) {
      valid = false
      context.logger.error(
          "@${clazz.simpleName} ${e.simpleName} can't be private or static.", e)
    }

    if (!STRING.contentEquals(e.asType().toString())) {
      valid = false
      context.logger.error(
          "@${clazz.simpleName} ${e.simpleName} must be type of String, now is ${e.asType()}",
          e)
    }

    if (!linkMap.containsKey(enclosingElement)) {
      valid = false
      context.logger.error(
          "@${clazz.simpleName} can't work standalone, ${enclosingElement.simpleName} should annotated with @${Link::class.simpleName}",
          enclosingElement)
    }

    return valid
  }
}
