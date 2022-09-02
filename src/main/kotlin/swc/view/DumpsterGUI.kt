package swc.view

import com.azure.digitaltwins.core.implementation.models.ErrorResponseException
import swc.azure.AzureDTManager
import swc.entities.Dumpster
import swc.entities.Volume
import java.awt.*
import java.util.concurrent.Executors
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
    private var dumpster: Dumpster? = null
    private var panel: JComponent? = null

    private var isOpenComboBox: JComboBox<Boolean>? = null
    private var isWorkingComboBox: JComboBox<Boolean>? = null
    private var occupiedVolumeSpinner: JSpinner? = null

    init {
        layout = BorderLayout()
        val dumpsterId = JOptionPane.showInputDialog(frame,"Insert the Dumpster's ID","Dumpster ID", JOptionPane.PLAIN_MESSAGE) as String

        try {
            dumpster = AzureDTManager.getDumpsterById(dumpsterId)
            showDigitalTwin()
            // Update DIGITAL TWIN
            updateDigitalTwin()
            updatePhysicalAsset()
        } catch (_: ErrorResponseException) {
            panel = CustomTextArea("Dumpster Not Found")
        }

        add(panel!!, BorderLayout.CENTER)
    }

    private fun updateDigitalTwin() = Executors.newSingleThreadExecutor().execute {
        while(true){
            val isWorking = isWorkingComboBox?.selectedItem.toString().toBoolean()
            val isOpen = isOpenComboBox?.selectedItem.toString().toBoolean()
            val occupiedVolume = Volume(occupiedVolumeSpinner?.value.toString().toDouble())
            if (dumpster?.isWorking != isWorking || dumpster?.isOpen != isOpen || dumpster?.occupiedVolume != occupiedVolume) {
                dumpster?.isWorking = isWorking
                dumpster?.isOpen = isOpen
                dumpster?.occupiedVolume = occupiedVolume
                AzureDTManager.updateDigitalTwin(dumpster!!)
            }
            Thread.sleep(500)
        }
    }

    private fun updatePhysicalAsset() = Executors.newSingleThreadExecutor().execute {
        while(true){
            dumpster = AzureDTManager.getDumpsterById(dumpster!!.id)
            isWorkingComboBox?.selectedItem = dumpster?.isWorking
            isOpenComboBox?.selectedItem = dumpster?.isOpen
            occupiedVolumeSpinner?.value = dumpster?.occupiedVolume?.value
            Thread.sleep(500)
        }
    }

    private fun showDigitalTwin() {
        val dtPanel = JPanel()
        val vbox = Box.createVerticalBox()
        val title = CustomTextArea("Dumpster Physical Asset")
        title.componentOrientation = ComponentOrientation.RIGHT_TO_LEFT
        vbox.add(title)
        vbox.add(CustomTextArea("ID: ${dumpster?.id}"))
        vbox.add(CustomTextArea("Size: ${dumpster?.dumpsterType?.size?.dimension}"))
        vbox.add(CustomTextArea("Capacity (liters): ${dumpster?.dumpsterType?.size?.capacity}"))
        vbox.add(CustomTextArea("Waste Name: ${dumpster?.dumpsterType?.typeOfOrdinaryWaste?.wasteName}"))

        isOpenComboBox = CustomComboBox(dumpster!!.isOpen)
        vbox.add(CustomFormElement("isOpen:", isOpenComboBox!!))

        isWorkingComboBox = CustomComboBox(dumpster!!.isWorking)
        vbox.add(CustomFormElement("isWorking:", isWorkingComboBox!!))

        occupiedVolumeSpinner = JSpinner(SpinnerNumberModel(dumpster?.occupiedVolume?.value, 0.0, dumpster?.dumpsterType?.size?.capacity, 0.1))
        occupiedVolumeSpinner?.minimumSize = CustomDimension()
        occupiedVolumeSpinner?.preferredSize = CustomDimension()
        occupiedVolumeSpinner?.maximumSize = CustomDimension()
        vbox.add(CustomFormElement("Occupied Volume (liters):", occupiedVolumeSpinner!!))

        dtPanel.layout = BoxLayout(dtPanel, BoxLayout.Y_AXIS)
        dtPanel.add(vbox)
        panel = dtPanel
    }
}