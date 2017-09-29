package me.twocities.linker.example

import me.twocities.linker.annotations.LinkModule
import me.twocities.linker.annotations.LinkResolverBuilder
import me.twocities.linker.example.library.LibraryLinkModule

@LinkResolverBuilder(modules = arrayOf(ExampleLinkModule::class, LibraryLinkModule::class))
interface ExampleLinkResolverBuilder

@LinkModule
class ExampleLinkModule
