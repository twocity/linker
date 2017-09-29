package me.twocities.linker.annotations

import kotlin.annotation.AnnotationRetention.BINARY
import kotlin.annotation.AnnotationTarget.CLASS

/**
 * LinkModule was used to generate an implementation of LinkParser.
 *
 * For example, class `@LinkModule class DemoModule()`, the compiler will generate a class named
 * `_DemoModule` witch implements LinkHandler. The generated class was used innerly.
 *
 * see LinkParser's documentation
 */
@Target(CLASS)
@Retention(BINARY)
@MustBeDocumented
annotation class LinkModule