package com.wtech.fitness.models.quantity

operator fun Int.plus(quantity: Quantity): Quantity {
    return Quantity(this + quantity.wholeNumber, quantity.fraction, quantity.unit)
}

private fun Quantity?.checkCanPerformOperationWith(other: Quantity?) {
    val canConvertUnits: Boolean = this?.unit?.canConvertTo(other?.unit)
        ?: (this?.unit == null && other?.unit == null)
    val canContainUnits: Boolean = this?.unit in Unit.containerUnits || other?.unit in Unit.containerUnits
    require(canConvertUnits || canContainUnits) {
        "Cannot add ingredients of types ${this?.unit} ${other?.unit}"
    }
}

operator fun Quantity?.plus(other: Quantity?): Quantity {

    checkCanPerformOperationWith(other)

    val wholeSum: Int = (this?.wholeNumber ?: 0) + (other?.wholeNumber ?: 0)

    val fractionSum: Quantity = (this?.fraction ?: Fraction.EMPTY_FRACTION) + (other?.fraction ?: Fraction.EMPTY_FRACTION)
    val sum = wholeSum + fractionSum
    return Quantity(sum.wholeNumber, sum.fraction, this?.unit)
}

operator fun Quantity?.plus(fraction: Fraction): Quantity {
    checkCanPerformOperationWith(Quantity(0, fraction, this?.unit))

    val fractionSum = fraction + ( this?.fraction ?: Fraction.EMPTY_FRACTION )
    return ( this?.wholeNumber ?: 0 ) + fractionSum
}

fun Unit?.canConvertTo(other: Unit?): Boolean {
    return (this != null && this == other) ||
            (this in Unit.volumeUnits && other in Unit.volumeUnits)
}

fun Unit.Companion.getThreshold(unit: Unit): Quantity {
    check(unit in containerUnits)

    return when (unit) {
        Unit.CAN -> Quantity(1, Fraction(1, 2), Unit.CUP)
        else -> error("No volume threshold for unit $unit")
    }
}

fun Unit.Companion.checkThreshold(threshold: Quantity, current: Quantity): Quantity {

    var test = Quantity(0, Fraction.EMPTY_FRACTION, threshold.unit)
    while (test < current) {
        test += threshold
    }
    return test
}

fun String.extractQuantity(): Quantity {
    val number = "\\d+(\\s*/\\s*\\d+)?"
    val quantityPattern: Regex = """($number\s+to\s+$number)|($number)|($number\s+(/\s+$number)?)""".toRegex()
    val rawString: String = quantityPattern.find(this)?.value ?: error("Cannot extract quantity from string: $this")
    val unit: Unit? = try {
        extractUnit()
    } catch (e: IllegalStateException) {
        null
    }
    return Quantity(rawString.replace('-', ' '), unit)
}

private fun String.extractUnitString(): String {
    val errorMsg = "Cannot extract unit from string: $this"

    val unitPattern: Regex = "(${Unit.values().flatMap { it.representations }.joinToString("|")})(e*s)?".toRegex()
    val quantityPattern: Regex = """(\d\s+to\s+\d)|(\d+/\d+)|(\d+(\s+|-)\d+(/\d+)?)|\d+""".toRegex()

    val quantityString: String = quantityPattern.find(this)?.value ?: ""
    val searchSpace: String = substringAfter(quantityString).trim().split(" ")[0]
    return unitPattern.find(searchSpace)?.value ?: error(errorMsg)
}

fun String.extractUnit(): Unit {
    val rawString: String = extractUnitString()
    return Unit.fromString(rawString)
}

fun String.extractGrocery(): String {
    val stringWithoutQuantity: String =  try {
        substringAfter(extractQuantity().toString())
    } catch (e: IllegalStateException) {
        this
    }

    return try {
        substringAfter(extractUnitString()).substringAfter("of")
    } catch (e: IllegalStateException) {
        stringWithoutQuantity
    }
        .trim()
}

