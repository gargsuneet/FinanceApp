package com.financeapp.presentation.pin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.financeapp.FinanceApplication
import com.financeapp.presentation.settings.SettingsViewModel

@Composable
fun PinScreen(
    onPinVerified: () -> Unit,
    viewModel: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = SettingsViewModel.Factory(FinanceApplication.instance)
    )
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF00897B)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Enter PIN", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(4) { i ->
                Box(
                    modifier = Modifier.size(16.dp).clip(CircleShape)
                        .background(if (i < pin.length) Color.White else Color.White.copy(alpha = 0.3f))
                )
            }
        }
        if (error) {
            Spacer(Modifier.height(8.dp))
            Text("Incorrect PIN", color = Color(0xFFFFCDD2), fontSize = 14.sp)
        }
        Spacer(Modifier.height(40.dp))
        val keys = listOf("1","2","3","4","5","6","7","8","9","","0","⌫")
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            keys.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    row.forEach { key ->
                        if (key.isEmpty()) {
                            Spacer(Modifier.size(72.dp))
                        } else {
                            Box(
                                modifier = Modifier.size(72.dp).clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .clickable {
                                        if (key == "⌫") {
                                            if (pin.isNotEmpty()) pin = pin.dropLast(1)
                                        } else if (pin.length < 4) {
                                            pin += key
                                            if (pin.length == 4) {
                                                if (viewModel.verifyPin(pin)) {
                                                    onPinVerified()
                                                } else {
                                                    error = true
                                                    pin = ""
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (key == "⌫") {
                                    Icon(Icons.Default.Backspace, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Text(key, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
