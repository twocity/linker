package me.twocities.linker.example;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import me.twocities.linker.annotations.Link;
import me.twocities.linker.annotations.LinkQuery;

@Link(link = "java://java/activity") public class JavaActivity extends AppCompatActivity {
  @LinkQuery(name = "nonnull") String nonNullValue;
  @LinkQuery(name = "nullable") @Nullable String nullableString;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    JavaActivityLinkBinderKt.bindLinkParams(this);
  }
}
