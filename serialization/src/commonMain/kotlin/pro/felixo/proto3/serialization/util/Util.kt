package pro.felixo.proto3.serialization.util

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor

private val polymorphicNamePattern = Regex("""^kotlinx\.serialization\.Polymorphic<(.*)>\??""")

fun simpleTypeName(descriptor: SerialDescriptor): String =
    fullTypeName(descriptor).substringAfterLast('.')

@OptIn(ExperimentalSerializationApi::class)
private fun fullTypeName(descriptor: SerialDescriptor): String =
    (polymorphicNamePattern.find(descriptor.serialName)?.groupValues?.get(1)
        ?: descriptor.serialName).removeSuffix("?")

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> List<*>.castItems(): List<T> {
    require(all { it is T })
    return this as List<T>
}
