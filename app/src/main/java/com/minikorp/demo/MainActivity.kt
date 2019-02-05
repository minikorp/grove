package com.minikorp.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.minikorp.grove.ConsoleLogTree
import com.minikorp.grove.Grove

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Grove.plant(ConsoleLogTree())

        val exception = IllegalStateException()

        Grove.v { "Test Log" }
        Grove.d { "Test Log" }
        Grove.i { "Test Log" }
        Grove.w { "Test Log" }
        Grove.e { "Test Log" }
        Grove.wtf { "Test Log" }

        Grove.v(exception) { "Test Log" }
        Grove.d(exception) { "Test Log" }
        Grove.i(exception) { "Test Log" }
        Grove.w(exception) { "Test Log" }
        Grove.e(exception) { "Test Log" }
        Grove.wtf(exception) { "Test Log" }
    }
}
