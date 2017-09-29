package me.twocities.linker

import android.content.Intent
import me.twocities.linker.LinkResolver.FallbackHandler

/**
 * Resolver of app link.
 *
 * Resolver
 * The resolver is the core part of app link's routing.
 * If an activity was annotated with AppLink, when a matched link comes, the resolver can parse the given
 * link as [Result] which has a property of [Intent], the caller can use it to start corresponding activity directly.
 *
 * Interceptors
 * The interceptor will give you an ability to change a link's intent, or put extra values to activity
 *
 * FallbackHandler
 * If there's no activity matches with the given link, or no interceptors has intercepted,
 * a fallback intent will be returned. The [FallbackHandler] gives you the ability to handle
 * unknown link: start another activity or show an error page.
 *
 * Order of link resolving:
 *
 * ```
 *   +------+          +---------------+         +---------------------+
 *   | link |  ----->  | interceptors |  -----> /    @Link Activities  /
 *   +------+          +--------------+         +----------------------+
 *                                                        |
 *                                                        |
 *                                                        V
 *                                             +-------------------+
 *                                             | fallback handler |
 *                                             +-------------------+
 * ```
 */
interface LinkResolver {
  fun resolve(link: String): Result

  /**
   * Change the original behavior of link
   */
  interface Interceptor {
    /**
     * Intercept
     * @param link the uri of a page
     * @param metadata of matched link, or null if no activity matched
     * @return intent of matched activity or null
     */
    fun intercept(link: String, metadata: LinkMetadata?): Intent?
  }

  /**
   * Handler of the unsupported link
   * If there's no activity matches with the given link, or no interceptors has intercepted, this handler
   * will be called
   */
  interface FallbackHandler {
    /**
     * Handle the unresolved link
     */
    fun handle(link: String): Intent?
  }

  /**
   * A callback when a link was resolved
   */
  interface ResolvedListener {
    fun onSuccess(link: String, target: Intent)
    fun onFailure(link: String, reason: String)
  }

}

/**
 * The result of app link resolving.
 */
class Result(val intent: Intent?) {
  val success
    get() = intent != null
}

internal class IllegalLinkException(msg: String) : Exception(msg)
