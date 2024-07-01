package com.example.milkpredict

import android.content.res.AssetManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.ByteBuffer
import java.nio.ByteOrder

class SimodelActivity : AppCompatActivity() {
    private lateinit var interpreter: Interpreter
    private val mModelPath = "milk.tflite"
    private lateinit var resultText: TextView
    private lateinit var pH: EditText
    private lateinit var Temprature: EditText
    private lateinit var Taste: EditText
    private lateinit var Odor: EditText
    private lateinit var Fat: EditText
    private lateinit var Turbidity: EditText
    private lateinit var Colour: EditText
    private lateinit var checkButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simodel)

        resultText = findViewById(R.id.txtResult)
        pH = findViewById(R.id.pH)
        Temprature = findViewById(R.id.Temprature)
        Taste = findViewById(R.id.Taste)
        Odor = findViewById(R.id.Odor)
        Fat = findViewById(R.id.Fat)
        Turbidity = findViewById(R.id.Turbidity)
        Colour = findViewById(R.id.Colour)
        checkButton = findViewById(R.id.btnCheck)

        checkButton.setOnClickListener {
            val result = doInference(
                pH.text.toString(),
                Temprature.text.toString(),
                Taste.text.toString(),
                Odor.text.toString(),
                Fat.text.toString(),
                Turbidity.text.toString(),
                Colour.text.toString()
            )
            runOnUiThread {
                when (result) {
                    0 -> resultText.text = "low"
                    1 -> resultText.text = "medium"
                    2 -> resultText.text = "high"
                }
            }
        }
        initInterpreter()
    }

    private fun initInterpreter() {
        val options = Interpreter.Options()
        options.setNumThreads(8)
        options.setUseNNAPI(true)
        interpreter = Interpreter(loadModelFile(assets, mModelPath), options)
    }

    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun doInference(vararg inputs: String): Int {
        val inputVal = FloatArray(inputs.size)
        for (i in inputs.indices) {
            inputVal[i] = inputs[i].toFloat()
        }

        // Normalisasi data input sesuai dengan preprocessing dalam notebook
        val normalizedInput = normalizeInput(inputVal)

        val inputBuffer = ByteBuffer.allocateDirect(4 * inputs.size).order(ByteOrder.nativeOrder())
        for (value in normalizedInput) {
            inputBuffer.putFloat(value)
        }

        val outputBuffer = ByteBuffer.allocateDirect(4 * 3).order(ByteOrder.nativeOrder())
        interpreter.run(inputBuffer, outputBuffer)
        outputBuffer.rewind()

        val output = FloatArray(3)
        outputBuffer.asFloatBuffer().get(output)

        return output.indices.maxByOrNull { output[it] } ?: -1
    }

    private fun normalizeInput(input: FloatArray): FloatArray {
        val minValues = floatArrayOf(3.0f, 34.0f, 0.0f, 0.0f, 0.0f, 0.0f, 50.0f)  // Contoh nilai minimum dari dataset
        val maxValues = floatArrayOf(9.0f, 90.0f, 1.0f, 1.0f, 1.0f, 1.0f, 255.0f)  // Contoh nilai maksimum dari dataset
        val normalizedInput = FloatArray(input.size)

        for (i in input.indices) {
            normalizedInput[i] = (input[i] - minValues[i]) / (maxValues[i] - minValues[i])
        }
        return normalizedInput
    }
}
