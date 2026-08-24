package com.shivayogih.packmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shivayogih.packmate.ui.PackMateApp
import com.shivayogih.packmate.ui.PackMateViewModel
import com.shivayogih.packmate.ui.theme.PackMateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val application = application as PackMateApplication
        setContent {
            val viewModel: PackMateViewModel = viewModel(
                factory = PackMateViewModel.Factory(application.tripRepository),
            )
            PackMateTheme {
                PackMateApp(viewModel = viewModel)
            }
        }
    }
}
