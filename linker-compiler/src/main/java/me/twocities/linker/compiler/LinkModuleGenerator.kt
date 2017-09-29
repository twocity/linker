package me.twocities.linker.compiler

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import me.twocities.linker.annotations.LinkModule
import java.io.File
import java.io.IOException
import javax.lang.model.element.TypeElement

/**
 * Generate an implementation of LinkParser
 * The generated class's name was decided by [LinkModule]
 *
 * @see [LinkModule]
 */
class LinkModuleGenerator(private val context: Context,
    private val moduleElement: TypeElement,
    private val linkMap: Map<TypeElement, String>,
    private val paramsMap: Map<TypeElement, LinkParams>
) {

  companion object {
    private val PREFIX_OF_MODULE_CLASS = "Linker"

    fun nameOfModuleClass(annotatedClassName: String): String {
      return PREFIX_OF_MODULE_CLASS + annotatedClassName
    }
  }

  @Throws(IOException::class)
  fun brewKotlin(directory: File) {
    val packageName = moduleElement.packageName(context.elements)
    val className = nameOfModuleClass(
        moduleElement.className(context.elements))
    val clazz = TypeSpec.classBuilder(ClassName(packageName, className))
        .addSuperinterface(LINK_PARSER)

    val property = PropertySpec.builder("list",
        ParameterizedTypeName.get(List::class.asClassName(),
            LINK_METADATA))
        .addModifiers(PRIVATE)

    val propertyInitializer = CodeBlock.Builder()
        .add("listOf(\n")
        .add(metadataInitializer())
        .add("\n)")
        .build()

    val parseFun = FunSpec.builder("parse")
        .addModifiers(OVERRIDE)
        .addParameter("link", String::class)
        .returns(LINK_METADATA.asNullable())
        .addStatement("return list.firstOrNull { it.matches(link) }")
        .build()

    FileSpec.builder(packageName, className)
        .addType(clazz.addProperty(property.initializer(propertyInitializer).build())
            .addFunction(parseFun)
            .build())
        .build().writeTo(directory)
  }

  private fun metadataInitializer(): CodeBlock {
    val codeBlock = CodeBlock.Builder()
    linkMap.asIterable().forEachIndexed { i, entry ->
      val lineBreaker = if (i == linkMap.values.size - 1) "" else ",\n"
      val type = ClassName.bestGuess(entry.key.toString())
      val param = paramsMap[entry.key]!!
      codeBlock.add(
          "%T(rawLink = %S,\n\ttarget = %T::class.java,\n\tpathParams = %L,\n\tqueryParams = %L)%L",
          LINK_METADATA, entry.value,
          type, buildPathSet(
          CacheablePathParser.getOrParse(entry.value)),
          buildQueryParamsArg(param.queries), lineBreaker)
    }
    return codeBlock.build()
  }

  private fun buildPathSet(paths: Set<String>): CodeBlock {
    val codeBlock = CodeBlock.Builder()
    codeBlock.add("setOf(")
    paths.forEachIndexed { index, s ->
      val separator = if (index != paths.size - 1) ", " else ""
      codeBlock.add("%S%L", s, separator)
    }
    codeBlock.add(")")
    return codeBlock.build()
  }

  private fun buildQueryParamsArg(queries: Set<QueryAnnotation>): CodeBlock {
    val codeBlock = CodeBlock.Builder()
    codeBlock.add("setOf(")
    queries.forEachIndexed { index, (name, _, required) ->
      val separator = if (index != queries.size - 1) ", " else ""
      codeBlock.add("%T(%S, %L)%L", QueryParam, name, required, separator)
    }
    codeBlock.add(")")
    return codeBlock.build()
  }
}