package me.twocities.linker.compiler

import com.squareup.kotlinpoet.ClassName

val LINK_PARSER = ClassName("me.twocities.linker", "LinkParser")
val FALLBACK_HANDLER = ClassName("me.twocities.linker", "LinkResolver", "FallbackHandler")
val RESOLVER_LISTENER = ClassName("me.twocities.linker", "LinkResolver",
    "ResolvedListener")
val INTERCEPTOR = ClassName("me.twocities.linker", "LinkResolver", "Interceptor")
val RESOLVER_IMPL = ClassName("me.twocities.linker", "ResolverImpl")
val LINK_RESOLVER = ClassName("me.twocities.linker", "LinkResolver")
val LINK_METADATA = ClassName("me.twocities.linker", "LinkMetadata")
val QueryParam = ClassName("me.twocities.linker", "QueryParam")