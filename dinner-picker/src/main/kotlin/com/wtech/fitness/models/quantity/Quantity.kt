package com.wtech.fitness.models.quantity

import com.wtech.fitness.models.quantity.decimal.fromDecimal

class Fraction(val numerator: Int, val denominator: Int, private val unit: Unit? = null) {

    private fun Int.gcd(other: Int): Int {
        var gcd = 1

        var i = 1
        while (i <= this && i <= other) {
            // Checks if `i` is factor of both integers
            if (this % i == 0 && other % i == 0)
                gcd = i
            ++i
        }

        return gcd
    }

    operator fun plus(other: Fraction): Quantity {
        if (this == EMPTY_FRACTION) return Quantity(0, other, other.unit)
        else if (other == EMPTY_FRACTION) return Quantity(0, this, unit)

        val a = Fraction(this.numerator * other.denominator, this.denominator * other.denominator, unit)
        val b = Fraction(other.numerator * this.denominator, other.denominator * this.denominator, unit)

        val numer = a.numerator + b.numerator
        val denom = a.denominator
        val gcd = numer.gcd(denom)

        val reducedNumer = numer / gcd
        val reducedDenom = denom / gcd

        val wholeNumber: Int = if (reducedNumer >= reducedDenom) 1 else 0
        val remainderNumer: Int = if (reducedNumer < reducedDenom) reducedNumer else reducedNumer - reducedDenom
        val fraction: Fraction = if (remainderNumer == 0) EMPTY_FRACTION else Fraction(remainderNumer, reducedDenom, unit)

        return Quantity(wholeNumber, fraction, unit)
    }

    override fun toString(): String {
        return "$numerator/$denominator"
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Fraction) return false

        val gcd = numerator.gcd(denominator)
        val otherGcd = other.numerator.gcd(other.denominator)

        val reducedNumer = numerator / gcd
        val reducedDenom = denominator / gcd

        val otherReducedNumer = other.numerator / otherGcd
        val otherReducedDenom = other.denominator / otherGcd

        return reducedNumer == otherReducedNumer && reducedDenom == otherReducedDenom
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
    companion object {
        val EMPTY_FRACTION = Fraction(0, 0, null)
    }
}

class Quantity(rawString: String, val unit: Unit? = null): Comparable<Quantity> {
    private val _rawString: String = rawString

    val wholeNumber: Int = "^\\d+(?!/)".toRegex()
        .find(rawString)
        ?.value
        ?.toInt() ?: 0

    val fraction: Fraction = """\d+\s*/\s*\d+""".toRegex()
        .find(rawString)?.let {
            val numbers = it.value.split("/")
            Fraction(numbers[0].toInt(), numbers[1].toInt(), unit)
        } ?: Fraction.EMPTY_FRACTION


    private val fractionalPart: Double = with (fraction) {
        if (numerator == 0 && denominator == 0) 0.0 else numerator.toDouble() / denominator.toDouble()
    }

    private val value: Double = wholeNumber.toDouble() + fractionalPart

    constructor(whole: Int, fraction: Fraction, unit: Unit? = null): this(asRawString(whole, fraction, unit), unit)
    constructor(whole: Int, unit: Unit?): this(asRawString(whole, Fraction.EMPTY_FRACTION, unit))
    constructor(whole: Int): this(asRawString(whole, Fraction.EMPTY_FRACTION, null))

    override fun toString(): String {
        return _rawString
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Quantity) return false

        val source =
            if ( (this.unit == null && other.unit == null) || !this.unit.canConvertTo(other.unit) )
            { this }
            else { convertTo(other.unit) }

        return source.unit == other.unit &&
                source.wholeNumber == other.wholeNumber &&
                source.fraction == other.fraction
    }

    private fun convertTo(unit: Unit?): Quantity {
        val bothNull = (this.unit == null && unit == null)
        check((this.unit != null && unit != null) || bothNull)
        require(this.unit.canConvertTo(unit) || bothNull)

        val cups: Double = value * getMultiplier(this.unit)
        val conversion: Double = cups * getMultiplier(unit)

        return fromDecimal(conversion, unit)
    }

    private fun getMultiplier(unit: Unit?): Double {
        return 1 / when (unit) {
            Unit.TSP -> 48.0
            Unit.TBSP -> 16.0
            Unit.OUNCE -> 8.0
            Unit.CUP -> 1.0
            Unit.PINT -> 0.5
            Unit.QUART -> 0.25
            Unit.GALLON -> 0.0625
            Unit.MILLILITER -> 250.0
            Unit.LITER -> 0.25
            else -> 1.0
        }
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun compareTo(other: Quantity): Int = compareValuesBy(this, other) { it.value }

    companion object {
        private fun asRawString(whole: Int, fraction: Fraction, unit: Unit?): String {
            val f = if (unit in Unit.containerUnits) Fraction.EMPTY_FRACTION else fraction
            return "$whole${if (f == Fraction.EMPTY_FRACTION) "" else " $f"}"
        }


    }
}

enum class Unit(val abbreviation: String, val representations: List<String>) {
    BUNCH("bunches", listOf("bunches")),
    CAN("can", listOf("can","cans")),
    CARTON("carton", listOf("carton")),
    CUP("cup", listOf("cup", "cups")),
    GALLON("gal", listOf("gallon")),
    JAR("jar", listOf("jar", "jars")),
    LARGE("large", listOf("large")),
    LITER("L", listOf("liter", "L")),
    LOAF("loaf", listOf("loaf")),
    MEDIUM("medium", listOf("medium")),
    MILLILITER("ml", listOf("milliliter")),
    OUNCE("oz", listOf("ounces", "oz")),
    PACKAGE("package", listOf("package")),
    PINT("pt", listOf("pint")),
    POUND("lbs", listOf("lb", "lbs", "pound", "pounds")),
    QUART("qt", listOf("quart")),
    SLICE("slices", listOf("slices")),
    SMALL("small", listOf("small")),
    TSP("tsp", listOf("teaspoon", "teaspoons")),
    TBSP("tbsp", listOf("tablespoon", "tablespoons")),
    TUBE("tube", listOf("tube")),
    WHOLE("whole", listOf("whole")),
    GRAM("g", listOf("gram", "grams", "g")),
    MILLIGRAM("mg", listOf("milligram", "milligrams", "mg")),
    ;

    companion object {

        val volumeUnits: List<Unit> = listOf(
            TSP,
            TBSP,
            OUNCE,
            CUP,
            PINT,
            QUART,
            GALLON,
            MILLILITER,
            LITER,
        )

        val containerUnits: List<Unit> = listOf(
            CAN,
            CARTON,
            JAR,
            PACKAGE,
            TUBE
        )

        fun fromString(s: String): Unit {
            return values().first { s in it.representations }
        }
    }
}

