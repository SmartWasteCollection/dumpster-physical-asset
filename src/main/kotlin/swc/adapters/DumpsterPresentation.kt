package swc.adapters

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import swc.azure.AzureConstants
import swc.entities.Dumpster
import swc.entities.DumpsterType
import swc.entities.Size
import swc.entities.TypeOfOrdinaryWaste
import swc.entities.Volume
import swc.entities.WasteName

object DumpsterSerialization {
    fun DumpsterType.toJson(): JsonObject {
        val obj = JsonObject()
        obj.add(DumpsterPropertyNames.SIZE, this.size.toJson())
        obj.add(DumpsterPropertyNames.TYPE_OF_ORDINARY_WASTE, this.typeOfOrdinaryWaste.toJson())
        return obj
    }

    fun Size.toJson(): JsonObject {
        val obj = JsonObject()
        obj.addProperty(DumpsterPropertyNames.DIMENSION, this.dimension.toString())
        obj.addProperty(DumpsterPropertyNames.CAPACITY, this.capacity)
        return obj
    }

    fun TypeOfOrdinaryWaste.toJson(): JsonObject {
        val obj = JsonObject()
        obj.addProperty(DumpsterPropertyNames.WASTE_NAME, this.wasteName.toString())
        obj.addProperty(DumpsterPropertyNames.COLOR, this.wasteColor.toString())
        return obj
    }

    fun Dumpster.toJson(): JsonObject {
        val metadata = JsonObject()
        metadata.addProperty(DigitalTwinPropertyNames.MODEL, AzureConstants.DUMPSTER_DT_MODEL_ID)

        val obj = JsonObject()
        obj.addProperty(DigitalTwinPropertyNames.DTID, this.id)
        obj.add(DigitalTwinPropertyNames.METADATA, metadata)
        obj.add(DumpsterPropertyNames.DUMPSTER_TYPE, this.dumpsterType.toJson())
        obj.addProperty(DumpsterPropertyNames.OPEN, this.isOpen)
        obj.addProperty(DumpsterPropertyNames.WORKING, this.isWorking)
        obj.addProperty(DumpsterPropertyNames.OCCUPIED_VOLUME, this.occupiedVolume.value)
        return obj
    }
}

object DumpsterDeserialization {
    fun JsonObject.toDumpster() = Dumpster(
        (this["id"] ?: this[DigitalTwinPropertyNames.DTID]).asString,
        this.getAsJsonObject(DumpsterPropertyNames.DUMPSTER_TYPE).toDumpsterType(),
        this[DumpsterPropertyNames.OPEN].asBoolean,
        this[DumpsterPropertyNames.WORKING].asBoolean,
        this.toVolume()
    )

    fun parse(json: String): JsonObject = JsonParser().parse(json).asJsonObject

    private fun JsonObject.toSize() = Size.from(
        this.getAsJsonPrimitive(DumpsterPropertyNames.CAPACITY)
            .asDouble
    )

    private fun JsonObject.toWasteName() = WasteName.valueOf(
        this.getAsJsonObject(DumpsterPropertyNames.TYPE_OF_ORDINARY_WASTE)
            .getAsJsonPrimitive(DumpsterPropertyNames.WASTE_NAME)
            .asString
    )

    private fun JsonObject.toVolume() = Volume(
        when (val volume = this[DumpsterPropertyNames.OCCUPIED_VOLUME]) {
            is JsonObject -> volume["value"]
            else -> volume
        }.asDouble
    )

    private fun JsonObject.toDumpsterType() = DumpsterType.from(
        this.getAsJsonObject(DumpsterPropertyNames.SIZE).toSize().capacity,
        this.toWasteName()
    )
}
