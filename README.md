# Protocol Buffers 3 support for Kotlin Multiplatform

[![Release](https://img.shields.io/github/v/release/berlix/protobuf-kotlin.svg)](https://github.com/berlix/protobuf-kotlin/releases)

[API documentation](https://berlix.github.io/protobuf-kotlin/apidocs/latest/)

A comprehensive Protobuf 3 library for Kotlin Multiplatform, offering features ranging from
serialization/deserialization with `kotlinx.serialization` to .proto document handling capabilities
and Protoscope language support.

Currently, the library supports JVM and JS platforms. Support for Native and WASM platforms will be looked into in
future versions.

Note that this library is still in its early stages, and the API is subject to change in any 0.x version.

## Table of contents
- [Usage](#usage)
    - [Serialization/deserialization](#serializationdeserialization)
    - [Schema documents](#schema-documents)
    - [Protoscope](#protoscope)
- [Missing features and roadmap](#missing-features-and-roadmap)
- [Contributing](#contributing)
- [License](#license)
- [Contact](#contact)

## Usage

You can obtain the required libraries from Maven Central. In Gradle projects, add this to you `build.gradle.kts` file:

```kotlin
repositories {
    mavenCentral()
}
```

See the examples below for which artifacts you need to include for which use case. For non-multiplatform projects,
you have to suffix the artifact name with the compilation target, i.e. `-jvm` or `-js`.

### Serialization/deserialization

Include the following dependency in your `build.gradle.kts` file:

```kotlin
dependencies {
    implementation("pro.felixo:protobuf-kotlin-serialization:<version>")
}
```

To serialize and deserialize Protobuf, create an `EncodingSchema` with the `SerialDescriptor`s of the messages you want
it to support, and use it to encode/decode messages:

```kotlin
import pro.felixo.protobuf.serialization.generation.encodingSchema

@Serializable
data class Person(val name: String, val age: Int)

val schema = encodingSchema(listOf(Person.serializer().descriptor))

val bytes = schema.encodeToByteArray(Person("Catalina", 2))
val person = schema.decodeFromByteArray<Person>(bytes)
```

#### Encoding parameters

In `encodingSchema`, specify a `SerializersModule` to support open polymorphic serialization or contextual
serialization. In this case, you also need to specify which types from the `SerializersModule` to include in the schema.

For compatibility especially with Protobuf 2, you can pass `encodeZeroValues = true` to `encodingSchema`. This controls
whether fields that have their Protobuf default values (zero/empty) are encoded or omitted.

#### Nullable and optional fields

This library is completely agnostic of the optionality of fields in terms of `kotlinx.serialization`:
fields that are _nullable_ in Kotlin will be made _optional_ in Protobuf, and the default values to use when decoding
a message with absent fields are dictated by the Protobuf spec (zero for numbers, empty for length-delimited fields).
This means that in order to avoid unexpected behaviour, in your serializable classes, only nullable fields should have
default values, and that default value should be `null`.

#### Customizing messages

The schema that is generated from Kotlin types can be customized using annotations:

```kotlin
@Serializable
data class Person(
    @ProtoNumber(3) // field number: 3
    @SerialName("fullName") // field name: "fullName"
    val name: String,
  
    @ProtoNumber(1) // field number: 1
    @ProtoIntegerType(IntegerType.Fixed) // use fixed32 rather than the default, int32
    val age: Int,
  
    @ProtoListItem(
        integerType = IntegerType.Signed, // use sint64 rather than the default, int64

        // Since this is a list of a nullable type, a synthetic message is generated to represent the list elements.
        // The following parameters control the schema of that synthetic message.
        messageName = "FavoriteNumber",
        fieldName = "number",
        fieldNumber = 5
    )
    val favoriteNumbers: List<Long?>, // will be auto-assigned the field number: 2

    // For maps, the schema of the synthetic message that represents the map entries can be customized as follows.
    @ProtoMapEntry(
      messageName = "PetEntry",
      keyName = "name",
      keyNumber = 3,
      keyIntegerType = IntegerType.Fixed, // map key is not an integer, so this is ignored
      valueName = "pet",
      valueNumber = 4,
      valueIntegerType = IntegerType.Signed // map value is not an integer, so this is ignored
    )
    val pets: Map<String, Pet>, // will be auto-assigned the field number: 4

    val species: Species // will be auto-assigned the field number: 5
)

@Serializable
enum class Species {
    @ProtoDefaultEnumValue
    Unknown,

    @ProtoNumber(1)
    Human,

    @ProtoNumber(2)
    Gorn
}
```

For more usage examples, see the
[serialization integration tests](protobuf-kotlin-serialization/src/commonTest/kotlin/pro/felixo/protobuf/serialization/integrationtests/).

### Schema documents

The module `protobuf-kotlin-schemadocument` provides support for `.proto` schema documents. It can be used to read
and write `.proto` documents, and to compare them for equality or equivalence. It also provides some support for
schema validation.

To parse a schema document, use the `SchemaDocumentReader` class:

```kotlin
import pro.felixo.protobuf.schemadocument.SchemaDocumentReader

val schemaDocument = SchemaDocumentReader().readSchema("...schema text...")
```

To generate a document from an encoding schema, use the `toSchemaDocument` extension function:

```kotlin
import pro.felixo.protobuf.serialization.generation.encodingSchema
import pro.felixo.protobuf.serialization.toSchemaDocument

@Serializable
data class Person(val name: String, val age: Int)

val schema = encodingSchema(listOf(Person.serializer().descriptor))
val schemaDocumentForEncodingSchema = encodingSchema.toSchemaDocument()
```

To compare two schema documents for equality, use the `==` operator. Note that
`SchemaDocument` does have a notion of the order of declarations, so two equivalent documents may be considered unequal
because the orders of their declarations differ:

```kotlin
val equal = schemaDocument == schemaDocumentForEncodingSchema
```

In order to compare two schema documents for equivalence, which is useful for testing purposes, use the `areEquivalent`
function:

```kotlin
import pro.felixo.protobuf.schemadocument.areEquivalent

val equivalent = areEquivalent(schemaDocument, schemaDocumentForEncodingSchema)

```

To validate a document against the Protobuf schema validation rules, use the `validate` function:

```kotlin
import pro.felixo.protobuf.schemadocument.validatation.validate

val validationResult: ValidationResult = validate(schemaDocument)
```

## Protoscope

The `protobuf-kotlin-protoscope` module implements
[the Protoscope language](https://github.com/protocolbuffers/protoscope), which allows for the
textual representation of the protobuf wire format and is useful for testing purposes.

Protoscope code can be tokenized and converted to its binary representation. Note that the reverse is not currently
supported.

```kotlin
import pro.felixo.protobuf.protoscope.ProtoscopeConverter

val bytes = ProtoscopeConverter().convert("""1: {"Catalina"} 2: 2 """)

// bytes now can be compared to the serialized form of a Person message, to assert its correctness
```

## Missing features and roadmap

Major future roadmap items are:

- **Performance:** The library has been written with performance in mind, but not a lot of work has gone specifically 
  into optimisation, and no benchmarks have been conducted yet, so there is probably room for improvement. Specifically,
  there's a good chance that the amount of heap allocations can be reduced, and that the encoding/decoding of byte
  arrays can be made much faster. 
- **Expanded multiplatform support:** Artifacts for other platforms, such as Native and WASM.
- **Protoscope:** Support the conversion of bytes to Protoscope.
- **Schema documents:** Support complete language syntax.

Major features that are not included and may or may not be implemented in the future are:
- **Framework-agnostic encoding layer:** The message-level encoding/decoding logic is partly conflated with
  code specific to `kotlinx.serialization`. In order to support other frameworks, this could be separated.
- **Canonical JSON encoding:** There is no support for the
  [canonical JSON encoding](https://protobuf.dev/programming-guides/proto3/#json).
- **Protobuf text format:** The [Protobuf text format](https://protobuf.dev/reference/protobuf/textformat-spec/)
  is not supported. The Protoscope language probably a better alternative for testing purposes, so the Protobuf text
  format will only be implemented if important use cases come up.
- **Kotlin multiplatform protoc compiler plugin:** As an alternative to `kotlinx.serialization`, a `protoc` compiler
  plugin could be written that generates Kotlin multiplatform code.
- **Protobuf 2 support:** The library is currently only compatible with Protobuf 3. Support for
  [Protobuf 2](https://protobuf.dev/programming-guides/proto2/) is not
  planned, but may be implemented if there is demand for it.
- **gRPC:** The library does not implement [gRPC](https://grpc.io/), even though the schema document format contains
  syntax for describing gRPC services.
- **Well-known types:** The library does not currently implement the
  [well-known types](https://protobuf.dev/reference/protobuf/google.protobuf/), nor does it treat them specially in
  any way when they are encountered in a schema document.

## Contributing

All contributions, whether issues raised or pull requests submitted, are appreciated. If you report a bug, please
consider including a unit test that demonstrates it, even if you aren't providing a fix.

## License

This project is licensed under the [MIT License](LICENSE).

## Contact

For any questions, issues, or discussions, feel free to:
- Email: [pub@felix-engelhardt.de](mailto:pub@felix-engelhardt.de)
- Raise an issue on the project's GitHub repository.
