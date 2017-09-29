package me.twocities.linker.annotations

import kotlin.annotation.AnnotationRetention.BINARY
import kotlin.annotation.AnnotationTarget.FIELD

/**
 * LinkPath respects paths of an uri.
 *
 * When compiling, any fields annotated with [LinkPath] will be recorded, and then a `inject` function
 * will be generated, the caller can use it inject field values.
 * Given the url: `link://product/detail/{id}`, you can define a property of Activity by [LinkPath]
 *
 * ```
 * @Link(link://product/detail/{id}) class ProductActivity {
 *   @LinkPath("id") lateinit var productId: String
 *
 *   override fun onCreate(savedInstanceState: Bundle?) {
 *     injectLinkParams()
 *   }
 * }
 * ```
 *
 * LinkPath's value must be one of uri's paths, and the type of annotated property must be String,
 * and filed's visibility should be public, otherwise, an error will be raised when compiling.
 */
@Target(FIELD)
@Retention(BINARY)
@MustBeDocumented
annotation class LinkPath(val path: String)
