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
 * Updated to load the model without using the TFLite Support library to avoid manifest conflicts.
 */
object SoundClassifier {

    private const val MODEL_FILENAME = "yamnet.tflite"
    private const val CONFIDENCE_THRESHOLD = 0.3f

    private val TARGET_LABELS = mapOf(
        20 to "Baby crying",
        350 to "Doorbell",
        353 to "Knock",
        382 to "Alarm",
        384 to "Telephone bell ringing"
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

    /**
     * Loads the model file from the assets folder using raw FileChannel.
     */
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

        for ((index, label) in TARGET_LABELS) {
            val confidence = scores[index]
            if (confidence > maxConfidence && confidence > CONFIDENCE_THRESHOLD) {
                maxConfidence = confidence
                bestLabel = label
            }
        }

        return if (bestLabel != null) {
            Log.i("SoundClassifier", "Detected: $bestLabel ($maxConfidence)")
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
