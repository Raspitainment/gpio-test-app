package raspitainment.gpiotest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
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
import raspitainment.gpiotest.ui.theme.GpiotestTheme
import java.util.Optional

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    companion object {
        init {
            System.loadLibrary("gpiotest")
        }

        external fun setupGPIO(): Any
        external fun readGPIO(): Any
        external fun closeGPIO(): Any
    }

    override fun onDestroy() {
        closeGPIO()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GPIOTestContent()
        }
    }

    @Composable
    fun GPIOTestContent() {
        var gpioValue by remember { mutableStateOf(Optional.empty<Boolean>()) }
        var setupErrorValue by remember { mutableStateOf(Optional.empty<String>()) }
        var readErrorValue by remember { mutableStateOf(Optional.empty<String>()) }

        GpiotestTheme {
            LaunchedEffect(Unit) {
                while (true) {
                    val value = readGPIO()
                    if (value is Boolean) {
                        gpioValue = Optional.of(value)
                    } else {
                        readErrorValue = Optional.of(value.toString())
                    }

                    delay(200)
                }
            }

            Scaffold(
                topBar = {
                    TopAppBar(title = { Text(text = "GPIO-Test") })
                },
                bottomBar = {
                    BottomAppBar {
                        Spacer(modifier = Modifier.weight(1f))
                        Text(text = "Setup error: ${setupErrorValue.orElse("None")}\nRead error: ${readErrorValue.orElse("None")}")
                        Spacer(modifier = Modifier.weight(1f))
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = {
                        val result = setupGPIO()
                        setupErrorValue = if (result is String) {
                            Optional.of(result)
                        } else {
                            Optional.empty()
                        }
                    }) {
                        Icon(Icons.Default.Build, contentDescription = "Setup")
                    }
                },
            ) { innerPadding ->
                Column(
                    modifier = Modifier.padding(innerPadding),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.CardDefaults.cardColors(
                            containerColor = gpioValue.map { if (it) Color.Green else Color.Red }.orElse(Color.Gray)
                        )
                    ) {
                        Text(
                            text = "GPIO value: ${gpioValue.map { if (it) "HIGH" else "LOW" }.orElse("Unknown")}",
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}