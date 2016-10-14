package uk.co.markormesher.easymaps.mapperapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import uk.co.markormesher.easymaps.sdk.sdkTest

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sdkTest(this)
    }
}
