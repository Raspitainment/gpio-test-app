package com.example.raspitainment

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import com.example.raspitainment.ui.theme.RaspitainmentTheme
import kotlinx.coroutines.delay
import java.io.Console
import java.util.Optional

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    companion object {
        init {
            System.loadLibrary("raspitainment")
        }

        external fun setupGpio(): Optional<String>
        external fun getValue(): Boolean
        external fun closeGpio()
    }

    override fun onDestroy() {
        closeGpio()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupGpio().ifPresent { error ->
            run {
                val notificationService =
                    getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                val notification = android.app.Notification.Builder(this, "raspitainment")
                    .setContentTitle("Raspitainment").setContentText("Error: $error")
                    .setSmallIcon(android.R.drawable.stat_notify_error).build()

                notificationService.notify(1, notification)

                finish()
            }
        }

        setContent {
            RaspitainmentContent()
        }
    }

    @Composable
    fun RaspitainmentContent() {
        var gpioValue by remember { mutableStateOf(false) }

        RaspitainmentTheme {
            LaunchedEffect(Unit) {
                while (true) {
                    gpioValue = getValue()
                    delay(200)
                }
            }

            Scaffold(
                topBar = {
                    TopAppBar(title = { Text(text = "Raspitainment") })
                },
                bottomBar = {
                    BottomAppBar {
                        Text(text = "Bottom bar")
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = {
                        Toast.makeText(this, "Hello world!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Info, contentDescription = "Info")
                    }
                },
            ) { innerPadding ->
                Column(
                    modifier = Modifier.padding(innerPadding),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.size(200.dp),
                    ) {
                        Text(
                            text = "GPIO value: ${if (gpioValue) "HIGH" else "LOW"}",
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}