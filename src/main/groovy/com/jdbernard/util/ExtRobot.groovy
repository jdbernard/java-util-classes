package com.jdbernard.util

import static com.jdbernard.util.Util.*
import static java.awt.event.KeyEvent.*
import java.awt.Robot

public class ExtRobot {

    @Delegate Robot robot

    int delayTime = 100

    public ExtRobot() { robot = new Robot() }

    public void pressAndRelease(int keyCode) {
        robot.with {
            keyPress(keyCode)
            keyRelease(keyCode)
            delay(delayTime)
        }
    }

    public void pressCombination(int... keyCodes) {
        keyCodes.each { keyCode -> robot.keyPress(keyCode) }
        robot.delay(delayTime)
        (keyCodes as List).reverse().each { keyCode -> robot.keyRelease(keyCode) }
    }

    /**
     * Currently only tested with alphanumeric!
     */
    public void type(String text) {
        text.each { letter ->
            if (letter.charAt(0).isUpperCase())
                robot.keyPress(VK_SHIFT)

            pressAndRelease((int) letter.toUpperCase().charAt(0))
            
            if (letter.charAt(0).isUpperCase())
                robot.keyRelease(VK_SHIFT)
        }
    }

    public String selectAllAndCopy() {
            robot.with {
                keyPress(VK_CONTROL)
                pressAndRelease(VK_A)
                pressAndRelease(VK_C)
                keyRelease(VK_CONTROL)

                delay(2 * delayTime)
            }

            return readClipboardText()
    }

    public void selectAllAndPaste(String text) {
        copyToClipboard(text)

        robot.with {
            keyPress(VK_CONTROL)
            pressAndRelease(VK_A)
            pressAndRelease(VK_V)
            keyRelease(VK_CONTROL)

            delay(2 * delayTime)
        }

    }

}
