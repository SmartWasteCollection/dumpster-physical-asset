package swc.view

import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.GridLayout
import javax.swing.*

object SwingGUI {
    class DumpsterManagerWindow(title: String) : JFrame() {

        init {
            createUI(title)
        }

        private fun createUI(title: String) {
            setTitle(title)
            defaultCloseOperation = EXIT_ON_CLOSE
            setSize(800, 400)
            setLocationRelativeTo(null)
            // adding buttons to the frame
            add(DumpsterGUI(this))
        }

    }

    fun createAndShowGUI() = DumpsterManagerWindow("Dumpster Manager Window").also { it.isVisible = true }
}
