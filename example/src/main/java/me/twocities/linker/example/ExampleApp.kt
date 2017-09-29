package me.twocities.linker.example

import android.app.Application


class ExampleApp : Application() {
  private val objectGraph = lazy {
    ObjectGraph(this)
  }

  override fun onCreate() {
    super.onCreate()
  }

  override fun getSystemService(name: String?): Any {
    if (ObjectGraph.matches(name)) {
      return objectGraph.value
    }
    return super.getSystemService(name)
  }

}
