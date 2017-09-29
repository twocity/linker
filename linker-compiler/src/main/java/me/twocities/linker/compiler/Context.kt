package me.twocities.linker.compiler

import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic.Kind.ERROR
import javax.tools.Diagnostic.Kind.NOTE
import javax.tools.Diagnostic.Kind.WARNING

/**
 * Like Android's Context component
 */
class Context(processingEnvironment: ProcessingEnvironment) {
  val options = processingEnvironment.options
  val types = processingEnvironment.typeUtils
  val elements = processingEnvironment.elementUtils
  val logger = Logger(processingEnvironment.messager)
}

class Logger(private val messenger: Messager) {

  fun note(msg: String) {
    messenger.printMessage(NOTE, msg)
  }

  fun error(msg: String, e: Element? = null) {
    messenger.printMessage(ERROR, msg, e)
  }

  fun warn(msg: String, e: Element? = null) {
    messenger.printMessage(WARNING, msg, e)
  }

  fun debug(msg: String, e: Element? = null) {
    if (e == null) {
      messenger.printMessage(WARNING, msg)
    } else {
      messenger.printMessage(WARNING, msg, e)
    }
  }
}