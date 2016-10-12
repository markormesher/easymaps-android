package uk.co.markormesher.prjandroid.scannerapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import uk.co.markormesher.prjandroid.sdk.sdkTest

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sdkTest(this)
    }
}
