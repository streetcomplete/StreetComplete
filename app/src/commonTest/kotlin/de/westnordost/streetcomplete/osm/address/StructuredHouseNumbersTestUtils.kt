package de.westnordost.streetcomplete.osm.address

fun numbers(vararg part: StructuredHouseNumbersPart) =
    StructuredHouseNumbers(part.toList())

fun range(start: StructuredHouseNumber, end: StructuredHouseNumber) =
    StructuredHouseNumbersPart.Range(start, end)

fun single(s: StructuredHouseNumber) =
    StructuredHouseNumbersPart.Single(s)

fun simple(number: Int) =
    StructuredHouseNumber.Simple(number)

fun withLetter(number: Int, letter: String, separator: String = "") =
    StructuredHouseNumber.WithLetter(number, separator, letter)

fun withNumber(number: Int, number2: Int, separator: String = "/") =
    StructuredHouseNumber.WithNumber(number, separator, number2)
