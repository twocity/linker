package me.twocities.linker.example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import me.twocities.linker.annotations.Link
import me.twocities.linker.annotations.LinkPath
import me.twocities.linker.annotations.LinkQuery
import me.twocities.linker.getLink

@Link("link://product/detail/{id}/{sub_id}")
class ProductActivity : AppCompatActivity() {

  @LinkPath("id") lateinit var productId: String
  @JvmField @LinkQuery("title") var productTitle: String? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    supportActionBar?.title = "product"
    bindLinkParams()
    Toast.makeText(this, "id: $productId, title: $title", Toast.LENGTH_LONG).show()
    Log.d("LINKER", "Link: ${intent.getLink()}")
  }
}
