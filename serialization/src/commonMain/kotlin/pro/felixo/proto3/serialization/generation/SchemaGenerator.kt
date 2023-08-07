package pro.felixo.proto3.serialization.generation

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.elementDescriptors
import kotlinx.serialization.descriptors.getContextualDescriptor
import kotlinx.serialization.descriptors.getPolymorphicDescriptors
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializerOrNull
import pro.felixo.proto3.EnumValue
import pro.felixo.proto3.FieldNumber
import pro.felixo.proto3.FieldRule
import pro.felixo.proto3.Identifier
import pro.felixo.proto3.serialization.EncodingSchema
import pro.felixo.proto3.serialization.Enum
import pro.felixo.proto3.serialization.Field
import pro.felixo.proto3.serialization.IntegerType
import pro.felixo.proto3.serialization.Message
import pro.felixo.proto3.serialization.OneOf
import pro.felixo.proto3.serialization.ProtoDefaultEnumValue
import pro.felixo.proto3.serialization.ProtoIntegerType
import pro.felixo.proto3.serialization.ProtoMapEntry
import pro.felixo.proto3.serialization.ProtoNumber
import pro.felixo.proto3.serialization.encoding.FieldEncoding
import pro.felixo.proto3.serialization.encoding.ListDecoder
import pro.felixo.proto3.serialization.encoding.ListEncoder
import pro.felixo.proto3.serialization.encoding.MapDecoder
import pro.felixo.proto3.serialization.encoding.MapEncoder
import pro.felixo.proto3.serialization.encoding.MessageDecoder
import pro.felixo.proto3.serialization.encoding.MessageEncoder
import pro.felixo.proto3.serialization.encoding.PolymorphicDecoder
import pro.felixo.proto3.serialization.encoding.PolymorphicEncoder
import pro.felixo.proto3.serialization.encoding.SyntheticDecoder
import pro.felixo.proto3.serialization.encoding.SyntheticEncoder
import pro.felixo.proto3.serialization.encoding.ValueDecoder
import pro.felixo.proto3.serialization.encoding.ValueEncoder
import pro.felixo.proto3.serialization.util.FieldNumberIterator
import pro.felixo.proto3.serialization.util.simpleTypeName
import pro.felixo.proto3.wire.WireBuffer
import pro.felixo.proto3.wire.WireValue
import kotlin.reflect.KType

@OptIn(ExperimentalSerializationApi::class)
class SchemaGenerator(
    val serializersModule: SerializersModule = EmptySerializersModule()
) {
    private val rootTypes = TypeContext()

    fun schema(): EncodingSchema = EncodingSchema(serializersModule, rootTypes.localTypesByName)

    fun add(descriptor: SerialDescriptor) = rootTypes.namedType(descriptor)

    fun addFromSerializersModule(type: KType) {
        rootTypes.namedType(
            serializersModule.serializerOrNull(type)?.descriptor ?: error("No serializer in module found for $type")
        )
    }

    private fun TypeContext.namedType(descriptor: SerialDescriptor): FieldEncoding.Reference = when (descriptor.kind) {
        PolymorphicKind.OPEN -> messageOfOpenPolymorphicClass(descriptor)
        PolymorphicKind.SEALED -> messageOfSealedPolymorphicClass(descriptor)
        SerialKind.CONTEXTUAL -> namedType(
            serializersModule.getContextualDescriptor(descriptor)
                ?: error("No contextual serializer found for ${descriptor.serialName}")
        )
        SerialKind.ENUM -> enum(descriptor)
        StructureKind.CLASS -> messageOfClass(descriptor)
        StructureKind.OBJECT -> messageOfObject(descriptor)
        StructureKind.LIST -> error("Cannot create named type for StructureKind.LIST (${descriptor.serialName})")
        StructureKind.MAP -> error("Cannot create named type for StructureKind.MAP (${descriptor.serialName})")
        is PrimitiveKind -> error("Cannot create named type for PrimitiveKind.* (${descriptor.serialName})")
    }

    private fun TypeContext.messageOfOpenPolymorphicClass(descriptor: SerialDescriptor) =
        messageOfPolymorphicClass(
            descriptor,
            serializersModule.getPolymorphicDescriptors(descriptor).sortedBy { it.serialName }
        )

    private fun TypeContext.messageOfSealedPolymorphicClass(descriptor: SerialDescriptor) =
        messageOfPolymorphicClass(descriptor, descriptor.elementDescriptors.drop(1).first().elementDescriptors)

    private fun TypeContext.messageOfPolymorphicClass(
        descriptor: SerialDescriptor,
        subTypes: Iterable<SerialDescriptor>
    ): FieldEncoding.Reference = putOrGet(descriptor) {
        typeContext {
            val numberIterator = fieldNumberIteratorFromSubTypes(subTypes)

            val fields = subTypes.map { it to fieldForSubType(it, numberIterator) }

            Message(
                Identifier(simpleTypeName(descriptor)),
                listOf(
                    OneOf(
                        Identifier("subtypes"),
                        fields.map { it.second }
                    )
                ),
                encoder = { output, isStandalone ->
                    PolymorphicEncoder(
                        serializersModule,
                        fields.toMap(),
                        isStandalone,
                        output
                    )
                },
                decoder = { values ->
                    PolymorphicDecoder(serializersModule, fields.associateBy { it.second.number }, values)
                }
            )
        }
    }

    private fun fieldForSubType(subDescriptor: SerialDescriptor, numberIterator: FieldNumberIterator): Field {
        val subTypeRef = rootTypes.namedType(subDescriptor)
        val number = FieldNumber(
            subDescriptor.annotations.filterIsInstance<ProtoNumber>()
                .firstOrNull()?.number
                ?: numberIterator.next()
        )
        return Field(
            Identifier(
                subTypeRef.name.value.replaceFirstChar { it.lowercaseChar() }
            ),
            subTypeRef,
            number,
            encoder = valueEncoder(subTypeRef, number),
            decoder = valueDecoder(subTypeRef)
        )
    }

    private fun TypeContext.repeatedField(
        name: Identifier,
        number: FieldNumber,
        descriptor: SerialDescriptor,
        annotations: List<Annotation>,
        syntheticMessageName: Identifier
    ): Field = if (descriptor.isNullable)
            syntheticRepeatedField(name, number, syntheticMessageName, annotations, descriptor)
        else
            when (val kind = descriptor.kind) {
                is PrimitiveKind -> naturalRepeatedField(name, number, scalar(annotations, kind))
                SerialKind.CONTEXTUAL -> repeatedField(
                    name,
                    number,
                    serializersModule.getContextualDescriptor(descriptor)
                        ?: error("No contextual serializer found for ${descriptor.serialName}"),
                    annotations,
                    syntheticMessageName
                )
                StructureKind.LIST -> if (descriptor.actual.getElementDescriptor(0).kind == PrimitiveKind.BYTE)
                    naturalRepeatedField(name, number, FieldEncoding.Bytes)
                else
                    syntheticRepeatedField(name, number, syntheticMessageName, annotations, descriptor)
                StructureKind.MAP -> syntheticRepeatedField(name, number, syntheticMessageName, annotations, descriptor)
                StructureKind.CLASS, StructureKind.OBJECT, SerialKind.ENUM, is PolymorphicKind ->
                    naturalRepeatedField(name, number, rootTypes.namedType(descriptor))
            }


    private fun naturalRepeatedField(
        name: Identifier,
        number: FieldNumber,
        type: FieldEncoding
    ): Field {
        val elementEncoder = { output: WireBuffer ->
            ValueEncoder(
                serializersModule,
                output,
                type,
                number.takeIf { !type.isPackable }
            )
        }
        val elementDecoder = { values: List<WireValue> ->
            ValueDecoder(
                serializersModule,
                values,
                type
            )
        }
        return Field(
            name,
            type,
            number,
            FieldRule.Repeated,
            { ListEncoder(serializersModule, number, type.isPackable, it, elementEncoder) },
            { ListDecoder(serializersModule, type, it, elementDecoder) }
        )
    }

    private fun TypeContext.syntheticRepeatedField(
        name: Identifier,
        number: FieldNumber,
        syntheticMessageName: Identifier,
        annotations: List<Annotation>,
        descriptor: SerialDescriptor
    ): Field {
        lateinit var innerField: Field

        val syntheticType = syntheticMessage(syntheticMessageName) {
            val field = field(
                Identifier("value"),
                FieldNumber(1),
                annotations,
                descriptor
            )

            innerField = field
            field
        }

        val elementEncoder = { output: WireBuffer ->
            SyntheticEncoder(serializersModule, output, innerField, number)
        }

        val elementDecoder = { values: List<WireValue> ->
            SyntheticDecoder(serializersModule, values, innerField)
        }

        return Field(
            name,
            syntheticType,
            number,
            FieldRule.Repeated,
            { ListEncoder(serializersModule, number, false, it, elementEncoder) },
            { ListDecoder(serializersModule, syntheticType, it, elementDecoder) }
        )
    }

    private fun TypeContext.syntheticMessage(
        syntheticMessageName: Identifier,
        field: TypeContext.() -> Field
    ): FieldEncoding.Reference = putOrGet(name = syntheticMessageName.value) {
        typeContext {
            Message(
                syntheticMessageName,
                listOf(field()),
                localTypes,
                encoder = { _, _ -> error("Synthetic messages don't have encoders") },
                decoder = { error("Synthetic messages don't have decoders") }
            )
        }
    }

    private fun TypeContext.field(
        name: Identifier,
        number: FieldNumber,
        annotations: List<Annotation>,
        descriptor: SerialDescriptor
    ): Field =
        when (val kind = descriptor.actual.kind) {
            is PrimitiveKind -> {
                val type = scalar(annotations, kind)
                Field(
                    name,
                    type,
                    number,
                    descriptor.nullableToOptional(),
                    valueEncoder(type, number),
                    valueDecoder(type)
                )
            }
            SerialKind.CONTEXTUAL ->
                field(
                    name,
                    number,
                    annotations,
                    serializersModule.getContextualDescriptor(descriptor)
                        ?: error("No contextual serializer found for ${descriptor.serialName}")
                )
            StructureKind.LIST ->
                if (descriptor.actual.getElementDescriptor(0).kind == PrimitiveKind.BYTE)
                    Field(
                        name,
                        FieldEncoding.Bytes,
                        number,
                        descriptor.nullableToOptional(),
                        valueEncoder(FieldEncoding.Bytes, number),
                        valueDecoder(FieldEncoding.Bytes)
                    )
                else if (descriptor.isNullable) {
                    val field = listField(Identifier("list"), FieldNumber(1), descriptor.actual)
                    Field(
                        name,
                        syntheticMessage(Identifier("${name.value.replaceFirstChar { it.uppercase() }}Value")) {
                            field
                        },
                        number,
                        FieldRule.Optional,
                        { SyntheticEncoder(serializersModule, it, field, number) },
                        { SyntheticDecoder(serializersModule, it, field) }
                    )
                } else
                    listField(name, number, descriptor.actual)
            StructureKind.MAP ->
                if (descriptor.isNullable) {
                    lateinit var field: Field
                    Field(
                        name,
                        syntheticMessage(Identifier("${name.value.replaceFirstChar { it.uppercase() }}Value")) {
                            mapField(Identifier("map"), FieldNumber(1), annotations, descriptor.actual)
                                .also { field = it }
                        },
                        number,
                        FieldRule.Optional,
                        { SyntheticEncoder(serializersModule, it, field, number) },
                        { SyntheticDecoder(serializersModule, it, field) }
                    )
                } else
                    mapField(name, number, annotations, descriptor.actual)
            StructureKind.CLASS, StructureKind.OBJECT, SerialKind.ENUM, is PolymorphicKind -> {
                val type = rootTypes.namedType(descriptor)
                Field(
                    name,
                    type,
                    number,
                    descriptor.nullableToOptional(),
                    valueEncoder(type, number),
                    valueDecoder(type)
                )
            }
        }

    private fun TypeContext.listField(
        name: Identifier,
        number: FieldNumber,
        descriptor: SerialDescriptor
    ): Field {
        val elementDescriptor = descriptor.getElementDescriptor(0).actual
        val elementAnnotations = descriptor.getElementAnnotations(0)
        val syntheticMessageName = Identifier("${name.value.replaceFirstChar { it.uppercase() }}Item")

        return repeatedField(name, number, elementDescriptor, elementAnnotations, syntheticMessageName)
    }

    private fun TypeContext.mapField(
        name: Identifier,
        number: FieldNumber,
        annotations: List<Annotation>,
        descriptor: SerialDescriptor
    ): Field {
        val mapEntryAnnotation = annotations.filterIsInstance<ProtoMapEntry>().firstOrNull() ?: ProtoMapEntry()
        val entryTypeName = "${name.value.replaceFirstChar { it.uppercase() }}Entry"
        lateinit var keyField: Field
        lateinit var valueField: Field
        val entryType = putOrGet(name = entryTypeName) {
            typeContext {
                Message(
                    Identifier(entryTypeName),
                    listOf(
                        field(
                            Identifier(mapEntryAnnotation.keyName),
                            FieldNumber(1),
                            descriptor.getElementAnnotations(0),
                            descriptor.getElementDescriptor(0)
                        ).also { keyField = it },
                        field(
                            Identifier(mapEntryAnnotation.valueName),
                            FieldNumber(2),
                            descriptor.getElementAnnotations(1),
                            descriptor.getElementDescriptor(1)
                        ).also { valueField = it },
                    ),
                    localTypes,
                    encoder = { _, _ -> error("Map entry messages don't have encoders") },
                    decoder = { error("Map entry messages don't have decoders") }
                )
            }
        }
        return Field(
            name,
            entryType,
            number,
            FieldRule.Repeated,
            encoder = { MapEncoder(serializersModule, number, keyField, valueField, it) },
            decoder = { MapDecoder(serializersModule, keyField, valueField, it) }
        )
    }

    private fun scalar(annotations: List<Annotation>, kind: PrimitiveKind): FieldEncoding =
        when (kind) {
            PrimitiveKind.BOOLEAN -> FieldEncoding.Bool
            PrimitiveKind.BYTE -> int32Type(annotations)
            PrimitiveKind.CHAR -> int32Type(annotations)
            PrimitiveKind.DOUBLE -> FieldEncoding.Double
            PrimitiveKind.FLOAT -> FieldEncoding.Float
            PrimitiveKind.INT -> int32Type(annotations)
            PrimitiveKind.LONG -> int64Type(annotations)
            PrimitiveKind.SHORT -> int32Type(annotations)
            PrimitiveKind.STRING -> FieldEncoding.String
        }

    private fun int32Type(annotations: List<Annotation>) = when (integerType(annotations)) {
        IntegerType.Default -> FieldEncoding.Int32
        IntegerType.Unsigned -> FieldEncoding.UInt32
        IntegerType.Signed -> FieldEncoding.SInt32
        IntegerType.Fixed -> FieldEncoding.Fixed32
        IntegerType.SignedFixed -> FieldEncoding.SFixed32
    }

    private fun int64Type(annotations: List<Annotation>) = when (integerType(annotations)) {
        IntegerType.Default -> FieldEncoding.Int64
        IntegerType.Unsigned -> FieldEncoding.UInt64
        IntegerType.Signed -> FieldEncoding.SInt64
        IntegerType.Fixed -> FieldEncoding.Fixed64
        IntegerType.SignedFixed -> FieldEncoding.SFixed64
    }

    private fun integerType(annotations: List<Annotation>) =
        annotations.filterIsInstance<ProtoIntegerType>().firstOrNull()?.type ?: IntegerType.Default

    private fun TypeContext.enum(descriptor: SerialDescriptor): FieldEncoding.Reference = putOrGet(descriptor) {
        val numberIterator = numberIteratorFromEnumElements(descriptor)

        val values = (0 until descriptor.elementsCount).map { index ->
            val annotations = descriptor.getElementAnnotations(index)
            val number =
                if (annotations.filterIsInstance<ProtoDefaultEnumValue>().any())
                    0
                else
                    annotations.filterIsInstance<ProtoNumber>().firstOrNull()?.number
                        ?.also { require(it > 0) { "Number of non-default enum value must be > 0." } }
                        ?: numberIterator.next()
            EnumValue(Identifier(descriptor.getElementName(index)), number)
        }

        Enum(
            Identifier(simpleTypeName(descriptor)),
            values
        )
    }

    private fun TypeContext.messageOfClass(descriptor: SerialDescriptor): FieldEncoding.Reference =
        putOrGet(descriptor) {
            typeContext {
                val numberIterator = fieldNumberIteratorFromClassElements(descriptor)

                val fields = (0 until descriptor.elementsCount).map {
                    val number =
                        descriptor.getElementAnnotations(it).filterIsInstance<ProtoNumber>().firstOrNull()?.number
                            ?: numberIterator.next()
                    val fieldName = descriptor.getElementName(it)
                    field(
                        Identifier(fieldName),
                        FieldNumber(number),
                        descriptor.getElementAnnotations(it),
                        descriptor.getElementDescriptor(it)
                    )
                }

                Message(
                    Identifier(simpleTypeName(descriptor)),
                    fields,
                    localTypes,
                    encoder =
                    { output, isStandalone -> MessageEncoder(serializersModule, fields, isStandalone, output) },
                    decoder = { MessageDecoder(serializersModule, fields, it) }
                )
            }
        }

    private fun TypeContext.messageOfObject(descriptor: SerialDescriptor): FieldEncoding.Reference =
        putOrGet(descriptor) {
            Message(
                Identifier(simpleTypeName(descriptor)),
                encoder =
                    { output, isStandalone -> MessageEncoder(serializersModule, emptyList(), isStandalone, output) },
                decoder = { MessageDecoder(serializersModule, emptyList(), it) }
            )
        }

    @Suppress("RecursivePropertyAccessor")
    private val SerialDescriptor.actual: SerialDescriptor
        get() = if (isInline) elementDescriptors.single().actual else this

    private fun valueEncoder(
        type: FieldEncoding,
        number: FieldNumber
    ): (WireBuffer) -> Encoder = {
        ValueEncoder(serializersModule, it, type, number)
    }

    private fun valueDecoder(type: FieldEncoding?): (List<WireValue>) -> Decoder = {
        ValueDecoder(serializersModule, it, type)
    }
}
