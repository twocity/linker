package me.twocities.linker.example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import me.twocities.linker.annotations.LINK

class FallbackActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    supportActionBar?.title = "fallback"
    Toast.makeText(this, intent.getStringExtra(LINK), Toast.LENGTH_LONG).show()
  }
}