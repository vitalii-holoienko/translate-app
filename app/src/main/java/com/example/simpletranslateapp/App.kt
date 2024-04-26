package com.example.simpletranslateapp

import android.app.Application

class App : Application() {
    val database by lazy { SavedStringDataBase.createDataBase(this)}
}