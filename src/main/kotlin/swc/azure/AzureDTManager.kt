package swc.azure

import com.azure.core.models.JsonPatchDocument
import swc.adapters.DumpsterDeserialization.parse
import swc.adapters.DumpsterDeserialization.toDumpster
import swc.entities.Dumpster

object AzureDTManager {
    fun getDumpsterById(id: String) =
        parse(AzureAuthentication.authClient.getDigitalTwin(id, String::class.java)).toDumpster()

    fun updateDigitalTwin(dumpster: Dumpster) {
        val updateTwinData = JsonPatchDocument()
        updateTwinData.appendReplace("/occupiedVolume", dumpster.occupiedVolume.value)
        updateTwinData.appendReplace("/working", dumpster.isWorking)
        updateTwinData.appendReplace("/open", dumpster.isOpen)

        AzureAuthentication.authClient.updateDigitalTwin(dumpster.id, updateTwinData)
    }

}
