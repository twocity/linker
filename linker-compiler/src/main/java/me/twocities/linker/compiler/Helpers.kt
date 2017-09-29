package me.twocities.linker.compiler

import com.google.auto.common.MoreElements
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements


/**
 * the Utils
 */
fun Element.asTypeElement(): TypeElement = MoreElements.asType(this)

fun TypeElement.packageName(elementUtils: Elements): String {
  return elementUtils.getPackageOf(this).qualifiedName.toString()
}

fun Element.hasAnnotationWithName(annotationName: String): Boolean {
  return this.annotationMirrors.any {
    annotationName == it.annotationType.asElement().simpleName.toString()
  }
}

fun TypeElement.className(elementUtils: Elements): String {
  val packageLen = packageName(elementUtils).length + 1
  return this.qualifiedName.toString().substring(packageLen).replace('.', '$')
}

fun <T> Iterable<T>.hasAnyOf(that: Iterable<T>) = this.any { other -> that.any { other == it } }

/**
 * Borrowed from ButterKnife
 */
fun isSubtypeOfType(typeMirror: TypeMirror, otherType: String): Boolean {
  if (isTypeEqual(typeMirror, otherType)) {
    return true
  }
  if (typeMirror.kind != TypeKind.DECLARED) {
    return false
  }
  val declaredType = typeMirror as DeclaredType
  val typeArguments = declaredType.typeArguments
  if (typeArguments.size > 0) {
    val typeString = StringBuilder(declaredType.asElement().toString())
    typeString.append('<')
    for (i in typeArguments.indices) {
      if (i > 0) {
        typeString.append(',')
      }
      typeString.append('?')
    }
    typeString.append('>')
    if (typeString.toString() == otherType) {
      return true
    }
  }
  val element = declaredType.asElement() as? TypeElement ?: return false
  val typeElement = element
  val superType = typeElement.superclass
  if (isSubtypeOfType(superType, otherType)) {
    return true
  }
  return typeElement.interfaces.any { isSubtypeOfType(it, otherType) }
}

private fun isTypeEqual(typeMirror: TypeMirror, otherType: String): Boolean {
  return otherType == typeMirror.toString()
}