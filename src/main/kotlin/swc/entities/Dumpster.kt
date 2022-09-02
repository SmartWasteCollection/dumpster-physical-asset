package swc.entities

import java.util.UUID

data class Dumpster(
    val id: String = "Dumpster-${UUID.randomUUID()}",
    val dumpsterType: DumpsterType,
    var isOpen: Boolean = false,
    var isWorking: Boolean = true,
    var occupiedVolume: Volume = Volume(),
) {
    companion object {
        const val CAPACITY_THRESHOLD = 95
        const val TIMEOUT_MS: Long = 30000

        fun from(capacity: Double, wasteName: WasteName) = Dumpster(dumpsterType = DumpsterType.from(capacity, wasteName))
        fun from(id: String, capacity: Double, wasteName: WasteName) = Dumpster(id = id, dumpsterType = DumpsterType.from(capacity, wasteName))
    }

    fun isAvailable(): Boolean = isWorking && occupiedVolume.getOccupiedPercentage(dumpsterType.size.capacity) < CAPACITY_THRESHOLD
}
