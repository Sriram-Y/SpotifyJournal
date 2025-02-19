package com.app.spotifyjournal.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.tooling.preview.devices.WearDevices
import com.app.spotifyjournal.R
import com.app.spotifyjournal.presentation.theme.SpotifyJournalTheme
import java.io.InputStream

class MainActivity : ComponentActivity() {

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startBackgroundService()
            } else {
                // Handle permission denial
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            WearApp("Android")
        }

        if (isPermissionGranted()) {
            startBackgroundService()
        } else {
            requestBodySensorsPermission()
        }

        val projectId = "spotify-journal"
        val zone = "us-west2-b"
        val smallMachineType = "e2-micro"
        val largeMachineType = "e2-standard-2"
        val sourceImage = "projects/debian-cloud/global/images/family/debian-11"
        var fdcKeyFileInputStream: InputStream =
            resources.openRawResource(R.raw.service_account_key)
        var sdcKeyFileInputStream: InputStream =
            resources.openRawResource(R.raw.service_account_key)
        var peKeyFileInputStream: InputStream =
            resources.openRawResource(R.raw.service_account_key)

        val createFdc = CreateInstance()
        createFdc.createInstanceWithKey(
            projectId,
            zone,
            "fdc-vm",
            smallMachineType,
            sourceImage,
            fdcKeyFileInputStream
        )   // fitness data collection vm

        val createSdc = CreateInstance()
        createSdc.createInstanceWithKey(
            projectId,
            zone,
            "sdc-vm",
            smallMachineType,
            sourceImage,
            sdcKeyFileInputStream
        )   // spotify data collection vm

        val createPe = CreateInstance()
        createPe.createInstanceWithKey(
            projectId,
            zone,
            "pe-vm",
            largeMachineType,    // different machine type for prediction engine vm
            sourceImage,
            peKeyFileInputStream
        )   // prediction engine vm
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.BODY_SENSORS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestBodySensorsPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.BODY_SENSORS
            )
        ) {
        }
        permissionLauncher.launch(Manifest.permission.BODY_SENSORS)
    }

    private fun startBackgroundService() {
        val serviceIntent = Intent(this, BackgroundService::class.java)
        startForegroundService(serviceIntent)
    }
}

@Composable
fun WearApp(greetingName: String) {
    SpotifyJournalTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            TimeText()
            Greeting(greetingName = greetingName)
        }
    }
}

@Composable
fun Greeting(greetingName: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = stringResource(R.string.hello_world, greetingName)
    )
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp("Preview Android")
}
