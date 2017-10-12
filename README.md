# Linker [![Build Status](https://travis-ci.org/twocity/linker.svg?branch=master)](https://travis-ci.org/twocity/linker)

Linker provides an annotation-based API to handle URI routing for Android. This library is written in kotlin, and the generated codes are also pure kotlin.

## Dependencies

Add the following to your `build.gradle` file:

```groovy
dependencies {
	api 'me.twocities:linker:0.0.5'
	kapt 'me.twocities:linker-compiler:0.0.5'
}
```

## Usage

There are two parts of Linker: annotations and `LinkResolver`

### Annotations

__@Link for activity__

Use annotation `@Link` indicate which URI was respect:

```kotlin
@Link("link://product/detail{id})
class ProductActivity: AppCompatActivity {
}
```

__`@LinkPath` `@LinkQuery` for parameters__

```kotlin
@Link("link://product/detail/{id})
class ProductActivity: AppCompatActivity {
  @LinkPath("id") lateinit var productId: String
  @LinkQuery("title") lateinit var productTitle: String
}
```

After annotation processing, an extension function `bindLinkParams()` of `ProductActivity` will be generated, you can use it to get values of `@LinkPath` `@LinkQuery` params:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  bindLinkParams()
}
```

__`@LinkModule` for module__

```kotlin
// generate LinkerExampleLinkModule
@LinkModule
class ExampleLinkModule
```

After annotation processing, a module class was generated, will contains informations of all defined `@Link`, `@LinkPath`, `@LinkQuery`. The generated class will be used to decided which uri will be routed.

`@LinkModule` supports multi android's library module.

__`@LinkResolverBuilder` for builder__

```kotlin
// generate LinkerExampleLinkResolverBuilder
@LinkResolverBuilder(modules = arrayOf(ExampleLinkModule::class, LibraryLinkModule::class))
interface ExampleLinkResolverBuilder
```

This will generate a LinkResolver's builder:

```
val resolver = LinkerExampleLinkResolverBuilder(context).build()
```


### LinkResolver

The definition of LinkResolver is much simpler:

```kotlin
interface LinkResolver {
  fun resolve(link: String): Result
}
```
Function `resolve` will parse the given link, and then return a `Result`, which has a nullable property of `Intent`, it will be null if no responding activities was found,

Routing:

```kotlin
val result = resolver.resolve("link//product/detail/123")
if (result.success()) startActivity(result.intent)
```
Thanks to kotlin, we can simplify this by writing extension function like this:

```kotlin
startActivity("link://product/detail/123")
```
see [ObjectGraph](https://github.com/twocity/linker/blob/master/example/src/main/java/me/twocities/linker/example/ObjectGraph.kt) for more details.

## Advance

Linker also provide other mechanisms: `Interceptor`, `FallbackHandler`, which you can change the behavior of a link. You can add interceptors or set fallback handler by the generated builder class:

```kotlin
 val resolver = LinkerBuilder(context)
        .addInterceptor(HttpUrlInterceptor(context))
        .setFallbackHandler(DefaultUrlHandler(context))
        .setListener(ResolverListener())
        .build()
```

the Listener will be notified when an link was resolved.

### Interceptors

The interceptor will give you an ability to change a link's intent, or put extra values to activity

```kotlin
// An interceptor handles how http(s) url was resolved
class HttpUrlInterceptor(private val context: Context) : Interceptor {
  override fun intercept(link: String, metadata: LinkMetadata?): Intent? {
    if (link.startsWith("http") or link.startsWith("https")) {
      // since `LINK` will be passed to intent automatically, we can return the intent directly
      return Intent(context, SimpleBrowserActivity::class.java)
    }
    return null
  }
}
```

### FallbackHandler

If there's no activities match with the given link, or no interceptors has intercepted, the fallback handler be called. The [FallbackHandler] gives you the ability to handle unknown link: start another activity or show an error page.

## Known issues

+ Generated kotlin files was not recognized by AndroidStudio automatically. see [KT-20269](https://youtrack.jetbrains.com/issue/KT-20269)

## TODO

+ [ ] Generate link's builder when compiling
+ [ ] Support multi links for per activity
+ [ ] More unit tests

## Credit

+ [DeepLinkDispatch](https://github.com/airbnb/DeepLinkDispatch)
+ [ButterKnife](https://github.com/JakeWharton/butterknife)

## License

Apache License, Version 2.0
