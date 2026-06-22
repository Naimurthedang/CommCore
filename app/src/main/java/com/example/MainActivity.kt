package com.example

import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.CommScreen
import com.example.ui.CommViewModel
import com.example.ui.screens.MainAppUi
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private lateinit var comViewModel: CommViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        comViewModel = viewModel()
        MainAppUi(viewModel = comViewModel)
      }
    }
  }

  override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
    if (event != null && event.isCtrlPressed) {
      if (keyCode == KeyEvent.KEYCODE_K) {
        if (::comViewModel.isInitialized) {
          val newScreen = if (comViewModel.currentScreen == CommScreen.CHAT) {
            CommScreen.DASHBOARD
          } else {
            CommScreen.CHAT
          }
          comViewModel.currentScreen = newScreen
          Toast.makeText(this, "Shortcut Activated: Switched to $newScreen", Toast.LENGTH_SHORT).show()
        }
        return true
      } else if (keyCode == KeyEvent.KEYCODE_T) {
        if (::comViewModel.isInitialized) {
          comViewModel.showTipDialog = !comViewModel.showTipDialog
          Toast.makeText(this, "Shortcut Activated: Tip of the Day Dialog Toggled", Toast.LENGTH_SHORT).show()
        }
        return true
      }
    }
    return super.onKeyDown(keyCode, event)
  }
}
