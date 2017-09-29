package me.twocities.linker.compiler

import com.google.common.truth.Truth.assertThat
import me.twocities.linker.compiler.hasAnyOf
import org.junit.Test


class HelperTest {
  @Test fun testHasAnyOf() {
    val list = listOf(1, 2, 3)
    assertThat(list.hasAnyOf(listOf(4, 5, 6))).isFalse()
    assertThat(list.hasAnyOf(listOf(4, 2, 6))).isTrue()
    assertThat(list.hasAnyOf(listOf(1, 2, 3))).isTrue()
    assertThat(list.hasAnyOf(listOf("a", "b", "c"))).isFalse()
  }
}
