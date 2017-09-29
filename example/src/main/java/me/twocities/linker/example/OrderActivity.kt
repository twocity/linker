package me.twocities.linker.example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import me.twocities.linker.annotations.Link
import me.twocities.linker.annotations.LinkQuery

@Link("link://order/detail")
class OrderActivity : AppCompatActivity() {
  @LinkQuery("id") lateinit var orderId: String

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    supportActionBar?.title = "order"
    bindLinkParams()
  }
}