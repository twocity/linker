package me.twocities.linker.example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_simple_browser.webView
import me.twocities.linker.example.R.layout
import me.twocities.linker.annotations.LINK

class SimpleBrowserActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(layout.activity_simple_browser)

    if (intent.hasExtra(LINK)) {
      webView.loadUrl(intent.getStringExtra(LINK))
    }

  }
}