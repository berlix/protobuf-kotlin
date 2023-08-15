package pro.felixo.protobuf.serialization.generation.internal

import kotlinx.serialization.descriptors.PrimitiveKind
import pro.felixo.protobuf.serialization.IntegerType
import pro.felixo.protobuf.serialization.ProtoIntegerType
import pro.felixo.protobuf.serialization.encoding.FieldEncoding

fun scalar(annotations: List<Annotation>, kind: PrimitiveKind): FieldEncoding =
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

fun int32Type(annotations: List<Annotation>): FieldEncoding.Integer32 = when (integerType(annotations)) {
    IntegerType.Default -> FieldEncoding.Int32
    IntegerType.Unsigned -> FieldEncoding.UInt32
    IntegerType.Signed -> FieldEncoding.SInt32
    IntegerType.Fixed -> FieldEncoding.Fixed32
    IntegerType.SignedFixed -> FieldEncoding.SFixed32
}

fun int64Type(annotations: List<Annotation>): FieldEncoding.Integer64 = when (integerType(annotations)) {
    IntegerType.Default -> FieldEncoding.Int64
    IntegerType.Unsigned -> FieldEncoding.UInt64
    IntegerType.Signed -> FieldEncoding.SInt64
    IntegerType.Fixed -> FieldEncoding.Fixed64
    IntegerType.SignedFixed -> FieldEncoding.SFixed64
}

fun integerType(annotations: List<Annotation>): IntegerType =
    annotations.filterIsInstance<ProtoIntegerType>().firstOrNull()?.type ?: IntegerType.Default
