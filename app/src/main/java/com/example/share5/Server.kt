package com.example.share5

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.net.ServerSocket

suspend fun Server(context: Context, uris: List<Uri>, fileNames: List<String>) {
    withContext(Dispatchers.IO) {
        try {
            // Create server socket
            Log.d("App", "Server on")
            val serverSocket = ServerSocket(37682)
            serverSocket.soTimeout=5000

            // Wait for a client to connect
            val clientSocket = serverSocket.accept()
            Log.d("App", "Client connected")

            // Send the number of files to the client.
            val numFiles = uris.size
            val outputStream = clientSocket.getOutputStream()
            val dataOutputStream = DataOutputStream(outputStream)
            dataOutputStream.writeInt(numFiles)

            // Send the files names to the client
            val outputString = fileNames.joinToString(separator = "\n")
            dataOutputStream.writeInt(outputString.length)
            dataOutputStream.writeBytes(outputString)
            dataOutputStream.flush()

            // Send each file
            for (uri in uris) {
                val fileSize = uriToByteArray(context.contentResolver, uri, outputStream)
                Log.d("App", "File sent: $fileSize bytes")
            }

            // Close the connection
            clientSocket.close()
            serverSocket.close()
        } catch (e: IOException) {
            Log.e("App", "Failed to start server: ${e.message}")
        }
    }
}

private fun sendByteArray(outputStream: OutputStream, byteArray: ByteArray) {
    val dataOutputStream = DataOutputStream(outputStream)
    dataOutputStream.writeInt(byteArray.size)
    dataOutputStream.write(byteArray)
    dataOutputStream.flush()
}

private fun uriToByteArray(contentResolver: ContentResolver, uri: Uri, outputStream: OutputStream): Long {
    var fileSize = 0L
    var inputStream: InputStream? = null

    try {
        inputStream = contentResolver.openInputStream(uri)
        val buffer = ByteArray(BUFFER_SIZE)
        var bytesRead: Int

        if (inputStream != null) {
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                sendByteArray(outputStream, buffer.copyOf(bytesRead))
                fileSize += bytesRead
            }
        }

        Log.d("App", "File read: $fileSize bytes")
    } catch (e: IOException) {
        Log.e("App", "Failed to read file: ${e.message}")
    } finally {
        try {
            inputStream?.close()
        } catch (e: IOException) {
            Log.e("App", "Failed to close input stream: ${e.message}")
        }
    }

    return fileSize
}

private const val BUFFER_SIZE = 8192

//
//
//
//
//fun uriToByteArray(contentResolver: ContentResolver, uri: Uri): ByteArray {
//    val inputStream: InputStream? = contentResolver.openInputStream(uri)
//    val buffer = ByteArrayOutputStream()
//
//    inputStream?.use { input ->
//        val data = ByteArray(1024)
//        var bytesRead: Int
//        while (input.read(data).also { bytesRead = it } != -1) {
//            buffer.write(data, 0, bytesRead)
//        }
//    }
//
//    return buffer.toByteArray()
//}
//
//fun sendByteArrays(outputStream: OutputStream, byteArray: ByteArray) {
//
//    val dataOutputStream = DataOutputStream(outputStream)
//    dataOutputStream.writeInt(byteArray.size)
//    dataOutputStream.write(byteArray)
//    dataOutputStream.flush()
//}
//
//
//@SuppressLint("Recycle")
//suspend fun Server(context: Context, uris: List<Uri>,fileNames:List<String>) {
//    withContext(Dispatchers.IO) {
//        try {
//            // Create server socket
//            Log.d("App", "Server on")
//            val serverSocket = ServerSocket(37682)
//
//            // Wait for a client to connect
//            val clientSocket = serverSocket.accept()
//            Log.d("App", "Client connected")
//
//            // Send the number of files to the client.
//
//            val numFiles = uris.size
//            val outputStream = clientSocket.getOutputStream()
//            val dataOutputStream = DataOutputStream(outputStream)
//            dataOutputStream.writeInt(numFiles)
//
//            // Send the  files names  to the client
//
//            val outputString = fileNames.joinToString(separator = "\n")
//            dataOutputStream.writeInt(outputString.length)
//            dataOutputStream.writeBytes(outputString)
//            dataOutputStream.flush()
//
//            for (uri in uris){
//                val byteArray = uriToByteArray(context.contentResolver,uri)
//                Log.d("App","byteArray size = ${byteArray.size}")
//                sendByteArrays(outputStream,byteArray)
//            }
//
//
//
//            // Close the connection
//            clientSocket.close()
//            serverSocket.close()
//        } catch (e: IOException) {
//            Log.e("App", "Failed to start server: ${e.message}")
//        }
//    }
//}
//
//
//
//
//
//
//
