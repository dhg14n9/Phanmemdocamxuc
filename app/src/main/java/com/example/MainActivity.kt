package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.ui.AuraApp
import com.example.ui.AuraViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val vm by lazy {
        ViewModelProvider(this)[AuraViewModel::class.java]
    }
    
    private val headsetReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_HEADSET_PLUG -> {
                    val state = intent.getIntExtra("state", -1)
                    vm.updateHeadsetState(state == 1)
                }
                android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    vm.updateHeadsetState(true)
                }
                android.bluetooth.BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    vm.updateHeadsetState(false)
                }
                AudioManager.ACTION_AUDIO_BECOMING_NOISY -> {
                    vm.updateHeadsetState(false)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        com.example.ui.AppPreferences.init(applicationContext)
        setContent {
            MyApplicationTheme {
                AuraApp(vm)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_HEADSET_PLUG)
            addAction(android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(android.bluetooth.BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        }
        registerReceiver(headsetReceiver, filter)
        
        // Initial check
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        vm.updateHeadsetState(audioManager.isWiredHeadsetOn || audioManager.isBluetoothA2dpOn)
    }

    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(headsetReceiver)
        } catch (e: Exception) {}
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK || 
            keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || 
            keyCode == KeyEvent.KEYCODE_MEDIA_PLAY || 
            keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            
            // Trigger voice/chat hands-free voice interaction via Bluetooth or Wired headset button!
            vm.toggleHeadsetVoiceListening(applicationContext)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
