package raspitainment.gpiotest

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import raspitainment.gpiotest.ui.theme.gpiotestTheme
import java.util.Optional

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    companion object {
        init {
            System.loadLibrary("gpiotest")
        }

        external fun setupGpio(): Optional<String>
        external fun getValue(): Boolean
        external fun closeGpio()
    }

    override fun onDestroy() {
        closeGpio()
        super.onDestroy()
    }

    private var errorValue: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupGpio().ifPresent { error ->
            run {
                println("Error: $error")

                val notificationService =
                    getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                val notification = android.app.Notification.Builder(this, "gpiotest")
                    .setContentTitle("GPIO-Test").setContentText("Error: $error")
                    .setSmallIcon(android.R.drawable.stat_notify_error).build()

                Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
                errorValue = error
                notificationService.notify(1, notification)
            }
        }

        setContent {
            GpiotestContent()
        }
    }

    @Composable
    fun GpiotestContent() {
        var gpioValue by remember { mutableStateOf(false) }

        gpiotestTheme {
            LaunchedEffect(Unit) {
                while (true) {
                    gpioValue = getValue()
                    delay(200)
                }
            }

            Scaffold(
                topBar = {
                    TopAppBar(title = { Text(text = "GPIO-Test") })
                },
                bottomBar = {
                    if (errorValue.isNotEmpty()) {
                        BottomAppBar {
                            Spacer(modifier = Modifier.weight(1f))
                            Text(text = "Error: $errorValue", textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.weight(1f))
                        }
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
                val cColor = if (gpioValue) {
                    Color.Red
                } else {
                    Color.Green
                }
                Column(
                    modifier = Modifier.padding(innerPadding),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.CardDefaults.cardColors(
                            containerColor = cColor
                        )
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