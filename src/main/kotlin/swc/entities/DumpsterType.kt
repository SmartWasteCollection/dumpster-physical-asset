package swc.entities

class DumpsterType private constructor(
    val size: Size,
    val typeOfOrdinaryWaste: TypeOfOrdinaryWaste,
) {
    companion object {
        fun from(capacity: Double, wasteName: WasteName) =
            DumpsterType(Size.from(capacity), TypeOfOrdinaryWaste.from(wasteName))
    }

    override fun toString(): String = "DumpsterType(size = $size, typeOfOrdinaryWaste = $typeOfOrdinaryWaste)"

    override fun equals(other: Any?) = (other is DumpsterType) &&
        size == other.size && typeOfOrdinaryWaste == other.typeOfOrdinaryWaste

    override fun hashCode() = 31 * size.hashCode() + typeOfOrdinaryWaste.hashCode()
}

class Size private constructor(
    val dimension: Dimension,
    val capacity: Double,
) {
    companion object {
        private const val BIG_DUMPSTER_THRESHOLD = 300.0

        fun from(capacity: Double): Size {
            require(capacity > 0.0) { "A Size capacity must be a positive value" }
            return when {
                capacity > BIG_DUMPSTER_THRESHOLD -> Size(Dimension.LARGE, capacity)
                else -> Size(Dimension.SMALL, capacity)
            }
        }
    }

    override fun toString(): String = "Size(dimension = $dimension, capacity = $capacity)"

    override fun equals(other: Any?) = (other is Size) &&
        dimension == other.dimension && capacity == other.capacity

    override fun hashCode() = 31 * dimension.hashCode() + capacity.hashCode()
}

class TypeOfOrdinaryWaste private constructor(
    val wasteName: WasteName,
    val wasteColor: WasteColor,
) {
    companion object {
        fun from(wasteName: WasteName) = when (wasteName) {
            WasteName.UNSORTED -> TypeOfOrdinaryWaste(wasteName, WasteColor.GRAY)
            WasteName.PLASTIC_ALUMINIUM -> TypeOfOrdinaryWaste(wasteName, WasteColor.YELLOW)
            WasteName.ORGANIC -> TypeOfOrdinaryWaste(wasteName, WasteColor.BROWN)
            WasteName.GLASS -> TypeOfOrdinaryWaste(wasteName, WasteColor.GREEN)
            else -> TypeOfOrdinaryWaste(wasteName, WasteColor.BLUE)
        }
    }

    override fun toString(): String = "TypeOfOrdinaryWaste(wasteName = $wasteName, wasteColor = $wasteColor)"

    override fun equals(other: Any?) = (other is TypeOfOrdinaryWaste) &&
        wasteName == other.wasteName && wasteColor == other.wasteColor

    override fun hashCode() = 31 * wasteColor.hashCode() + wasteName.hashCode()
}

data class Volume(
    val value: Double = 0.0
) {
    init {
        require(value >= 0.0) { "A Volume value can not be a negative number" }
    }

    fun getOccupiedPercentage(capacity: Double) = (value * 100) / capacity
}

enum class WasteName {
    UNSORTED, PLASTIC_ALUMINIUM, ORGANIC, GLASS, PAPER
}

enum class WasteColor {
    GRAY, YELLOW, BLUE, GREEN, BROWN
}

enum class Dimension {
    SMALL, LARGE
}
