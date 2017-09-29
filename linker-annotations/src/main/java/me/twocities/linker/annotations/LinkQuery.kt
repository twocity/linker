package me.twocities.linker.annotations

import kotlin.annotation.AnnotationRetention.BINARY
import kotlin.annotation.AnnotationTarget.FIELD

/**
 * LinkQuery respects queries of an uri.
 *
 * When compiling, any fields annotated with [LinkQuery] will be recorded, and then a `inject` function
 * will be generated, the caller can use it inject field values.
 * Given the url: `link://product/detail/123?title=oreo`, you can define a filed of Activity by [LinkQuery]
 *
 * ```
 * @Link(link://product/detail/{id}) class ProductActivity {
 *   @LinkQuery("title") lateinit var productTitle: String
 *
 *   override fun onCreate(savedInstanceState: Bundle?) {
 *     injectLinkParams()
 *   }
 * }
 * ```
 *
 * Type of annotated filed must be String, and filed's visibility should be public,
 * otherwise, an error will be raised when compiling.
 */
@Target(FIELD)
@Retention(BINARY)
@MustBeDocumented
annotation class LinkQuery(val name: String)