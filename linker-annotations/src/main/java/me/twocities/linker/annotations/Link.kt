package me.twocities.linker.annotations

import kotlin.annotation.AnnotationRetention.BINARY
import kotlin.annotation.AnnotationTarget.CLASS

/**
 * AppLink defines a Uri which respect a page(Activity) of application.
 *
 * Any uris matches with [link] will be routed to the corresponding activity.
 * A standard uri would seems like: `link://product/detail/{id}`, and matches with the followings:
 *
 * + `link://product/detail/1234`
 * + `link://product/detail/1234?title=oreo`
 *
 * Parameters in the given uri: `id` and queries(`title`) will be parsed and bundled to Activity's Intent,
 * Original link(`link://product/detail/1234`) will also be passed to activity with key of [LINK]
 *
 * @see [LinkPath]
 * @see [LinkQuery]
 */
@Target(CLASS)
@Retention(BINARY)
@MustBeDocumented
annotation class Link(val link: String)

const val LINK = "me.twocities.applink.extras.LINK"
