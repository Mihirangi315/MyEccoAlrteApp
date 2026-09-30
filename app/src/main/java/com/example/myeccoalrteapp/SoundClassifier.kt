package com.example.myeccoalrteapp

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * SoundClassifier uses a pre-trained YAMNet model to identify sounds.
 * Updated with "Smart Competition" logic to reduce false positives from music and speech.
 */
object SoundClassifier {

    private const val MODEL_FILENAME = "yamnet.tflite"
    
    // Raised threshold to 0.15 to filter out low-level background room noise
    private const val CONFIDENCE_THRESHOLD = 0.15f 

    // Grouping labels: Removed broad indices prone to false positives (like general 'Telephone')
    private val TARGET_LABELS = mapOf(
        20 to "Baby crying",
        21 to "Baby crying",
        350 to "Doorbell",
        351 to "Doorbell",
        353 to "Knock",
        382 to "Alarm",
        384 to "Telephone bell ringing", // Most specific telephone bell
        390 to "Alarm",                  // Siren
        393 to "Alarm"                   // Smoke detector
    )

    private var interpreter: Interpreter? = null

    fun initialize(context: Context) {
        if (interpreter != null) return
        try {
            val model = loadModelFile(context)
            val options = Interpreter.Options()
            interpreter = Interpreter(model, options)
            Log.d("SoundClassifier", "YAMNet model loaded manually.")
        } catch (e: Exception) {
            Log.e("SoundClassifier", "Error loading model: ${e.message}")
        }
    }

    private fun loadModelFile(context: Context): MappedByteBuffer {
        val fileDescriptor: AssetFileDescriptor = context.assets.openFd(MODEL_FILENAME)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel: FileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * Runs inference and compares target sounds against background noise (Music/Speech).
     */
    fun classify(audioBuffer: FloatArray): Pair<String, Float>? {
        val tflite = interpreter ?: return null

        val input = arrayOf(audioBuffer)
        val output = Array(1) { FloatArray(521) }

        try {
            tflite.run(input, output)
        } catch (e: Exception) {
            Log.e("SoundClassifier", "Inference failed: ${e.message}")
            return null
        }

        val scores = output[0]

        // 1. Find the Absolute Top Sound out of all 521 categories (Music, Speech, etc.)
        var globalTopScore = 0f
        var globalTopIndex = -1
        for (i in scores.indices) {
            if (scores[i] > globalTopScore) {
                globalTopScore = scores[i]
                globalTopIndex = i
            }
        }

        // 2. Find the best sound among our TARGET categories
        var bestTargetScore = 0f
        var bestTargetLabel: String? = null
        for ((index, label) in TARGET_LABELS) {
            if (scores[index] > bestTargetScore) {
                bestTargetScore = scores[index]
                bestTargetLabel = label
            }
        }

        // 3. SMART COMPETITION LOGIC
        // Case A: No target sound heard or it's below our minimum threshold
        if (bestTargetLabel == null || bestTargetScore < CONFIDENCE_THRESHOLD) {
            return null
        }

        // Case B: A target was heard! Now check if it's "competing" with noise (like Music)
        // If the top global sound is NOT a target sound, we check the ratio.
        if (!TARGET_LABELS.containsKey(globalTopIndex)) {
            // If the target sound is less than 50% as strong as the noise (Music/Speech), ignore it.
            if (bestTargetScore < (globalTopScore * 0.5f)) {
                Log.d("SoundClassifier", "Ignoring $bestTargetLabel ($bestTargetScore) due to stronger noise: Index $globalTopIndex ($globalTopScore)")
                return null
            }
        }

        // Case C: Target sound is strong and "wins" or is competitive enough!
        Log.i("SoundClassifier", "MATCH! Detected: $bestTargetLabel ($bestTargetScore)")
        return Pair(bestTargetLabel, bestTargetScore)
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}
