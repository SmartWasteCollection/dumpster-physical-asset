package swc.azure

import com.azure.core.models.JsonPatchDocument
import swc.adapters.DumpsterDeserialization.parse
import swc.adapters.DumpsterDeserialization.toDumpster
import swc.entities.Dumpster

object AzureDTManager {
    fun getDumpsterById(id: String) =
        parse(AzureAuthentication.authClient.getDigitalTwin(id, String::class.java)).toDumpster()

    fun updateOccupiedVolumeProperty(dumpsterId: String, newVolume: Double) =
        AzureAuthentication.authClient.updateDigitalTwin(dumpsterId,
            JsonPatchDocument().appendReplace("/occupiedVolume", newVolume))

    fun updateOpenProperty(dumpsterId: String, isOpen: Boolean) =
        AzureAuthentication.authClient.updateDigitalTwin(dumpsterId,
            JsonPatchDocument().appendReplace("/open", isOpen))

    fun updateWorkingProperty(dumpsterId: String, isWorking: Boolean) =
        AzureAuthentication.authClient.updateDigitalTwin(dumpsterId,
            JsonPatchDocument().appendReplace("/working", isWorking))

}
