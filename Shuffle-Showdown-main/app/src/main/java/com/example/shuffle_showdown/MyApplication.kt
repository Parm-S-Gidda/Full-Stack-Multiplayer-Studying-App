package com.example.shuffle_showdown

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// Referencing https://dagger.dev/hilt/gradle-setup
// Makes the app working with Hilt
@HiltAndroidApp
class MyApplication: Application()