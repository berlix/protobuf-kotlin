package pro.felixo.proto3.protoscope

val PROTOSCOPE_SPECIFICATION = """# Quoted strings.

"Quoted strings are delimited by double quotes. Backslash denotes escape
sequences. Legal escape sequences are: \\ \" \x00 \000 \n. \x00 consumes two
hex digits and emits a byte. \000 consumes one to three octal digits and emits
a byte (rejecting values that do not fit in a single octet). Otherwise, any
byte before the closing quote, including a newline, is emitted as-is."

# Tokens in the file are emitted one after another, so the following lines
# produce the same output:
"hello world"
"hello " "world"

# The Protobuf wire format only deals in UTF-8 when it deals with text at all,
# so there is no equivalent of DER-ASCII's UTF-16/32 string literals.


# Hex literals.

# Backticks denote hex literals. Either uppercase or lowercase is legal, but no
# characters other than hexadecimal digits may appear. A hex literal emits the
# decoded byte string.
`00`
`abcdef`
`AbCdEf`


# Integers.

# Tokens which match /-?[0-9]+/ or /-?0x[0-9a-fA-F]+/ are integer tokens.
# They encode into a Protobuf varint (base 128).
456
-0xffFF

# Signed integers encode as their 64-bit two's complement by default. If an
# integer is suffixed with z, it uses the zigzag encoding instead.
-2z 3  # Equivalent tokens.

# An integer may instead by suffixed with i32 or i64, which indicates it should
# be encoded as a fixed-width integer.
0i32
-23i64

# An integer may follow a 'long-form:N' token. This will cause the varint to
# have N more bytes than it needs to successfully encode. For example, the
# following are equivalent:
long-form:3 3
`83808000`


# Booleans.

# The following tokens emit `01` and `00`, respectively.
true
false


# Floats.

# Tokens that match /-?[0-9]+\.[0-9]+([eE]-?[0-9]+)?/ or
# /-?0x[0-9a-fA-F]+\.[0-9a-fA-F]+([pP]-?[0-9]+)?/ are floating-point
# tokens. They encode to a IEEE 754 binary64 value.
1.0
9.423e-2
-0x1.ffp52

# Decimal floats are only guaranteed a particular encoding when conversion from
# decimal to binary is exact. Hex floats always have an exact conversion. The
# i32 prefix from above may be used to specify a 32-bit float (i64 is permitted,
# but redundant).
1.5i32
0xf.fi64

# The strings inf32, inf64, -inf32, and -inf64 are recognized as shorthands for
# 32-bit and 64-bit infinities. There is no shorthand for NaN (since there are 
# so many of them), and it is best spelled out as a fixed-size hex int.
inf32
-inf64


# Tag expressions.

# An integer followed by a : denotes a tag expression. This encodes the tag of
# a Protobuf field. This is identical to an ordinary integer, except that a
# wire type between 0 and 7 is prepended via the expression 
#
#  tag := int << 3 | wireType
#
# The integer specifies the field number, while what follows after the :
# specifies the field type. In the examples below, no whitespace may be
# present around the : rune.
#
# Field numbers may be hex, signed, and zigzag per the syntax above, but not
# fixed-width. They may have a long-form prefix.

1:VARINT  # A varint.
2:I64     # A fixed-width, 64-bit blob.
3:LEN     # A length-prefixed blob.
4:SGROUP  # A start-group marker.
5:EGROUP  # An end-group marker.
6:I32     # A fixed-width, 32-bit blob.

0x10:0  # Also a varint, explicit value for the type.
8:6     # Invalid wire type (6 and 7 are unused).

# This is an error: the wire type must be between 0 and 7.
# 9:8

# If the : is instead followed by any rune not matching /[\w-]/, the scanner
# will seek forward to the next token. If it is a fixed-width integer or a
# float, the wire type will be inferred to be I32 or I64 as appropriate; if it
# is a {, or a 'long-form:N' followed by a {, the type is inferred as LEN;
# if it is a '!', the type is inferred as SGROUP; otherwise, it defaults to
# VARINT.

1: 55z
2: 1.23
3: {"text"}
6: -1i32
8: !{42}


# Length prefixes.

# Matching curly brace tokens denote length prefixes. They emit a varint-encoded
# length prefix followed by the encoding of the brace contents.
#
# It may optionally be preceded by 'long-form:N', as an integer would, to
# introduce redundant bytes in the encoding of the length prefix.

# This is a string field. Note that 23:'s type is inferred to be LEN.
23: {"my cool string"}

# This is a nested message field.
24: {
  1: 5
  2: {"nested string"}
}

# This is a packed repeated int field.
25: { 1 2 3 4 5 6 7 }

# This string field's length prefix will be 3, rather than one, bytes.
23: long-form:2 {"non-minimally-prefixed"}


# Groups

# If matching curly braces are prefixed with a ! (no spaces before the first
# {), it denotes a group. Encoding a group requires a field number, so the !{}
# must come immediately before a tag expression without an explicit type (which
# will be inferred to be SGROUP). The closing brace will generate a
# corresponding EGROUP-typed tag to match the SGROUP tag.
26: !{
  1: 55z
  2: 1.4
  3: {"abcd"}
}

# long-form:N may be the last token between a group's braces, which will be
# applied to the EGROUP tag.
27: !{long-form:3}


# Examples.

# These primitives may be combined with raw byte strings to produce other
# encodings.

# This is another way to write a message, using an explicit length
2:LEN 4
  "abcd"

# This allows us to insert the wrong length.
2:LEN 5
  "abcd"

# The wrong wire type can be used with no consequences.
5:I64 "stuff"
"""

/**
 * Generated from [PROTOSCOPE_SPECIFICATION] using:
 *
 * ```
 * git clone git@github.com:protocolbuffers/protoscope.git
 * cd protoscope
 * cat language.txt | go run cmd/protoscope/main.go -s | hexdump -ve '1/1 "%.2x"'
 * ```
 */
val PROTOSCOPE_SPECIFICATION_HEX = """
    51756f74656420737472696e6773206172652064656c696d6974656420627920646f75626c652071756f7465732e204261636b736c
    6173682064656e6f746573206573636170650a73657175656e6365732e204c6567616c206573636170652073657175656e63657320
    6172653a205c202220002000200a2e200020636f6e73756d65732074776f0a6865782064696769747320616e6420656d6974732061
    20627974652e200020636f6e73756d6573206f6e6520746f207468726565206f6374616c2064696769747320616e6420656d697473
    0a612062797465202872656a656374696e672076616c756573207468617420646f206e6f742066697420696e20612073696e676c65
    206f63746574292e204f74686572776973652c20616e790a62797465206265666f72652074686520636c6f73696e672071756f7465
    2c20696e636c7564696e672061206e65776c696e652c20697320656d69747465642061732d69732e68656c6c6f20776f726c646865
    6c6c6f20776f726c6400abcdefabcdefc8038180fcffffffffffff01030300000000e9ffffffffffffff8380800083808000010000
    0000000000f03f1d554d10751fb83f0000000000f03fc30000c03f0000000000e02f400000807f000000000000f0ff08111a232c35
    800146086e11ae47e17a14aef33f1a047465787435ffffffff432a44ba010e6d7920636f6f6c20737472696e67c201110805120d6e
    657374656420737472696e67ca010701020304050607ba019680006e6f6e2d6d696e696d616c6c792d7072656669786564d301086e
    11666666666666f63f1a0461626364d401db01dc81808000120461626364120561626364297374756666
    """
