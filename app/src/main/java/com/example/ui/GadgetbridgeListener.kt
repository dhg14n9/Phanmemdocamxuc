package com.example.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun GadgetbridgeListener(viewModel: AuraViewModel) {
    val context = LocalContext.current

    val receiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val action = intent.action
                Log.d("GadgetbridgeListener", "Received: $action")
                
                when (action) {
                    "nodomain.freeyourgadget.gadgetbridge.intent.action.REALTIME_HEARTRATE",
                    "nodomain.freeyourgadget.gadgetbridge.devices.action.realtime_samples" -> {
                        val hr = intent.getIntExtra("HEARTRATE", -1).takeIf { it > 0 }
                            ?: intent.getIntExtra("EXTRA_HEARTRATE", -1).takeIf { it > 0 }
                            ?: intent.getIntExtra("realtime_sample_heart_rate", -1).takeIf { it > 0 }
                            ?: intent.getIntExtra("heart_rate", -1)
                        if (hr > 0) {
                            viewModel.updateBpm(hr)
                            viewModel.updateWatchConnection(true, "Xiaomi Band (Gadgetbridge)")
                        }
                    }
                    "nodomain.freeyourgadget.gadgetbridge.intent.action.REALTIME_SPO2",
                    "nodomain.freeyourgadget.gadgetbridge.intent.action.SPO2" -> {
                        val spo2 = intent.getIntExtra("SPO2", -1)
                        if (spo2 > 0) {
                            viewModel.updateSpo2(spo2)
                            viewModel.updateWatchConnection(true, "Xiaomi Band (Gadgetbridge)")
                        }
                    }
                    "nodomain.freeyourgadget.gadgetbridge.intent.action.SLEEP" -> {
                        // Normally Gadgetbridge doesn't send sleep score this way, but we handle it just in case
                        val sleepScore = intent.getIntExtra("SLEEP_SCORE", -1)
                        if (sleepScore > 0) {
                            // Can add to viewmodel if sleep metric is supported
                            Log.d("GadgetbridgeListener", "Sleep: $sleepScore")
                        }
                    }
                }
            }
        }
    }

    DisposableEffect(context) {
        val filter = IntentFilter().apply {
            addAction("nodomain.freeyourgadget.gadgetbridge.intent.action.REALTIME_HEARTRATE")
            addAction("nodomain.freeyourgadget.gadgetbridge.devices.action.realtime_samples")
            addAction("nodomain.freeyourgadget.gadgetbridge.intent.action.REALTIME_SPO2")
            addAction("nodomain.freeyourgadget.gadgetbridge.intent.action.SPO2")
            addAction("nodomain.freeyourgadget.gadgetbridge.intent.action.SLEEP")
        }
        
        androidx.core.content.ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            androidx.core.content.ContextCompat.RECEIVER_EXPORTED
        )

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }
}
