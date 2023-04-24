package com.example.share5

import android.content.ContentResolver
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.net.*

suspend fun Client(context: Context, device: InetAddress?, activity: MainActivity) {

    val directory = activity.getOutputDirectory()

    withContext(Dispatchers.IO) {
        try {
            // Connect to server
            Log.d("App", "Client connecting...")
            val clientSocket = Socket(device, 37682)
            Log.d("App", "Client connected")

            // Receive the number of files from the server
            val inputStream = clientSocket.getInputStream()
            val dataInputStream = DataInputStream(inputStream)
            val numFiles = dataInputStream.readInt()
            Log.d("App", "Number of files = $numFiles")

            // Receive the files names from the server
            val fileNameLength = dataInputStream.readInt()
            val fileNameBytes = ByteArray(fileNameLength)
            dataInputStream.readFully(fileNameBytes)
            val fileNames = String(fileNameBytes).split("\n")
            Log.d("App", "File names = $fileNames")

            // Receive each file
            for (i in 0 until numFiles) {
                val startTime = System.currentTimeMillis()
                val fileSize = byteArrayToUri(context.contentResolver, directory, fileNames[i], clientSocket.getInputStream()).toDouble()/1024/1024
                Log.d("App", "File received: $fileSize bytes")
                val endTime = System.currentTimeMillis()
                val transferTime = (endTime.toDouble() - startTime.toDouble())/1000

                val transferSpeed = fileSize / (transferTime) // calculate speed in MB/s
                Log.d("App","file transfer time = $transferTime seconds and speed = $transferSpeed Mbps")
            }

            // Close the connection
            clientSocket.close()
        } catch (e: IOException) {
            Log.e("App", "Failed to connect to server: ${e.message}")
        }
    }
}

private fun receiveByteArray(inputStream: InputStream): ByteArray {
    val dataInputStream = DataInputStream(inputStream)
    val byteArraySize = dataInputStream.readInt()
    val byteArray = ByteArray(byteArraySize)
    dataInputStream.readFully(byteArray)
    return byteArray
}

private fun byteArrayToUri(contentResolver: ContentResolver, directory: File, fileName: String, inputStream: InputStream): Long {
    val file = File(directory, fileName)
    val outputStream = FileOutputStream(file)
    var fileSize = 0L

    try {
        var byteArray = receiveByteArray(inputStream)
        while (byteArray.isNotEmpty()) {
            outputStream.write(byteArray)
            fileSize += byteArray.size
            byteArray = receiveByteArray(inputStream)
        }

        Log.d("App", "File saved: ${file.absolutePath}, $fileSize bytes")
    } catch (e: IOException) {
        Log.e("App", "Failed to save file: ${e.message}")
    } finally {
        try {
            outputStream.close()
        } catch (e: IOException) {
            Log.e("App", "Failed to close output stream: ${e.message}")
        }
    }

    return fileSize
}
