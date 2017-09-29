package me.twocities.linker

import android.content.Context
import android.content.Intent
import com.google.common.truth.Truth.assertThat
import me.twocities.linker.LinkResolver.FallbackHandler
import me.twocities.linker.LinkResolver.Interceptor
import me.twocities.linker.LinkResolver.ResolvedListener
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowApplication

@RunWith(value = RobolectricTestRunner::class)
class ResolverImplTest {
  private val context: Context = ShadowApplication.getInstance().applicationContext

  @Test fun illegalLinkShouldFail() {
    val listener = object : ResolvedListener {
      override fun onSuccess(link: String, target: Intent) {
        fail()
      }

      override fun onFailure(link: String, reason: String) {
        assertThat(link).isEqualTo("applink")
        assertThat(reason).isEqualTo("Malformed link")
      }
    }
    val resolver = createResolver(listener = listener)
    val result = resolver.resolve("applink")
    assertThat(result.intent).isNull()
  }

  @Test fun testInterceptor() {
    val interceptor = object : Interceptor {
      override fun intercept(link: String, metadata: LinkMetadata?): Intent? {
        return Intent().apply { action = "interceptor" }
      }
    }
    val resolver = createResolver(interceptors = listOf(interceptor))
    val result = resolver.resolve("applink://foo/bar")
    assertThat(result.intent).isNotNull()
    assertThat(result.intent!!.action).isEqualTo("interceptor")
  }

  @Test fun testFallback() {
    val fallback = object : FallbackHandler {
      override fun handle(link: String): Intent? {
        return Intent().apply { action = "fallback" }
      }
    }
    val resolver = createResolver(fallbackHandler = fallback)
    val result = resolver.resolve("applink://foo/bar")
    assertThat(result.intent).isNotNull()
    assertThat(result.intent!!.action).isEqualTo("fallback")
  }

  @Test fun testLinkModule() {
    val resolver = createResolver(listOf(LinkModule()))
    val result = resolver.resolve("applink://foo/bar?title=title")
    assertThat(result).isNotNull()
    val intent = result.intent!!
    assertThat(intent.getStringExtra("bar")).isEqualTo("bar")
    assertThat(intent.getStringExtra("title")).isEqualTo("title")
  }

  @Test fun noActivitiesMatches() {
    val resolver = createResolver()
    val result = resolver.resolve("applink://foo/bar?title=title")
    assertThat(result.intent).isNull()
  }

  private fun createResolver(modules: List<LinkParser> = listOf(),
      interceptors: List<Interceptor> = listOf(),
      listener: ResolvedListener? = null,
      fallbackHandler: FallbackHandler? = null): ResolverImpl = ResolverImpl(
      context, modules,
      interceptors, listener, fallbackHandler)

  class LinkModule : LinkParser {
    override fun parse(link: String): LinkMetadata? {
      return LinkMetadata("applink://foo/{bar}", String::class.java,
          setOf("bar"), setOf(
          QueryParam("title", true)))
    }

  }
}