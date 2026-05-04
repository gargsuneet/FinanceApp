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
fun PinSetupScreen(
    onPinSet: () -> Unit,
    onCancel: () -> Unit,
    viewModel: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = SettingsViewModel.Factory(FinanceApplication.instance)
    )
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isConfirming by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    val currentPin = if (isConfirming) confirmPin else pin

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF00897B)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            if (isConfirming) "Confirm PIN" else "Set New PIN",
            color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(4) { i ->
                Box(
                    modifier = Modifier.size(16.dp).clip(CircleShape)
                        .background(if (i < currentPin.length) Color.White else Color.White.copy(alpha = 0.3f))
                )
            }
        }
        if (error.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(error, color = Color(0xFFFFCDD2), fontSize = 14.sp)
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
                                        error = ""
                                        if (key == "⌫") {
                                            if (!isConfirming && pin.isNotEmpty()) pin = pin.dropLast(1)
                                            else if (isConfirming && confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1)
                                        } else {
                                            if (!isConfirming && pin.length < 4) {
                                                pin += key
                                                if (pin.length == 4) isConfirming = true
                                            } else if (isConfirming && confirmPin.length < 4) {
                                                confirmPin += key
                                                if (confirmPin.length == 4) {
                                                    if (pin == confirmPin) {
                                                        viewModel.setupPin(pin)
                                                        onPinSet()
                                                    } else {
                                                        error = "PINs don't match, try again"
                                                        pin = ""
                                                        confirmPin = ""
                                                        isConfirming = false
                                                    }
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
        Spacer(Modifier.height(24.dp))
        TextButton(onClick = onCancel) {
            Text("Cancel", color = Color.White.copy(alpha = 0.7f))
        }
    }
}
