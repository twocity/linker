package me.twocities.linker.compiler

import com.google.auto.common.BasicAnnotationProcessor
import javax.lang.model.SourceVersion

/**
 * The main processor
 */
@Suppress("unused")
class LinkerProcessor : BasicAnnotationProcessor() {

  override fun initSteps(): MutableIterable<ProcessingStep> {
    val context = Context(processingEnv)
    return arrayListOf(LinkerProcessingStep(context),
        LinkBuilderProcessingStep(context))
  }

  override fun getSupportedSourceVersion(): SourceVersion {
    return SourceVersion.latestSupported()
  }
}