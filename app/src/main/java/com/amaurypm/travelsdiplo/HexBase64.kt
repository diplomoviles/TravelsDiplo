package com.amaurypm.travelsdiplo

import android.os.Build
import androidx.annotation.RequiresApi
import java.util.Base64

@RequiresApi(Build.VERSION_CODES.O)
fun main(){
    val hexSha1 = "64:BC:37:33:4C:DB:85:87:43:D8:73:52:1F:E5:90:6B:6E:D3:59:17"
    println("Base64 SHA-1: ${hexSha1ToBase64(hexSha1)}")
}

// Función para convertir una cadena hexadecimal a un array de bytes
fun hexToByteArray(hex: String): ByteArray {
    val cleanHex = hex.replace(":", "") // Eliminar los dos puntos
    val len = cleanHex.length
    val data = ByteArray(len / 2)
    for (i in 0 until len step 2) {
        data[i / 2] = ((Character.digit(cleanHex[i], 16) shl 4)
                + Character.digit(cleanHex[i + 1], 16)).toByte()
    }
    return data
}

// Función para codificar un array de bytes en base64
@RequiresApi(Build.VERSION_CODES.O)
fun byteArrayToBase64(byteArray: ByteArray): String {
    return Base64.getEncoder().encodeToString(byteArray)
}

// Función para convertir un SHA-1 hexadecimal a base64
@RequiresApi(Build.VERSION_CODES.O)
fun hexSha1ToBase64(hexSha1: String): String {
    val byteArray = hexToByteArray(hexSha1)
    return byteArrayToBase64(byteArray)
}