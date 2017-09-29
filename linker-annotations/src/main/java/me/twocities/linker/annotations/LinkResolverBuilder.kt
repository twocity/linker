package me.twocities.linker.annotations

import kotlin.annotation.AnnotationRetention.BINARY
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.reflect.KClass

/**
 * LinkResolverBuilder was used to generate an LinkResolver's builder class
 *
 * For example, class `@LinkResolverBuilder class MyBuilder()` will generate a class named
 * `_MyBuilder()`, the builder class has a `build()` function which creates an instance of LinkResolver.
 *
 * ```
 * val resolver = _MyBuilder().build()
 * val intent = resolver.resolve("applink://product/detail/123")
 * startActivity(intent)
 * ```
 */
@Target(CLASS)
@Retention(BINARY)
annotation class LinkResolverBuilder(val modules: Array<KClass<*>>)