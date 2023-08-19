package pro.felixo.protobuf.schemadocument

import pro.felixo.protobuf.EnumValue
import pro.felixo.protobuf.FieldNumber
import pro.felixo.protobuf.FieldRule
import pro.felixo.protobuf.Identifier

val MAXIMAL_SCHEMA = SchemaDocument(
    listOf(
        Message(
            Identifier("EmptyMessage")
        ),
        Enum(
            Identifier("MaximalEnum"),
            listOf(
                EnumValue(Identifier("A"), 1),
                EnumValue(Identifier("B"), 0),
                EnumValue(Identifier("C"), 2)
            ),
            true,
            listOf(Identifier("x"), Identifier("y")),
            listOf(10..10, 20..30, 40..Int.MAX_VALUE)
        ),
        Message(
            Identifier("MaximalMessage"),
            listOf(
                Field(
                    Identifier("field1"),
                    FieldType.Bytes,
                    FieldNumber(1)
                ),
                Field(
                    Identifier("field2"),
                    FieldType.SFixed64,
                    FieldNumber(2),
                    FieldRule.Repeated
                ),
                Field(
                    Identifier("field3"),
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
            listOf(
                Enum(
                    Identifier("NestedEnum"),
                    listOf(
                        EnumValue(Identifier("X"), 0)
                    )
                ),
                Message(
                    Identifier("NestedMessage"),
                    listOf(
                        Field(
                            Identifier("field"),
                            FieldType.Float,
                            FieldNumber(1)
                        )
                    )
                )
            ),
            listOf(Identifier("x"), Identifier("y")),
            listOf(10..10, 20..30, 40..Int.MAX_VALUE)
        ),
        Enum(
            Identifier("MinimalEnum"),
            listOf(EnumValue(Identifier("Value"), 0))
        ),
        Message(
            Identifier("SmallMessage"),
            listOf(
                Field(
                    Identifier("field"),
                    FieldType.Int32,
                    FieldNumber(1)
                )
            )
        ),

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
  bytes field1 = 1;
  repeated sfixed64 field2 = 2;
  optional double field3 = 3;
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
