package me.twocities.linker.example

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import me.twocities.linker.LinkMetadata
import me.twocities.linker.LinkResolver
import me.twocities.linker.LinkResolver.FallbackHandler
import me.twocities.linker.LinkResolver.Interceptor
import me.twocities.linker.Result


/**
 * Poor man's dependency management
 */
class ObjectGraph(private val app: Application) {
  val linkResolver by lazy {
    LinkerExampleLinkResolverBuilder(app)
        .addInterceptor(HttpUrlInterceptor(app))
        .setFallbackHandler(DefaultUrlHandler(app))
        .setListener(ResolverListener())
        .build()
  }

  companion object {
    private val SERVICE_NAME = "OBJECT_GRAPH"

    fun matches(service: String?): Boolean {
      if (service == null) {
        return false
      }
      return SERVICE_NAME.contentEquals(service)
    }

    fun get(context: Context): ObjectGraph {
      val appContext = context.applicationContext
      return appContext.getSystemService(
          SERVICE_NAME) as ObjectGraph
    }
  }
}

class HttpUrlInterceptor(private val context: Context) : Interceptor {
  override fun intercept(link: String, metadata: LinkMetadata?): Intent? {
    if (link.startsWith("http") or link.startsWith("https")) {
      // since `LINK` will be passed to intent automatically, we can return the intent directly
      return Intent(context, SimpleBrowserActivity::class.java)
    }
    return null
  }
}

class DefaultUrlHandler(private val context: Context) : FallbackHandler {
  override fun handle(link: String): Intent? {
    return Intent(context, FallbackActivity::class.java)
  }

}

class ResolverListener : LinkResolver.ResolvedListener {
  companion object {
    private val TAG = "LINKER"
  }

  override fun onSuccess(link: String, target: Intent) {
    Log.d(TAG, "Resolve $link into $target")
  }

  override fun onFailure(link: String, reason: String) {
    Log.e(TAG, "Can't resolve link: $link: $reason")
  }
}

// extension function of start Result
fun Context.startActivity(result: Result) {
  if (result.success) this.startActivity(result.intent)
}

fun Context.startActivity(link: String) = this.startActivity(this.linkResolver.resolve(link))

val Context.linkResolver: LinkResolver
  get() = ObjectGraph.get(this).linkResolver
