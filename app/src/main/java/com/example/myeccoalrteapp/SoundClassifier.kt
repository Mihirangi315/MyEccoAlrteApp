package com.example.myeccoalrteapp

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

object SoundClassifier {

    private const val MODEL_FILENAME = "yamnet.tflite"
    
    // Minimum score to even consider it a "match"
    private const val CONFIDENCE_THRESHOLD = 0.05f 

    // Grouping labels correctly to match your Settings exactly
    private val TARGET_LABELS = mapOf(
        20 to "Baby crying",
        21 to "Baby crying",
        350 to "Doorbell",
        351 to "Doorbell",
        353 to "Knock",
        382 to "Alarm",
        383 to "Telephone bell ringing", // Updated index 383
        384 to "Telephone bell ringing",
        385 to "Telephone bell ringing", // Ringtone index
        390 to "Alarm",
        393 to "Alarm"
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
        var maxConfidence = 0f
        var bestLabel: String? = null

        // 1. Check for our target sounds
        for ((index, label) in TARGET_LABELS) {
            val confidence = scores[index]
            if (confidence > maxConfidence && confidence > CONFIDENCE_THRESHOLD) {
                maxConfidence = confidence
                bestLabel = label
            }
        }

        // 2. DEBUGGING: If nothing caught, find the #1 sound out of ALL 521 sounds
        // This helps us see if the model thinks the sound is something else (like "Speech")
        if (bestLabel == null) {
            var topScore = 0f
            var topIndex = -1
            for (i in scores.indices) {
                if (scores[i] > topScore) {
                    topScore = scores[i]
                    topIndex = i
                }
            }
            Log.v("SoundClassifier", "Top sound heard: Index $topIndex with score $topScore")
        }

        return if (bestLabel != null) {
            Log.i("SoundClassifier", "MATCH! Detected: $bestLabel ($maxConfidence)")
            Pair(bestLabel, maxConfidence)
        } else {
            null
        }
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}
