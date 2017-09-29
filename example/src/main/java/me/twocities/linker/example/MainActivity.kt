package me.twocities.linker.example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.fallback
import kotlinx.android.synthetic.main.activity_main.interceptorButton
import kotlinx.android.synthetic.main.activity_main.invalidButton
import kotlinx.android.synthetic.main.activity_main.libraryButton
import kotlinx.android.synthetic.main.activity_main.orderButton
import kotlinx.android.synthetic.main.activity_main.productButton

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    productButton.setOnClickListener { startActivity("link://product/detail/123/456?title=oreo") }

    orderButton.setOnClickListener { startActivity("link://order/detail?id=456") }

    invalidButton.setOnClickListener { startActivity("link") }

    interceptorButton.setOnClickListener { startActivity("https://d.android.com") }

    fallback.setOnClickListener { startActivity("link://product") }

    libraryButton.setOnClickListener { startActivity("library://product/194?title=abc") }
  }
}
