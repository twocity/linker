package me.twocities.linker.compiler

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.INTERNAL
import java.io.File
import java.io.IOException
import javax.lang.model.element.TypeElement
import me.twocities.linker.annotations.Link
import me.twocities.linker.annotations.LinkQuery
import me.twocities.linker.annotations.LinkPath

/**
 * Generate an extension function which can inject link's param values into activity.
 *
 * Each Activity class which annotated with [Link] and [LinkQuery] or [LinkPath]
 * will generated an extension function named `injectLinkParams`
 * The caller can use it directly in activity:
 *
 * ```
 * @Link(link://product/detail/{id}) class ProductActivity {
 *   @LinkPath("id") lateinit var pageId: String
 *   @LinkQuery("title") lateinit var pageTitle: String
 *
 *  override fun onCreate(savedInstanceState: Bundle?) {
 *    super.onCreate(savedInstanceState)
 *     injectLinkParams()
 *     Log.d(TAG, "page id:${pageId}, page title: ${pageTitle}")
 *   }
 * }
 * ```
 * @see [Link]
 * @see [LinkPath]
 * @see [LinkQuery]
 */
class ActivityInjectorGenerator(private val context: Context,
    private val paramsMap: Map<TypeElement, LinkParams>) {

  @Throws(IOException::class)
  fun brewKotlin(directory: File) {
    paramsMap.forEach {
      if (it.value.hasParams()) {
        generateFun(it.key, it.value).writeTo(directory)
      }
    }
  }

  private fun generateFun(e: TypeElement, linkParams: LinkParams): FileSpec {
    val packageName = e.packageName(context.elements)
    val className = e.className(context.elements)

    val func = FunSpec.builder("bindLinkParams")
        .addModifiers(INTERNAL)
        .receiver(ClassName(packageName, className))
        .addStatement("val intent = this.intent")

    linkParams.paths.forEach {
      func.addCode(generateBindingCode((Triple(it.key, it.fieldName, true))))
    }
    linkParams.queries.forEach {
      func.addCode(generateBindingCode(Triple(it.name, it.fieldName, it.required)))
    }

    return FileSpec.builder(packageName, className + "LinkBinder")
        .addFunction(func.build())
        .build()
  }

  private fun generateBindingCode(params: Triple<String, String, Boolean>): CodeBlock {
    val builder = CodeBlock.Builder()
    val isOptional = !params.third
    if (isOptional) {
      builder.beginControlFlow("if(intent.hasExtra(%S))", params.first)
    }
    builder.addStatement("this.%L = intent.getStringExtra(%S)", params.second, params.first)
    if (isOptional) {
      builder.endControlFlow()
    }
    return builder.build()
  }

  /**
   * Generate binder function only when activity has params to bind
   */
  private fun LinkParams.hasParams(): Boolean {
    return this.paths.isNotEmpty() || this.queries.isNotEmpty()
  }
}