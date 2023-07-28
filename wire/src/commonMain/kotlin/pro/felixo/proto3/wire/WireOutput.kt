package pro.felixo.proto3.wire

@Suppress("MagicNumber")
class WireOutput {
    private var buffer: ByteArray = ByteArray(INITIAL_BUFFER_SIZE)

    var length: Int = 0
        private set

    private fun ensureCapacity(bytes: Int) {
        if (length + bytes <= buffer.size)
            return
        val newBuffer = ByteArray((length + bytes).takeHighestOneBit() shl 1)
        buffer.copyInto(newBuffer)
        buffer = newBuffer
    }

    fun getBytes(): ByteArray {
        val ret = ByteArray(length)
        buffer.copyInto(ret, 0, 0, length)
        return ret
    }

    fun writeFixed32(value: Int) {
        ensureCapacity(4)
        for (i in 0..3)
            buffer[length++] = (value shr i * 8).toByte()
    }

    fun writeFixed64(value: Long) {
        ensureCapacity(8)
        for (i in 0..7)
            buffer[length++] = (value shr i * 8).toByte()
    }

    fun writeVarInt(value: Int, extraBytes: Int = 0) {
        val varIntLength = (31 - value.countLeadingZeroBits()) / 7 + extraBytes
        ensureCapacity(varIntLength + 1)
        var current: Int = value
        for (i in 0 until varIntLength) {
            buffer[length + i] = ((current and 0x7F) or 0x80).toByte()
            current = current ushr 7
        }
        buffer[length + varIntLength] = current.toByte()
        length += varIntLength + 1
    }

    fun writeVarInt(value: Long, extraBytes: Int = 0) {
        val varIntLength = (63 - value.countLeadingZeroBits()) / 7 + extraBytes
        ensureCapacity(varIntLength + 1)
        var current: Long = value
        for (i in 0 until varIntLength) {
            buffer[length + i] = ((current and 0x7F) or 0x80).toByte()
            current = current ushr 7
        }
        buffer[length + varIntLength] = current.toByte()
        length += varIntLength + 1
    }

    fun writeByte(value: Byte) {
        ensureCapacity(1)
        buffer[length] = value
        length++
    }

    fun write(value: ByteArray) {
        ensureCapacity(value.size)
        value.copyInto(buffer, length)
        length += value.size
    }

    fun write(value: WireInput) {
        val inputSize = value.remaining
        ensureCapacity(inputSize)
        value.writeTo(buffer, length)
        length += inputSize
    }

    companion object {
        private const val INITIAL_BUFFER_SIZE = 32
    }
}

fun WireOutput.encodeField(tag: Tag, value: WireValue) {
    writeVarInt(tag.value)
    encodeValue(value)
}

fun WireOutput.encodeValue(value: WireValue) {
    when (value) {
        is WireValue.VarInt -> writeVarInt(value.value)
        is WireValue.Fixed64 -> writeFixed64(value.value)
        is WireValue.Len -> {
            writeVarInt(value.value.remaining)
            write(value.value)
        }
        is WireValue.Fixed32 -> writeFixed32(value.value)
    }
}