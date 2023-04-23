package com.wtech.fitness.models.quantity.decimal

import com.wtech.fitness.models.quantity.Fraction
import com.wtech.fitness.models.quantity.Quantity
import com.wtech.fitness.models.quantity.Unit
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.math.pow

private val Double.decimalParts: Pair<Int, String> get() {
    val parts = toString().split(".")
    return Pair(parts[0].toInt(), parts[1])
}

private val Double.wholePart: Int get() = decimalParts.first

private val Double.fractionalPart: String get() = decimalParts.second

private enum class DecimalType { TERMINATING, REPEATING_SINGLE_NUMBER, ENDS_REPEATING_SINGLE_NUMBER, REPEATING_PATTERN }

private fun getDecimalType(decimal: Double): DecimalType {
    val endsRepeatingSingleNumberPattern = """\d+\.\d+(\d)\1+""".toRegex()
    val repeatedSingleNumberPattern = """\d+\.(\d)\1+""".toRegex()
    val repeatedPatternPattern = """\d+\.(\d+)\1+\d+""".toRegex()

    val d = decimal.toString()
    return when {
        d.matches(repeatedSingleNumberPattern) -> DecimalType.REPEATING_SINGLE_NUMBER
        d.matches(endsRepeatingSingleNumberPattern) -> DecimalType.ENDS_REPEATING_SINGLE_NUMBER
        d.matches(repeatedPatternPattern) -> DecimalType.REPEATING_PATTERN
        else -> DecimalType.TERMINATING
    }
}

private fun forTerminatingDecimal(decimal: Double, unit: Unit?): Quantity {
    // get number of digits after
    val fractionalPart = decimal.toString()
        .substringAfter(".")

    if (fractionalPart.toInt() == 0)
        return Quantity(decimal.toString().substringBefore(".").toInt(), Fraction.EMPTY_FRACTION, unit)

    val power: Int = fractionalPart.length

    val scale: BigDecimal = BigDecimal.TEN.pow(power)
    val denominator: BigInteger = scale.toBigIntegerExact()
    val numerator: BigInteger = (decimal.toBigDecimal() * scale).toBigIntegerExact()

    val gcd = numerator.gcd(denominator)

    val reducedNum = numerator / gcd
    val reducedDenom = denominator / gcd

    val whole = reducedNum / reducedDenom
    val numer = reducedNum - (whole * reducedDenom)

    return Quantity(whole.toInt(), Fraction(numer.toInt(), reducedDenom.toInt()), unit)
}



private fun forRepeatingSingleNumber(decimal: Double, unit: Unit?): Quantity {
    val numerator: Int = decimal.fractionalPart[0].digitToInt()

    return Quantity(
        decimal.wholePart,
        Fraction(numerator, 9),
        unit
    )
}

private fun forEndsRepeatingSingleNumber(decimal: Double, unit: Unit?): Quantity {


    val fractionalPart = decimal.fractionalPart
    val d: Double = "${ fractionalPart[0] }.${ fractionalPart.substring(1) }"
        .toDouble()

    val pattern = """(\d)\1+""".toRegex()
    val repeatingStart = pattern.split(d.toString())[0].substringAfter(".").length + 1

    val a = ".${ d.fractionalPart.substring(0, repeatingStart) }".toDouble()
    val b = ".${decimal.fractionalPart.substring(0, repeatingStart)}".toDouble()
    val rawNumerator: Double = a - b

    val exponent = rawNumerator.fractionalPart.length
    val numerator = rawNumerator * 10.0.pow(exponent)
    val denominator = 9 * 10.0.pow(exponent)



    return Quantity(
        decimal.wholePart,
        Fraction(numerator.toInt(), denominator.toInt()),
        unit
    )
}

private fun forRepeatingPattern(decimal: Double, unit: Unit?): Quantity {
    val whole = decimal.wholePart
    val fraction= decimal.fractionalPart
    val pattern = "(\\d+)\\1+".toRegex().find(fraction)

    val numerator: Int = pattern!!.groupValues.let { it[it.lastIndex] }.toInt()
    val denominator = List(numerator.toString().length) { 9 }.joinToString("").toInt()
    return Quantity(whole, Fraction(numerator, denominator), unit)
}

fun Quantity.Companion.fromDecimal(decimal: Double, unit: Unit?): Quantity {
    return when (getDecimalType(decimal)) {
        DecimalType.TERMINATING -> forTerminatingDecimal(decimal, unit)
        DecimalType.REPEATING_SINGLE_NUMBER -> forRepeatingSingleNumber(decimal, unit)
        DecimalType.ENDS_REPEATING_SINGLE_NUMBER -> forEndsRepeatingSingleNumber(decimal, unit)
        DecimalType.REPEATING_PATTERN -> forRepeatingPattern(decimal, unit)
    }
}