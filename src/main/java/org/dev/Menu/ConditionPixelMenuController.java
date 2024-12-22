package org.dev.Menu;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import org.dev.AppScene;
import org.dev.Enum.LogLevel;
import org.dev.Operation.Condition.Condition;
import org.dev.Operation.Condition.PixelCondition;
import org.dev.Operation.ConditionController;
import org.dev.Enum.ReadingCondition;
import org.dev.Operation.ActivityController;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ResourceBundle;

public class ConditionPixelMenuController extends OptionsMenuController implements Initializable {
    @FXML
    private Pane pixelMenuPane;
    @FXML
    private CheckBox showHideLineCheckBox, blackWhiteLineCheckBox;
    @FXML
    private CheckBox notOptionCheckBox, requiredOptionCheckBox, globalSearchCheckBox;

    private ConditionController conditionController;
    private final String className = this.getClass().getSimpleName();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        loadPixelTypeChoiceBox();
    }
    private void loadPixelTypeChoiceBox() {
        showHideLineCheckBox.setOnAction(this::showHideBoxAction);
        blackWhiteLineCheckBox.setOnAction(this::changePixelLineColor);
        AppScene.addLog(LogLevel.TRACE, className, "Pixel menu pixel type choice loaded");
    }

    // ------------------------------------------------------
    @Override
    public void loadMenu(ActivityController activityController) {
        if (activityController == null) {
            AppScene.addLog(LogLevel.WARN, className, "Condition controller is not set - loadMenu");
            return;
        }
        this.conditionController = (ConditionController) activityController;
        Condition condition = conditionController.getCondition();
        if (condition != null && condition.getChosenReadingCondition() == ReadingCondition.Pixel) {
            PixelCondition pixelCondition = (PixelCondition) conditionController.getCondition();
            currentMainImage = pixelCondition.getMainImage();
            displayMainImageView(pixelCondition.getDisplayImage());
            mainImageBoundingBox = pixelCondition.getMainImageBoundingBox();
            currentDisplayImage = pixelCondition.getDisplayImage();
            notOptionCheckBox.setSelected(pixelCondition.isNot());
            requiredOptionCheckBox.setSelected(pixelCondition.isRequired());
            imageWidth = currentMainImage.getWidth();
            imageHeight = currentMainImage.getHeight();
            outsideBoxWidth = (currentDisplayImage.getHeight() - imageHeight)/2;
            AppScene.addLog(LogLevel.TRACE, className, "Loaded preset reading pixel");
        }
        else
            resetPixelMenu();
        GlobalScreen.addNativeKeyListener(this);
        showMenu(true);
    }
    private void resetPixelMenu() {
        resetMenu();
        notOptionCheckBox.setSelected(false);
        requiredOptionCheckBox.setSelected(true);
        showHideLineCheckBox.setSelected(true);
        blackWhiteLineCheckBox.setSelected(true);
        AppScene.addLog(LogLevel.TRACE, className, "Menu reset");
    }
    public void showMenu(boolean show) {
        visible = show;
        pixelMenuPane.setVisible(visible);
        AppScene.addLog(LogLevel.TRACE, className, "Menu showed: " + visible);
    }

    // ------------------------------------------------------
    @Override
    protected void save(MouseEvent event) {
        if (conditionController == null) {
            AppScene.addLog(LogLevel.WARN, className, "Condition controller is not set - save");
            return;
        }
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on save button");
        if (currentMainImage == null) {
            AppScene.addLog(LogLevel.WARN, className, "Reading pixel condition is not set - save failed");
            return;
        }
        conditionController.registerReadingCondition(new PixelCondition(ReadingCondition.Pixel, currentMainImage,
                mainImageBoundingBox, notOptionCheckBox.isSelected(), requiredOptionCheckBox.isSelected(),
                currentDisplayImage, globalSearchCheckBox.isSelected()));
        AppScene.addLog(LogLevel.INFO, className, "Pixel - saved");
    }
    @Override
    protected void backToPreviousMenu(MouseEvent event) {
        if (visible) {
            stopAllListeners();
            showMenu(false);
            AppScene.conditionMenuController.loadMenu(conditionController);
            AppScene.addLog(LogLevel.DEBUG, className, "Backed to main condition menu");
        }
    }

    // ------------------------------------------------------
    private BufferedImage getDisplayImageForReadingPixel(int x, int y) throws AWTException {
        mainImageBoundingBox = new Rectangle(x, y, imageWidth, imageHeight);
        currentMainImage = captureCurrentScreen(mainImageBoundingBox);
        AppScene.addLog(LogLevel.TRACE, className, "Current screen is captured");
        BufferedImage box = (showHideLineCheckBox.isSelected()) ? drawBox(imageWidth, imageHeight, getPixelColor()) : null;
        BufferedImage imageWithEdges = (box == null) ? getImageWithEdges(currentMainImage, x, y, 1.0f) :
                getImageWithEdges(box, x, y, 1.0f);
        currentDisplayImage = (imageWithEdges == null) ? currentMainImage : imageWithEdges;
        BufferedImage zoomedImage = getZoomedImage(imageWithEdges);
        if (zoomedImage != null)
            return zoomedImage;
        else if (imageWithEdges != null)
            return imageWithEdges;
        return (box == null) ? currentMainImage : box;
    }
    public BufferedImage drawBox(int width, int height, Color color) {
        BufferedImage temp = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = temp.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, width, height);
        g.dispose();
        return temp;
    }

    // ------------------------------------------------------
    private void showHideBoxAction(javafx.event.ActionEvent event) {
        if (mouseStopped) {
            Graphics2D g = currentDisplayImage.createGraphics();
            if (showHideLineCheckBox.isSelected())
                g.drawImage(drawBox(imageWidth, imageHeight, getPixelColor()),
                        outsideBoxWidth, outsideBoxWidth, imageWidth, imageHeight, null);
            else
                g.drawImage(currentMainImage, outsideBoxWidth, outsideBoxWidth, imageWidth, imageHeight, null);
            g.dispose();
            if (currentZoomValue != 1.00)
                displayMainImageView(getScaledImage(currentDisplayImage, currentZoomValue));
            else
                displayMainImageView(currentDisplayImage);
        }
    }
    private void changePixelLineColor(javafx.event.ActionEvent event) {
        if (mouseStopped) {
            showHideLineCheckBox.setSelected(true);
            AppScene.addLog(LogLevel.DEBUG, className, "Change pixel line color");
            Graphics2D g = currentDisplayImage.createGraphics();
            g.drawImage(drawBox(imageWidth, imageHeight, getPixelColor()),
                    outsideBoxWidth, outsideBoxWidth, imageWidth, imageHeight, null);
            g.dispose();
            if (currentZoomValue != 1.00)
                displayMainImageView(getScaledImage(currentDisplayImage, currentZoomValue));
            else
                displayMainImageView(currentDisplayImage);
        }
    }
    private Color getPixelColor() {
        Color color = blackWhiteLineCheckBox.isSelected() ? Color.BLACK : Color.WHITE;
        AppScene.addLog(LogLevel.DEBUG, className, "New pixel color box: " + color.toString());
        return color;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == mouseTimer) {
            Point p = MouseInfo.getPointerInfo().getLocation();
            if (p.equals(previousMousePoint)) {
                return;
            }
            previousMousePoint = p;
            try {
                displayMainImageView(getDisplayImageForReadingPixel(p.x, p.y));
            } catch (Exception ex) {
                AppScene.addLog(LogLevel.ERROR, className, "Error at displaying captured image at mouse pointer");
            }
        }
    }

    public void nativeKeyReleased(NativeKeyEvent e) {
        AppScene.addLog(LogLevel.TRACE, className, "Key Released: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
        if (e.getKeyCode() == NativeKeyEvent.VC_F2) {
            AppScene.addLog(LogLevel.DEBUG, className, "Clicked on F2 key to start mouse listening");
            startMouseMotionListening();
        }
        else if (e.getKeyCode() == NativeKeyEvent.VC_F1) {
            AppScene.addLog(LogLevel.DEBUG, className, "Clicked on F1 key to stop mouse listening");
            stopMouseMotionListening();
        }
    }
}