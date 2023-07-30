package pro.felixo.proto3.internal

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import pro.felixo.proto3.ProtoNumber
import pro.felixo.proto3.schema.FieldRule
import pro.felixo.proto3.util.FieldNumberIterator
import pro.felixo.proto3.util.NumberIterator
import pro.felixo.proto3.util.requireNoDuplicates

@OptIn(ExperimentalSerializationApi::class)
fun numberIteratorFromEnumElements(descriptor: SerialDescriptor) = NumberIterator(
    1,
    reserved = (0 until descriptor.elementsCount).mapNotNull {
        descriptor.getElementAnnotations(it).filterIsInstance<ProtoNumber>().firstOrNull()?.number
    }.requireNoDuplicates { "Duplicate field number in descriptor ${descriptor.serialName}: $it" }
        .map { it..it }
)

@OptIn(ExperimentalSerializationApi::class)
fun fieldNumberIteratorFromClassElements(descriptor: SerialDescriptor) =
    FieldNumberIterator(
        (0 until descriptor.elementsCount).mapNotNull {
            descriptor.getElementAnnotations(it).filterIsInstance<ProtoNumber>().firstOrNull()?.number
        }.requireNoDuplicates { "Duplicate field number in descriptor ${descriptor.serialName}: $it" }
    )

@OptIn(ExperimentalSerializationApi::class)
fun fieldNumberIteratorFromSubTypes(descriptors: Iterable<SerialDescriptor>): FieldNumberIterator {
    return FieldNumberIterator(
        descriptors.mapNotNull {
            it.annotations.filterIsInstance<ProtoNumber>().firstOrNull()?.number
        }.requireNoDuplicates { duplicatedNumber ->
            "Duplicate field number $duplicatedNumber in sub-types: ${descriptors.map { it.serialName }}"
        }
    )
}

@OptIn(ExperimentalSerializationApi::class)
fun SerialDescriptor.nullableToOptional() = if (isNullable) FieldRule.Optional else FieldRule.Singular

private val polymorphicNamePattern = Regex("""^kotlinx\.serialization\.Polymorphic<(.*)>\??""")

fun simpleTypeName(descriptor: SerialDescriptor): String =
    fullTypeName(descriptor).substringAfterLast('.')

@OptIn(ExperimentalSerializationApi::class)
fun fullTypeName(descriptor: SerialDescriptor): String =
    (polymorphicNamePattern.find(descriptor.serialName)?.groupValues?.get(1)
        ?: descriptor.serialName).removeSuffix("?")

@OptIn(ExperimentalSerializationApi::class)
fun SerialDescriptor.isCompatibleWith(other: SerialDescriptor): Boolean =
    other === this || (
        kind == other.kind &&
            (0 until elementsCount).all {
                getElementName(it) == other.getElementName(it) &&
                getElementDescriptor(it) == other.getElementDescriptor(it) &&
                getElementAnnotations(it) == other.getElementAnnotations(it)
            }
        )

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> List<*>.castItems(): List<T> {
    require(all { it is T })
    return this as List<T>
}
