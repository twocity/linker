package me.twocities.linker.compiler

import com.google.auto.common.BasicAnnotationProcessor.ProcessingStep
import com.google.auto.common.MoreElements
import com.google.auto.common.MoreTypes
import com.google.common.collect.SetMultimap
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.INTERNAL
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import me.twocities.linker.annotations.LinkResolverBuilder
import java.io.File
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind.CLASS
import javax.lang.model.element.ElementKind.INTERFACE
import javax.lang.model.element.TypeElement

/**
 * A [ProcessingStep], handles [LinkResolverBuilder]
 * After processing, a AppLinkResolver's builder class will be generated
 *
 * @see [LinkResolverBuilder]
 */
class LinkBuilderProcessingStep(private val context: Context) : ProcessingStep {
  companion object {
    private val PREFIX_OF_BUILDER = "Linker"
    private val RESOLVER_BUILDER = LinkResolverBuilder::class.simpleName
    private val CONTEXT_NAME = ClassName("android.content", "Context")

    fun nameOfLinkBuilder(annotatedClassName: String): String {
      return PREFIX_OF_BUILDER + annotatedClassName
    }
  }

  override fun process(
      elementsByAnnotation: SetMultimap<Class<out Annotation>, Element>): MutableSet<Element> {
    val elements = elementsByAnnotation[LinkResolverBuilder::class.java]

    if (elements.isEmpty()) {
      context.logger.warn("Can't find ${LinkResolverBuilder::class.simpleName}")
    } else if (elements.size > 1) {
      val modules = elements.joinToString("\n") {
        "@${LinkResolverBuilder::class.simpleName} $it"
      }
      context.logger.error(
          "Found ${elements.size} elements annotated with @${LinkResolverBuilder::class.simpleName}: \n$modules\nrequired only one.")
    } else {
      val element = elements.single()
      if (element.kind != CLASS && element.kind != INTERFACE) {
        context.logger.error(
            "@${RESOLVER_BUILDER} $element must be class or interface, now is ${element.kind}",
            element)
      } else {
        val typeElement = element.asTypeElement()
        val mirror = MoreElements.getAnnotationMirror(element, LinkResolverBuilder::class.java)
        if (mirror.isPresent) {
          val moduleElements = mirror.get().getTypeValue("modules")
              .map { MoreTypes.asTypeElement(it) }
          val kotlinGenerated = context.options[LinkerProcessingStep.KAPT_KOTLIN_GENERATED_OPTION]
          val folder = File(kotlinGenerated)
          generate(typeElement, moduleElements).writeTo(folder)
        }
      }
    }
    return mutableSetOf()
  }

  private fun generate(element: TypeElement, moduleElements: List<TypeElement>): FileSpec {
    val packageName = element.packageName(context.elements)
    val className = nameOfLinkBuilder(
        element.simpleName.toString())
    val clazz = TypeSpec.classBuilder(ClassName(packageName, className))
        .addModifiers(INTERNAL)
        .primaryConstructor(FunSpec.constructorBuilder()
            .addParameter("context",
                CONTEXT_NAME)
            .build())
        .addProperty(PropertySpec.builder("context",
            CONTEXT_NAME, PRIVATE)
            .initializer("context")
            .build())
    clazz.addProperty(PropertySpec.builder("modules",
        ParameterizedTypeName.get(List::class.asClassName(),
            LINK_PARSER), PRIVATE)
        .initializer(
            CodeBlock.Builder().add("listOf(").add(buildModulesInitializer(moduleElements)).add(
                ")").build())
        .build())
        .addProperty(
            PropertySpec.builder("listener", RESOLVER_LISTENER.asNullable())
                .addModifiers(PRIVATE)
                .mutable(true)
                .initializer(CodeBlock.of("null"))
                .build())
        .addProperty(
            PropertySpec.builder("fallbackHandler", FALLBACK_HANDLER.asNullable())
                .addModifiers(PRIVATE)
                .mutable(true)
                .initializer(CodeBlock.of("null"))
                .build())
        .addProperty(
            PropertySpec.builder("interceptors",
                ParameterizedTypeName.get(ClassName("java.util", "ArrayList"),
                    INTERCEPTOR)
                , PRIVATE)
                .initializer(CodeBlock.of("%T()",
                    ParameterizedTypeName.get(ClassName("java.util", "ArrayList"),
                        INTERCEPTOR)))
                .build())

    val thisClassName = ClassName(packageName, className)

    val fallbackHandlerFun = FunSpec.builder("setFallbackHandler")
        .addParameter("handler", FALLBACK_HANDLER)
        .returns(thisClassName)
        .addStatement("this.fallbackHandler = handler")
        .addStatement("return this")
        .build()

    val listenerFun = FunSpec.builder("setListener")
        .addParameter("listener", RESOLVER_LISTENER)
        .returns(thisClassName)
        .addStatement("this.listener = listener")
        .addStatement("return this")
        .build()

    val interceptorFun = FunSpec.builder("addInterceptor")
        .addParameter("interceptor", INTERCEPTOR)
        .returns(thisClassName)
        .addStatement("interceptors.add(interceptor)")
        .addStatement("return this")
        .build()

    val buildFun = FunSpec.builder("build")
        .returns(LINK_RESOLVER)
        .addCode("return %T(context, modules, interceptors.toList(), listener, fallbackHandler)",
            RESOLVER_IMPL)
        .build()

    return FileSpec.builder(packageName, className)
        .addType(clazz.addFunction(fallbackHandlerFun)
            .addFunction(listenerFun)
            .addFunction(interceptorFun)
            .addFunction(buildFun)
            .build())
        .build()

  }

  private fun buildModulesInitializer(moduleElements: List<TypeElement>): CodeBlock {
    val codeBlock = CodeBlock.Builder()
    moduleElements.forEachIndexed { i, e ->
      val separator = if (i == moduleElements.size - 1) "" else ","
      codeBlock.add("%T()%L",
          ClassName(e.packageName(context.elements),
              LinkModuleGenerator.nameOfModuleClass(
                  e.className(context.elements))),
          separator)
    }
    return codeBlock.build()
  }

  override fun annotations(): MutableSet<out Class<out Annotation>> {
    return mutableSetOf(LinkResolverBuilder::class.java)
  }

}