package org.dev.Task.Action;

import java.awt.AWTException;
import java.awt.Robot;

public class ActionKeyPressMouseClick extends Action {
    @Override
    public void performAction() {
        try {
            Robot robot = new Robot();
            performKeyPressAndMouseClick(robot, keyCode);
        } catch (AWTException e) {
            System.out.println("Error with action perform key press mouse click");
        }
    }

    protected void performKeyPressAndMouseClick(Robot robot, int eventKey) throws AWTException {
        performKeyPress(robot, eventKey);
        performMouseClick(mainImageBoundingBox);
        robot.delay(70 + (int) (Math.random() * 50));
        robot.keyRelease(eventKey);
    }
}
