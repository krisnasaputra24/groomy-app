package com.krisna.groomy.data.ml

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.flex.FlexDelegate
import java.io.FileInputStream
import java.nio.channels.FileChannel

class KeluhanClassifier(context: Context) {

    private var interpreter: Interpreter? = null
    private var flexDelegate: FlexDelegate? = null
    private var labels: List<String> = emptyList()
    private var vocab: Map<String, Int> = emptyMap()
    private val MAX_LEN = 50

    data class ClassificationResult(
        val label: String,
        val confidence: Float
    )

    init {
        try {
            // 1. Load Model
            val fileDescriptor: AssetFileDescriptor = context.assets.openFd("keluhan_model.tflite")
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.length
            val modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)

            // 2. Inisialisasi FlexDelegate secara EKSPLISIT (Solusi Utama Error READ_VARIABLE)
            flexDelegate = FlexDelegate()
            
            val options = Interpreter.Options().apply {
                addDelegate(flexDelegate)
                setNumThreads(4)
                setUseXNNPACK(false) // WAJIB false saat menggunakan Flex/READ_VARIABLE
            }
            
            interpreter = Interpreter(modelBuffer, options)

            // 3. Load Labels & Vocab
            labels = LabelLoader(context).loadLabels("labels.json")
            val tempVocab = mutableMapOf<String, Int>()
            context.assets.open("vocab.txt").bufferedReader().useLines { lines ->
                lines.forEachIndexed { index, word ->
                    val clean = word.trim().lowercase()
                    if (clean.isNotEmpty()) tempVocab[clean] = index
                }
            }
            vocab = tempVocab
            Log.d("KeluhanClassifier", "FlexDelegate initialized and Model loaded.")
        } catch (e: Exception) {
            Log.e("KeluhanClassifier", "CRITICAL ERROR: ${e.message}")
        }
    }

    fun classify(text: String): ClassificationResult {
        val currentInterpreter = interpreter ?: return ClassificationResult("Model Error", 0f)
        
        return try {
            val inputIds = IntArray(MAX_LEN) { 0 }
            val words = text.lowercase()
                .replace(Regex("[^a-z\\s]"), "")
                .split(" ")
                .filter { it.isNotBlank() }

            for (i in 0 until minOf(words.size, MAX_LEN)) {
                inputIds[i] = vocab[words[i]] ?: 0
            }

            // Input: [1, 50], Output: [1, 30]
            val inputBuffer = arrayOf(inputIds)
            val outputBuffer = Array(1) { FloatArray(30) }

            currentInterpreter.run(inputBuffer, outputBuffer)

            val results = outputBuffer[0]
            val maxIdx = results.indices.maxByOrNull { results[it] } ?: 0
            val maxScore = results[maxIdx]

            ClassificationResult(
                label = labels.getOrElse(maxIdx) { "unknown" },
                confidence = maxScore
            )
        } catch (e: Exception) {
            Log.e("KeluhanClassifier", "Inference Failed: ${e.message}")
            ClassificationResult("Error: ${e.localizedMessage}", 0f)
        }
    }

    fun close() {
        interpreter?.close()
        flexDelegate?.close()
        interpreter = null
        flexDelegate = null
    }
}
