package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.TaskViewModel
import com.example.ui.screens.MainAppScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private val taskViewModel: TaskViewModel by viewModels {
    val app = application as TodoApplication
    TaskViewModel.provideFactory(app.repository)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val uiState by taskViewModel.uiState.collectAsStateWithLifecycle()
      MyApplicationTheme(themeMode = uiState.themeMode) {
        MainAppScreen(viewModel = taskViewModel)
      }
    }
  }
}

