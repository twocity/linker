package me.twocities.linker

import android.content.Intent
import me.twocities.linker.annotations.LINK

internal fun Intent.putLink(link: String) = putExtra(LINK, link)

fun Intent.getLink(): String? = getStringExtra(LINK)
