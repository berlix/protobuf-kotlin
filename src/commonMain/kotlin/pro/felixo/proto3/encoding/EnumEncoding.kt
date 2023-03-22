package pro.felixo.proto3.encoding

data class EnumEncoding(
    val numberByElementIndex: List<Int>
) {
    private val elementIndexByNumber = numberByElementIndex.withIndex().associate { it.value to it.index }
    private val defaultElementIndex: Int = numberByElementIndex.indexOf(0)

    fun decode(number: Int): Int = elementIndexByNumber[number] ?: defaultElementIndex
    fun encode(elementIndex: Int): Int = numberByElementIndex[elementIndex]
}
