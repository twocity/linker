package me.twocities.linker.compiler

import me.twocities.linker.annotations.Link

/**
 * Model classes stand for [Link] annotations's information
 */
data class LinkParams(val rawLink: String, val paths: Set<PathAnnotation>,
    val queries: Set<QueryAnnotation>)

data class PathAnnotation(val key: String, val fieldName: String)

data class QueryAnnotation(val name: String, val fieldName: String, val required: Boolean)
