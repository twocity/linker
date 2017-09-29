package me.twocities.linker.compiler

import com.google.auto.common.AnnotationMirrors
import com.google.common.collect.ImmutableList
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.AnnotationValue
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.SimpleAnnotationValueVisitor7


/**
 * Borrowed from Dagger, Copyright Google
 * https://github.com/google/dagger/blob/master/compiler/src/main/java/dagger/internal/codegen
 * /MoreAnnotationMirrors.java
 */
private val AS_ANNOTATION_VALUES = object : SimpleAnnotationValueVisitor7<List<AnnotationValue>, String>() {
  override fun visitArray(vals: List<AnnotationValue>,
      elementName: String?): List<AnnotationValue> {
    return vals.toList()
  }

  override fun defaultAction(o: Any?, elementName: String?): ImmutableList<AnnotationValue> {
    throw IllegalArgumentException(elementName + " is not an array: " + o)
  }
}

private val AS_TYPE = object : SimpleAnnotationValueVisitor7<TypeMirror, Void>() {
  override fun visitType(t: TypeMirror, p: Void?): TypeMirror {
    return t
  }

  override fun defaultAction(o: Any?, p: Void?): TypeMirror {
    throw TypeNotPresentException(o!!.toString(), null)
  }
}

fun AnnotationMirror.getTypeValue(elementName: String): Iterable<TypeMirror> {
  return AnnotationMirrors.getAnnotationValue(this, elementName).asAnnotationValues()
      .map { AS_TYPE.visit(it) }
}

private fun AnnotationValue.asAnnotationValues(): Iterable<AnnotationValue> {
  return this.accept(AS_ANNOTATION_VALUES, null)
}

