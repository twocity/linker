package me.twocities.linker.example.library

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import me.twocities.linker.annotations.Link
import me.twocities.linker.annotations.LinkQuery

@Link("library://product/{id}")
class LibraryActivity : AppCompatActivity() {

  @LinkQuery("title") lateinit var title: String

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    bindLinkParams()
    Toast.makeText(this, title, Toast.LENGTH_SHORT).show()
  }
}
