package swc.view

import swc.azure.AzureAuthentication
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*

object SwingGUI {
    class DumpsterManagerWindow(title: String) : JFrame() {

        init {
            createUI(title)
        }

        private fun createUI(title: String) {
            setTitle(title)
            defaultCloseOperation = EXIT_ON_CLOSE
            this.addWindowListener(object: WindowAdapter(){
                override fun windowClosing(e: WindowEvent?) {
                    AzureAuthentication.queueProcessor.close()
                    super.windowClosing(e)
                }
            })
            setSize(800, 400)
            setLocationRelativeTo(null)
            // adding buttons to the frame
            add(DumpsterGUI(this))
        }

    }

    fun createAndShowGUI() = DumpsterManagerWindow("Dumpster Manager Window").also { it.isVisible = true }
}
