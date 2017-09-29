package me.twocities.linker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import me.twocities.linker.LinkResolver.FallbackHandler
import me.twocities.linker.LinkResolver.Interceptor
import me.twocities.linker.LinkResolver.ResolvedListener
import me.twocities.linker.annotations.LINK
import me.twocities.linker.annotations.LinkResolverBuilder

/**
 * The implementation of [LinkResolver]
 *
 * Generated builder class by [LinkResolverBuilder] will create an instance of this class.
 */
class ResolverImpl(private val context: Context,
    private val modules: List<LinkParser>,
    private val userInterceptors: List<Interceptor>,
    private val listener: ResolvedListener? = null,
    private val fallbackHandler: FallbackHandler? = null) : LinkResolver {

  override fun resolve(link: String): Result {
    if (DeepLinkUri.parse(link) == null) {
      listener?.onFailure(link, "Malformed link")
      return Result(null)
    }
    val metadata = parseMetadata(link)
    // interceptor processing
    var intent = processInterceptors(link, metadata)

    if (intent == null && metadata != null) {
      try {
        intent = Intent(context, metadata.target)
        intent.putExtras(metadata.parseParams(link).asBundle())
      } catch (e: IllegalLinkException) {
        listener?.onFailure(link, e.message!!)
        return Result(null)
      }
    }

    // fallback handling
    if (intent == null) {
      intent = fallbackHandler?.handle(link)
    }

    if (intent != null) {
      intent.putExtra(LINK, link)
      listener?.onSuccess(link, intent)
    }
    return Result(intent)
  }

  private fun processInterceptors(link: String, metadata: LinkMetadata?): Intent? {
    userInterceptors.forEach {
      val intent = it.intercept(link, metadata)
      if (intent != null) {
        return intent
      }
    }
    return null
  }

  private fun parseMetadata(link: String): LinkMetadata? {
    modules.forEach {
      val metadata = it.parse(link)
      if (metadata != null) {
        return metadata
      }
    }
    return null
  }
}

private fun Map<String, String>.asBundle(): Bundle {
  val bundle = Bundle()
  this.forEach {
    bundle.putString(it.key, it.value)
  }
  return bundle
}

