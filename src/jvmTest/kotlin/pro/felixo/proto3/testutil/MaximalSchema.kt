package pro.felixo.proto3.testutil

import pro.felixo.proto3.schema.EnumValue
import pro.felixo.proto3.schema.Enumeration
import pro.felixo.proto3.schema.Field
import pro.felixo.proto3.schema.FieldNumber
import pro.felixo.proto3.schema.FieldRule
import pro.felixo.proto3.schema.FieldType
import pro.felixo.proto3.schema.Identifier
import pro.felixo.proto3.schema.Message
import pro.felixo.proto3.schema.Schema

val MAXIMAL_SCHEMA = Schema(
    setOf(
        Enumeration(
            Identifier("MinimalEnum"),
            setOf(EnumValue(Identifier("Value"), 0))
        ),
        Enumeration(
            Identifier("MaximalEnum"),
            setOf(
                EnumValue(Identifier("A"), 1),
                EnumValue(Identifier("B"), 0),
                EnumValue(Identifier("C"), 2)
            ),
            true,
            setOf(Identifier("x"), Identifier("y")),
            setOf(10..10, 20..30, 40..Int.MAX_VALUE)
        ),
        Message(
            Identifier("EmptyMessage"),
            emptySet()
        ),
        Message(
            Identifier("SmallMessage"),
            setOf(
                Field(
                    Identifier("field"),
                    FieldType.Int32,
                    FieldNumber(1)
                )
            )
        ),
        Message(
            Identifier("MaximalMessage"),
            setOf(
                Field(
                    Identifier("field"),
                    FieldType.Bytes,
                    FieldNumber(1)
                ),
                Field(
                    Identifier("field"),
                    FieldType.SFixed64,
                    FieldNumber(2),
                    FieldRule.Repeated
                ),
                Field(
                    Identifier("field"),
                    FieldType.Double,
                    FieldNumber(3),
                    FieldRule.Optional
                ),
                Field(
                    Identifier("empty"),
                    FieldType.Reference(listOf(Identifier("EmptyMessage"))),
                    FieldNumber(4)
                ),
                Field(
                    Identifier("nested"),
                    FieldType.Reference(listOf(Identifier("NestedMessage"))),
                    FieldNumber(5),
                    FieldRule.Repeated
                ),
                Field(
                    Identifier("enum"),
                    FieldType.Reference(listOf(Identifier("NestedEnum"))),
                    FieldNumber(6)
                ),
            ),
            setOf(
                Message(
                    Identifier("NestedMessage"),
                    setOf(
                        Field(
                            Identifier("field"),
                            FieldType.Float,
                            FieldNumber(1)
                        )
                    )
                ),
                Enumeration(
                    Identifier("NestedEnum"),
                    setOf(
                        EnumValue(Identifier("X"), 0)
                    )
                )
            ),
            setOf(Identifier("x"), Identifier("y")),
            setOf(10..10, 20..30, 40..Int.MAX_VALUE)
        )
    )
)

const val MAXIMAL_SCHEMA_TEXT = """
syntax = "proto3";

message EmptyMessage {
}

enum MaximalEnum {
  option allow_alias = true;
  reserved 10, 20 to 30, 40 to max;
  reserved "x", "y";
  A = 1;
  B = 0;
  C = 2;
}

message MaximalMessage {
  enum NestedEnum {
    X = 0;
  }
  message NestedMessage {
    float field = 1;
  }
  reserved 10, 20 to 30, 40 to 2147483647;
  reserved "x", "y";
  bytes field = 1;
  repeated sfixed64 field = 2;
  optional double field = 3;
  EmptyMessage empty = 4;
  repeated NestedMessage nested = 5;
  NestedEnum enum = 6;
}

enum MinimalEnum {
  Value = 0;
}

message SmallMessage {
  int32 field = 1;
}
"""
