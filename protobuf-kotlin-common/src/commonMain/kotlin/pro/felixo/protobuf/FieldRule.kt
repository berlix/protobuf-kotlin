package pro.felixo.protobuf

/**
 * Represents a Protobuf field rule:
 *
 * - [Singular] (the default in proto3)
 * - [Optional] ('optional' in proto3 syntax)
 * - [Repeated] ('repeated' in proto3 syntax)
 *
 * Note that the 'map' field rule, which is merely a shorthand for a repeated field whose type is a message representing
 * a key-value pair, is not currently supported.
 */
enum class FieldRule {
    Singular,
    Optional,
    Repeated
}
