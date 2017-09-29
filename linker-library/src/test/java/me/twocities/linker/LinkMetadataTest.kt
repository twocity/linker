package me.twocities.linker

import com.google.common.truth.Truth.assertThat
import org.junit.Test


class LinkMetadataTest {

  @Test fun illegalLinkNotMatch() {
    assertThat(metadataOf().matches("app")).isFalse()
  }

  @Test fun pathsNotMatch() {
    assertThat(metadataOf().matches("link://foo")).isFalse()
  }

  @Test fun linkWithQueriesWillMatch() {
    assertThat(metadataOf().matches("link://foo/123?456=445")).isTrue()
  }

  @Test fun singleParam() {
    val metadata = metadataOf()
    assertThat(metadata.parseParams("link://foo/bar")).containsEntry("bar", "bar")
  }

  @Test fun linkParamsWithQueries() {
    val metadata = metadataOf("link://foo/{bar}", pathParams = setOf("bar"), queryParams = setOf(
        QueryParam("x", true), QueryParam("z", true)
    ))
    val params = metadata.parseParams("link://foo/bar?x=y&z=w")
    assertThat(params.size).isEqualTo(3)
    assertThat(params).containsEntry("bar", "bar")
    assertThat(params).containsEntry("x", "y")
    assertThat(params).containsEntry("z", "w")
  }

  @Test fun linkWithNoParams() {
    val metadata = metadataOf("link://foo/bar", pathParams = setOf(), queryParams = setOf())
    assertThat(metadata.parseParams("link://foo/bar")).isEmpty()
  }

  @Test(expected = IllegalLinkException::class) fun missingParamsWillThrowException() {
    val metadata = metadataOf("link://foo/bar", pathParams = setOf(), queryParams = setOf(
        QueryParam("key", true)))
    metadata.parseParams("link://foo/bar")
  }

  @Test fun missingParamsWontThrowException() {
    val metadata = metadataOf("link://foo/bar", pathParams = setOf(), queryParams = setOf(
        QueryParam("key", false)))
    assertThat(metadata.parseParams("link://foo/bar")).isEmpty()
  }

  @Test fun matches() {
    val metadata = metadataOf()
    assertThat(metadata.matches("link://foo/1234?title=4567")).isTrue()
  }

  @Test fun differentSchemeWontMatch() {
    val metadata = metadataOf()
    assertThat(metadata.matches("thelink://foo/1234")).isFalse()
  }

  @Test fun spaceInLinkPath() {
    val metadata = metadataOf()
    assertThat(metadata.parseParams("link://foo/foo%20bar")).containsEntry("bar", "foo%20bar")
  }

  @Test fun diffPathWontMatch() {
    val metadata = metadataOf()
    assertThat(metadata.matches("link://foo/1234/4567")).isFalse()
  }

  private fun metadataOf(link: String = "link://foo/{bar}",
      target: Class<*> = String::class.java,
      pathParams: Set<String> = setOf("bar"),
      queryParams: Set<QueryParam> = setOf()): LinkMetadata
      = LinkMetadata(link, target, pathParams, queryParams)
}