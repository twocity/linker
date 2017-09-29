package me.twocities.linker.compiler

import com.google.auto.common.BasicAnnotationProcessor.ProcessingStep
import com.google.common.collect.SetMultimap
import me.twocities.linker.annotations.Link
import me.twocities.linker.annotations.LinkModule
import me.twocities.linker.annotations.LinkPath
import me.twocities.linker.annotations.LinkQuery
import java.io.File
import javax.lang.model.element.Element

class LinkerProcessingStep(private val context: Context) : ProcessingStep {

  companion object {
    val KAPT_KOTLIN_GENERATED_OPTION = "kapt.kotlin.generated"
  }

  override fun process(elements: SetMultimap<Class<out Annotation>, Element>): MutableSet<Element> {
    val kotlinGenerated = context.options[KAPT_KOTLIN_GENERATED_OPTION]
    val linkModuleElements = elements[LinkModule::class.java]
    val linkElements = elements[Link::class.java]
    if (linkModuleElements.isEmpty()) {
      if (linkElements.isNotEmpty()) {
        context.logger.warn(
            "Found elements annotated with @${Link::class.simpleName}, but no @${LinkModule::class.simpleName} founded, did you forget it?")
      }
    } else if (linkModuleElements.size != 1) {
      val modules = linkModuleElements.joinToString("\n") {
        "@${LinkModule::class.simpleName} $it"
      }
      context.logger.error(
          "Found ${linkModuleElements.size} elements annotated with @${LinkModule::class.simpleName}, required one: \n$modules")

    } else {
      val linkModuleElement = linkModuleElements.single().asTypeElement()
      val linkMap = LinkParser(context).parse(linkElements)
      val paramMap = ParamsParser(context,
          linkMap).parse(elements[LinkPath::class.java],
          elements[LinkQuery::class.java])
      val buildFolder = File(kotlinGenerated)
      LinkModuleGenerator(context, linkModuleElement, linkMap,
          paramMap).brewKotlin(buildFolder)
      ActivityInjectorGenerator(context, paramMap).brewKotlin(buildFolder)
    }
    return mutableSetOf()
  }

  override fun annotations(): MutableSet<out Class<out Annotation>> {
    return mutableSetOf(
        LinkModule::class.java, Link::class.java, LinkPath::class.java,
        LinkQuery::class.java)
  }
}