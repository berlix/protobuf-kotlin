@file:Suppress("MagicNumber")

package pro.felixo.proto3.wire

fun Int.encodeSInt32(): Int = (this shl 1) xor (this shr 31)
fun Int.decodeSInt32(): Int = (this ushr 1) xor -(this and 1)

fun Long.encodeSInt64(): Long = (this shl 1) xor (this shr 63)
fun Long.decodeSInt64(): Long = (this ushr 1) xor -(this and 1)
