package com.example.myeccoalrteapp

import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    
    // UI Elements
    private lateinit var switchDoorbell: SwitchMaterial
    private lateinit var switchAlarm: SwitchMaterial
    private lateinit var switchBaby: SwitchMaterial
    private lateinit var switchPhone: SwitchMaterial
    private lateinit var switchKnock: SwitchMaterial
    private lateinit var switchRingtone: SwitchMaterial
    private lateinit var switchContinuous: SwitchMaterial
    private lateinit var sbSensitivity: SeekBar
    private lateinit var tvSensitivityValue: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        database = AppDatabase.getDatabase(this)
        initViews()
        loadSettings()
        setupListeners()
    }

    private fun initViews() {
        switchDoorbell = findViewById(R.id.switchDoorbell)
        switchAlarm = findViewById(R.id.switchAlarm)
        switchBaby = findViewById(R.id.switchBaby)
        switchPhone = findViewById(R.id.switchPhone)
        switchKnock = findViewById(R.id.switchKnock)
        switchRingtone = findViewById(R.id.switchRingtone)
        switchContinuous = findViewById(R.id.switchContinuous)
        sbSensitivity = findViewById(R.id.sbSensitivity)
        tvSensitivityValue = findViewById(R.id.tvSensitivityValue)

        findViewById<MaterialButton>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    private fun loadSettings() {
        lifecycleScope.launch {
            val settings = database.soundSettingDao().getAllSettings()
            
            settings.forEach { setting ->
                when (setting.soundLabel) {
                    "Doorbell" -> switchDoorbell.isChecked = setting.isEnabled
                    "Alarm" -> switchAlarm.isChecked = setting.isEnabled
                    "Baby crying" -> switchBaby.isChecked = setting.isEnabled
                    "Telephone bell ringing" -> switchPhone.isChecked = setting.isEnabled
                    "Knock" -> switchKnock.isChecked = setting.isEnabled
                    "RingtoneAlert" -> switchRingtone.isChecked = setting.isEnabled
                    "ContinuousAlert" -> switchContinuous.isChecked = setting.isEnabled
                    "GlobalSensitivity" -> {
                        val progress = (setting.sensitivity * 100).toInt()
                        sbSensitivity.progress = progress
                        tvSensitivityValue.text = "$progress%"
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        val switches = mapOf(
            switchDoorbell to "Doorbell",
            switchAlarm to "Alarm",
            switchBaby to "Baby crying",
            switchPhone to "Telephone bell ringing",
            switchKnock to "Knock",
            switchRingtone to "RingtoneAlert",
            switchContinuous to "ContinuousAlert"
        )

        switches.forEach { (switch, label) ->
            switch.setOnCheckedChangeListener { _, isChecked ->
                saveSoundSetting(label, isChecked)
            }
        }

        sbSensitivity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvSensitivityValue.text = "$progress%"
                if (fromUser) {
                    saveSensitivity(progress / 100f)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun saveSoundSetting(label: String, enabled: Boolean) {
        lifecycleScope.launch {
            val current = database.soundSettingDao().getSettingForSound(label)
            val sensitivity = current?.sensitivity ?: 0.5f
            database.soundSettingDao().saveSetting(SoundSetting(label, enabled, sensitivity))
        }
    }

    private fun saveSensitivity(value: Float) {
        lifecycleScope.launch {
            database.soundSettingDao().saveSetting(SoundSetting("GlobalSensitivity", true, value))
        }
    }
}
