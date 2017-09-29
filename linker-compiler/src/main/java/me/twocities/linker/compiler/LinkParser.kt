package me.twocities.linker.compiler

import me.twocities.linker.annotations.Link
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier.ABSTRACT
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.STATIC
import javax.lang.model.element.TypeElement

/**
 * Parse [Link] annotated element into a map
 */
class LinkParser(private val context: Context) {

  companion object {
    private val ACTIVITY = "android.app.Activity"
  }

  fun parse(
      elements: Iterable<Element>): Map<TypeElement, String> {
    val linkMap = mutableMapOf<TypeElement, String>()
    elements.forEach {
      validateElement(it)
      val link = it.getAnnotation(Link::class.java).link
      validateLink(link, it)
      val typeElement = it.asTypeElement()
      if (linkMap[typeElement] != null) {
        context.logger.error("$link has already annotated with $typeElement", it)
      }
      linkMap[typeElement] = link
    }
    return linkMap.toMap()
  }


  private fun validateElement(element: Element): Boolean {
    var valid = true

    if (!element.kind.isClass) {
      valid = false
      context.logger.error("@${Link::class.simpleName} must be annotated with class", element)
    }

    // only work with android's activity
    if (!isSubtypeOfType(element.asType(),
        ACTIVITY)) {
      valid = false
      context.logger.error(
          "@${Link::class.simpleName} ${element.simpleName} must be subclass of Activity",
          element)
    }

    if (element.modifiers.hasAnyOf(listOf(PRIVATE, ABSTRACT, STATIC))) {
      valid = false
      context.logger.error("${element.simpleName} can't be private, abstract, or static.", element)
    }
    return valid
  }

  private fun validateLink(link: String, element: Element): Boolean {
    if (DeepLinkUri.parse(link) == null) {
      context.logger.error("Malformed Uri $link", element)
      return false
    }
    return true
  }

}