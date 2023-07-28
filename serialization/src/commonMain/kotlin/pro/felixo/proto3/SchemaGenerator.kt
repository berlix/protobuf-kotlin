package pro.felixo.proto3

import pro.felixo.proto3.internal.FieldNumberIterator
import pro.felixo.proto3.internal.NumberIterator
import pro.felixo.proto3.internal.nullableToOptional
import pro.felixo.proto3.internal.simpleTypeName
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
import pro.felixo.proto3.encoding.CompositeEncoding
import pro.felixo.proto3.encoding.EnumEncoding
import pro.felixo.proto3.encoding.ListDecoder
import pro.felixo.proto3.encoding.ListEncoder
import pro.felixo.proto3.encoding.MapDecoder
import pro.felixo.proto3.encoding.MapEncoder
import pro.felixo.proto3.encoding.MessageDecoder
import pro.felixo.proto3.encoding.MessageEncoder
import pro.felixo.proto3.encoding.PolymorphicDecoder
import pro.felixo.proto3.encoding.PolymorphicEncoder
import pro.felixo.proto3.encoding.SyntheticDecoder
import pro.felixo.proto3.encoding.SyntheticEncoder
import pro.felixo.proto3.encoding.ValueDecoder
import pro.felixo.proto3.encoding.ValueEncoder
import pro.felixo.proto3.internal.TypeContext
import pro.felixo.proto3.internal.fullTypeName
import pro.felixo.proto3.internal.typeContext
import pro.felixo.proto3.schema.EnumValue
import pro.felixo.proto3.schema.Enumeration
import pro.felixo.proto3.schema.Field
import pro.felixo.proto3.schema.FieldNumber
import pro.felixo.proto3.schema.FieldRule
import pro.felixo.proto3.schema.Identifier
import pro.felixo.proto3.schema.Message
import pro.felixo.proto3.schema.OneOf
import pro.felixo.proto3.schema.Schema
import pro.felixo.proto3.wire.WireOutput
import pro.felixo.proto3.wire.WireValue
import kotlin.reflect.KType

@OptIn(ExperimentalSerializationApi::class)
class SchemaGenerator(
    val serializersModule: SerializersModule = EmptySerializersModule()
) {
    private val rootTypes = TypeContext()

    private val enumEncodings = mutableMapOf<String, EnumEncoding>()
    private val compositeEncodings = mutableMapOf<String, CompositeEncoding>()

    fun schema(): Schema = Schema(rootTypes.localTypes.toSet())

    fun add(descriptor: SerialDescriptor) {
        rootTypes.namedType(descriptor)
    }

    fun addFromSerializersModule(type: KType) {
        rootTypes.namedType(
            serializersModule.serializerOrNull(type)?.descriptor ?: error("No serializer in module found for $type")
        )
    }

    fun getCompositeEncoding(descriptor: SerialDescriptor): CompositeEncoding {
        val name = fullTypeName(descriptor)
        return compositeEncodings[name] ?: run {
            add(descriptor)
            compositeEncodings[name] ?: error("Not a composite: ${descriptor.serialName}")
        }
    }

    fun getEnumEncoding(descriptor: SerialDescriptor): EnumEncoding {
        val name = fullTypeName(descriptor)
        return enumEncodings[name] ?: run {
            add(descriptor)
            enumEncodings[name] ?: error("Not an enum: ${descriptor.serialName}")
        }
    }

    private fun TypeContext.namedType(descriptor: SerialDescriptor): FieldType.Reference = when (descriptor.kind) {
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
    ): FieldType.Reference = putOrGet(descriptor) {
        typeContext {
            val numberIterator = FieldNumberIterator.fromSubTypes(subTypes)

            val fields = subTypes.map { it to fieldForSubType(it, numberIterator) }

            compositeEncodings[fullTypeName(descriptor)] = CompositeEncoding(
                { output, isStandalone ->
                    PolymorphicEncoder(
                        this@SchemaGenerator,
                        fields.toMap(),
                        isStandalone,
                        output
                    )
                },
                { PolymorphicDecoder(this@SchemaGenerator, fields.associateBy { it.second.number }, it) }
            )

            Message(
                Identifier(simpleTypeName(descriptor)),
                setOf(
                    OneOf(
                        Identifier("subtypes"),
                        fields.map { it.second }.toSet()
                    )
                )
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
                subTypeRef.components.last().value.replaceFirstChar { it.lowercaseChar() }
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
                    naturalRepeatedField(name, number, FieldType.Bytes)
                else
                    syntheticRepeatedField(name, number, syntheticMessageName, annotations, descriptor)
                StructureKind.MAP -> syntheticRepeatedField(name, number, syntheticMessageName, annotations, descriptor)
                StructureKind.CLASS, StructureKind.OBJECT, SerialKind.ENUM, is PolymorphicKind ->
                    naturalRepeatedField(name, number, rootTypes.namedType(descriptor))
            }


    private fun naturalRepeatedField(
        name: Identifier,
        number: FieldNumber,
        type: FieldType
    ): Field {
        val elementEncoder = { output: WireOutput ->
            ValueEncoder(
                this@SchemaGenerator,
                output,
                type,
                number.takeIf { !type.isPackable }
            )
        }
        val elementDecoder = { values: List<WireValue> ->
            ValueDecoder(
                this@SchemaGenerator,
                values,
                type
            )
        }
        return Field(
            name,
            type,
            number,
            FieldRule.Repeated,
            { ListEncoder(this@SchemaGenerator, number, type.isPackable, it, elementEncoder) },
            { ListDecoder(this@SchemaGenerator, type, it, elementDecoder) }
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

            // TODO remove and put directly in field instead?
            compositeEncodings[fullTypeName(descriptor)] = CompositeEncoding(
                { output, isStandalone -> MessageEncoder(this@SchemaGenerator, listOf(field), isStandalone, output) },
                { MessageDecoder(this@SchemaGenerator, listOf(field), it) }
            )

            innerField = field
            field
        }

        val elementEncoder = { output: WireOutput ->
            SyntheticEncoder(this@SchemaGenerator, output, innerField, number)
        }

        val elementDecoder = { values: List<WireValue> ->
            SyntheticDecoder(this@SchemaGenerator, values, innerField)
        }

        return Field(
            name,
            syntheticType,
            number,
            FieldRule.Repeated,
            { ListEncoder(this@SchemaGenerator, number, false, it, elementEncoder) },
            { ListDecoder(this@SchemaGenerator, syntheticType, it, elementDecoder) }
        )
    }

    private fun TypeContext.syntheticMessage(
        syntheticMessageName: Identifier,
        field: TypeContext.() -> Field
    ): FieldType.Reference = putOrGet(name = syntheticMessageName.value) {
        typeContext {
            Message(
                syntheticMessageName,
                setOf(field()),
                localTypes
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
                        FieldType.Bytes,
                        number,
                        descriptor.nullableToOptional(),
                        valueEncoder(FieldType.Bytes, number),
                        valueDecoder(FieldType.Bytes)
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
                        { SyntheticEncoder(this@SchemaGenerator, it, field, number) },
                        { SyntheticDecoder(this@SchemaGenerator, it, field) }
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
                        { SyntheticEncoder(this@SchemaGenerator, it, field, number) },
                        { SyntheticDecoder(this@SchemaGenerator, it, field) }
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
                    setOf(
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
                    localTypes
                )
            }
        }
        return Field(
            name,
            entryType,
            number,
            FieldRule.Repeated,
            encoder = { MapEncoder(this@SchemaGenerator, number, keyField, valueField, it) },
            decoder = { MapDecoder(this@SchemaGenerator, keyField, valueField, it) }
        )
    }

    private fun scalar(annotations: List<Annotation>, kind: PrimitiveKind): FieldType =
        when (kind) {
            PrimitiveKind.BOOLEAN -> FieldType.Bool
            PrimitiveKind.BYTE -> int32Type(annotations)
            PrimitiveKind.CHAR -> int32Type(annotations)
            PrimitiveKind.DOUBLE -> FieldType.Double
            PrimitiveKind.FLOAT -> FieldType.Float
            PrimitiveKind.INT -> int32Type(annotations)
            PrimitiveKind.LONG -> int64Type(annotations)
            PrimitiveKind.SHORT -> int32Type(annotations)
            PrimitiveKind.STRING -> FieldType.String
        }

    private fun int32Type(annotations: List<Annotation>) = when (integerType(annotations)) {
        IntegerType.Default -> FieldType.Int32
        IntegerType.Unsigned -> FieldType.UInt32
        IntegerType.Signed -> FieldType.SInt32
        IntegerType.Fixed -> FieldType.Fixed32
        IntegerType.SignedFixed -> FieldType.SFixed32
    }

    private fun int64Type(annotations: List<Annotation>) = when (integerType(annotations)) {
        IntegerType.Default -> FieldType.Int64
        IntegerType.Unsigned -> FieldType.UInt64
        IntegerType.Signed -> FieldType.SInt64
        IntegerType.Fixed -> FieldType.Fixed64
        IntegerType.SignedFixed -> FieldType.SFixed64
    }

    private fun integerType(annotations: List<Annotation>) =
        annotations.filterIsInstance<ProtoIntegerType>().firstOrNull()?.type ?: IntegerType.Default

    private fun TypeContext.enum(descriptor: SerialDescriptor): FieldType.Reference = putOrGet(descriptor) {
        val numberIterator = NumberIterator.fromEnumElements(descriptor)

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

        enumEncodings[fullTypeName(descriptor)] = EnumEncoding(values.map { it.number })

        Enumeration(
            Identifier(simpleTypeName(descriptor)),
            values.toSet()
        )
    }

    private fun TypeContext.messageOfClass(descriptor: SerialDescriptor): FieldType.Reference = putOrGet(descriptor) {
        typeContext {
            val numberIterator = FieldNumberIterator.fromClassElements(descriptor)

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

            compositeEncodings[fullTypeName(descriptor)] = CompositeEncoding(
                { output, isStandalone -> MessageEncoder(this@SchemaGenerator, fields, isStandalone, output) },
                { MessageDecoder(this@SchemaGenerator, fields, it) }
            )

            Message(Identifier(simpleTypeName(descriptor)), fields.toSet(), localTypes)
        }
    }

    private fun TypeContext.messageOfObject(descriptor: SerialDescriptor): FieldType.Reference = putOrGet(descriptor) {
        compositeEncodings[fullTypeName(descriptor)] = CompositeEncoding(
            { output, isStandalone -> MessageEncoder(this@SchemaGenerator, emptyList(), isStandalone, output) },
            { MessageDecoder(this@SchemaGenerator, emptyList(), it) }
        )

        Message(Identifier(simpleTypeName(descriptor)), emptySet())
    }

    private val SerialDescriptor.actual: SerialDescriptor
        get() = if (isInline) elementDescriptors.single().actual else this

    private fun valueEncoder(
        type: FieldType,
        number: FieldNumber
    ): (WireOutput) -> Encoder = {
        ValueEncoder(this, it, type, number)
    }

    private fun valueDecoder(type: FieldType?): (List<WireValue>) -> Decoder = {
        ValueDecoder(this, it, type)
    }
}
