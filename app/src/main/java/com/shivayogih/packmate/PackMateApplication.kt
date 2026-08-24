package com.shivayogih.packmate

import android.app.Application
import com.shivayogih.packmate.data.TripRepository

class PackMateApplication : Application() {
    val tripRepository: TripRepository by lazy {
        TripRepository(applicationContext)
    }
}
