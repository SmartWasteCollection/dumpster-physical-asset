package swc.view

import com.azure.digitaltwins.core.implementation.models.ErrorResponseException
import com.google.gson.Gson
import com.google.gson.JsonObject
import swc.azure.AzureAuthentication
import swc.azure.AzureDTManager
import swc.entities.Dumpster
import java.awt.*
import java.awt.event.ItemEvent
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.util.UUID
import javax.swing.*

class CustomTextArea(content: String): JTextPane() {
    init {
        text = content
        isOpaque = true
        isEditable = false
        isFocusable = false
        font = Font("Courier", Font.BOLD, 20)
    }
}

class CustomFormElement(title: String, component: JComponent): JPanel() {
    init {
        add(CustomTextArea(title))
        add(component)
        layout = BoxLayout(this, BoxLayout.X_AXIS)
    }
}

class CustomComboBox(defaultValue: Boolean): JComboBox<Boolean>(arrayOf(true, false)) {
    init {
        selectedItem = defaultValue
        minimumSize = CustomDimension()
        preferredSize = CustomDimension()
        maximumSize = CustomDimension()
    }
}

class CustomDimension: Dimension(300, 30)

class DumpsterGUI(frame: JFrame): JPanel() {
    private lateinit var dumpster: Dumpster
    private lateinit var panel: JComponent

    private lateinit var isOpenComboBox: JComboBox<Boolean>
    private lateinit var isWorkingComboBox: JComboBox<Boolean>
    private lateinit var occupiedVolumeSpinner: JSpinner

    private val processor = AzureAuthentication.queueProcessorClient {
        // Consume message only if it's related to this dumpster's ID
        when (it.message.applicationProperties["cloudEvents:subject"]) {
            dumpster.id -> {
                val patch = Gson()
                    .fromJson(it.message.body.toString(), JsonObject::class.java)["patch"]
                    .asJsonArray.first().asJsonObject
                val value = patch["value"]
                println(patch)
                when (patch["path"].asString) {
                    "/occupiedVolume" -> occupiedVolumeSpinner.value = value.toString().toDouble()
                    "/open" -> isOpenComboBox.selectedItem = value.toString().toBoolean()
                    "/working" -> isWorkingComboBox.selectedItem = value.toString().toBoolean()
                }
                it.complete()
            }
            else -> it.abandon()
        }
    }

    init {
        layout = BorderLayout()
        val dumpsterId = JOptionPane.showInputDialog(frame,"Insert the Dumpster's ID","Dumpster ID", JOptionPane.PLAIN_MESSAGE) as String

        try {
            dumpster = AzureDTManager.getDumpsterById(dumpsterId)
            showDigitalTwin()
        } catch (_: ErrorResponseException) {
            panel = CustomTextArea("Dumpster Not Found")
        }

        add(panel, BorderLayout.CENTER)

        processor.start()
    }

    private fun showDigitalTwin() {
        val dtPanel = JPanel()
        val vbox = Box.createVerticalBox()
        val title = CustomTextArea("Dumpster Physical Asset")
        title.componentOrientation = ComponentOrientation.RIGHT_TO_LEFT
        vbox.add(title)
        vbox.add(CustomTextArea("ID: ${dumpster.id}"))
        vbox.add(CustomTextArea("Size: ${dumpster.dumpsterType.size.dimension}"))
        vbox.add(CustomTextArea("Capacity (liters): ${dumpster.dumpsterType.size.capacity}"))
        vbox.add(CustomTextArea("Waste Name: ${dumpster.dumpsterType.typeOfOrdinaryWaste.wasteName}"))

        isOpenComboBox = CustomComboBox(dumpster.isOpen)
        isOpenComboBox.addItemListener {
            if (it.stateChange == ItemEvent.SELECTED) {
                AzureDTManager.updateOpenProperty(dumpster.id, it.item.toString().toBoolean())
            }
        }
        vbox.add(CustomFormElement("isOpen:", isOpenComboBox))

        isWorkingComboBox = CustomComboBox(dumpster.isWorking)
        isWorkingComboBox.addItemListener {
            if (it.stateChange == ItemEvent.SELECTED) {
                val isWorking = it.item.toString().toBoolean()
                AzureDTManager.updateWorkingProperty(dumpster.id, isWorking)
                if (!isWorking) sendComplaint()
            }
        }
        vbox.add(CustomFormElement("isWorking:", isWorkingComboBox))

        occupiedVolumeSpinner = JSpinner(SpinnerNumberModel(dumpster.occupiedVolume.value, 0.0, dumpster.dumpsterType.size.capacity, 1))
        occupiedVolumeSpinner.value = dumpster.occupiedVolume.value
        occupiedVolumeSpinner.minimumSize = CustomDimension()
        occupiedVolumeSpinner.preferredSize = CustomDimension()
        occupiedVolumeSpinner.maximumSize = CustomDimension()
        occupiedVolumeSpinner.addChangeListener {e ->
            val occupiedVolume = (e.source as JSpinner).value.toString().toDouble()
            println(occupiedVolume)
            AzureDTManager.updateOccupiedVolumeProperty(dumpster.id, occupiedVolume)
            checkAvailability(occupiedVolume)
        }
        vbox.add(CustomFormElement("Occupied Volume (liters):", occupiedVolumeSpinner))

        dtPanel.layout = BoxLayout(dtPanel, BoxLayout.Y_AXIS)
        dtPanel.add(vbox)
        panel = dtPanel
    }

    private fun checkAvailability(occupiedVolume: Double) {
        if (occupiedVolume > (dumpster.dumpsterType.size.capacity * 0.75)) {
            val client = HttpClient.newBuilder().build()
            val request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:3004/missions/ordinary"))
                .POST(BodyPublishers.ofString(dumpster.id))
                .build()
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            println("Mission Created! Id: ${response.body()}.")
        }
    }

    private fun sendComplaint() {
        val client = HttpClient.newBuilder().build()
        val jsonObject = JsonObject()
        jsonObject.addProperty("id", UUID.randomUUID().toString())
        jsonObject.addProperty("ownerId", dumpster.id)
        jsonObject.addProperty("title", "A dumpster stopped working.")
        jsonObject.addProperty("issuer", "DUMPSTER")
        jsonObject.addProperty("message", "Dumpster with id '${dumpster.id}' is not working.")
        jsonObject.addProperty("status", "OPEN")

        println(jsonObject.toString())
        val request = HttpRequest.newBuilder()
            .header("Content-Type", "application/json")
            .uri(URI.create("http://localhost:3003/complaints"))
            .POST(BodyPublishers.ofString(jsonObject.toString()))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        println("Complaint Created! Id: ${response.body()}.")
    }
}