package com.example.helloandroid

import android.os.Bundle
import androidx.activity.ComponentActivity

import com.example.helloandroid.customizeview.CustomizeView


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val customizeView = CustomizeView(this)
        setContentView(customizeView) // 直接将它作为根视图

    }
}
