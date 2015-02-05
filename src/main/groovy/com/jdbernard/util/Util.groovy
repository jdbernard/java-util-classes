package com.jdbernard.util

import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable

public class Util {

    static Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard()

    public static String readClipboardText() {
        Transferable contents = clipboard.getContents(null)

        if (contents != null &&
            contents.isDataFlavorSupported(DataFlavor.stringFlavor))
            return (String)contents.getTransferData(DataFlavor.stringFlavor)

        else return null
    }

    public static void copyToClipboard(String text) {
        StringSelection data = new StringSelection(text)
        clipboard.setContents(data, data)
    }


}
