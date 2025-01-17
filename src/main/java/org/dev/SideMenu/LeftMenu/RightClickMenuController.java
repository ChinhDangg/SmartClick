package org.dev.SideMenu.LeftMenu;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import org.dev.Enum.AppLevel;
import org.dev.Operation.DataController;

import java.net.URL;
import java.util.ResourceBundle;

public class RightClickMenuController implements Initializable {
    @FXML
    private VBox parentNode;
    @FXML
    private HBox newTaskSection, newActionSection, copySection, pasteSection, deleteSection;

    private Popup rightMenuPopup;
    private DataController currentParentDataController;
    private DataController currentDataController;
    private DataController copiedDataController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        rightMenuPopup = new Popup();
        rightMenuPopup.getContent().add(parentNode);
        rightMenuPopup.setAutoHide(true);
        newActionSection.setOnMouseClicked(this::addNewAction);
        newTaskSection.setOnMouseClicked(this::addNewTask);
        copySection.setOnMouseClicked(this::copyData);
        pasteSection.setOnMouseClicked(this::pasteData);
        deleteSection.setOnMouseClicked(this::deleteData);
    }

    public void showRightMenu(MouseEvent event, DataController dataController, DataController parentDataController) {
        disableAllSelectOption();
        enableNewLevelOption(dataController.getAppLevel());
        enableSelectOption(dataController.getAppLevel());
        rightMenuPopup.show((Node) event.getSource(), event.getScreenX(), event.getScreenY());
        currentDataController = dataController;
        currentParentDataController = parentDataController;
    }

    private void enableNewLevelOption(AppLevel appLevel) {
        if (appLevel == AppLevel.Operation)
            newTaskSection.setDisable(false);
        else if (appLevel == AppLevel.Task) {
            newTaskSection.setDisable(false);
            newActionSection.setDisable(false);
        }
        else if (appLevel == AppLevel.Action)
            newActionSection.setDisable(false);
    }

    private void enableSelectOption(AppLevel appLevel) {
        if (appLevel == AppLevel.Task || appLevel == AppLevel.Action || appLevel == AppLevel.Condition) {
            copySection.setDisable(false);
            deleteSection.setDisable(false);
        }
        if (copiedDataController != null) {
            int order = copiedDataController.getAppLevel().getOrder() - appLevel.getOrder();
            if (order == 0 || order == 1)
                pasteSection.setDisable(false);
        }
    }

    private void disableAllSelectOption() {
        boolean disable = true;
        newTaskSection.setDisable(disable);
        newActionSection.setDisable(disable);
        copySection.setDisable(disable);
        pasteSection.setDisable(disable);
        deleteSection.setDisable(disable);
    }

    private void addNewAction(MouseEvent event) {
        if (currentDataController.getAppLevel() == AppLevel.Action)
            currentParentDataController.addSavedData(null);
        else
            currentDataController.addSavedData(null);
        hideRightMenu();
    }

    private void addNewTask(MouseEvent event) {
        if (currentDataController.getAppLevel() == AppLevel.Task)
            currentParentDataController.addSavedData(null);
        else
            currentDataController.addSavedData(null);
        hideRightMenu();
    }

    private void copyData(MouseEvent event) {
        copiedDataController = currentDataController;
        hideRightMenu();
    }

    private void pasteData(MouseEvent event) {
        if (copiedDataController.getAppLevel().getOrder() - currentDataController.getAppLevel().getOrder() == 0)
            currentParentDataController.addSavedData(copiedDataController.getSavedData());
        else
            currentDataController.addSavedData(copiedDataController.getSavedData());
        hideRightMenu();
    }

    private void deleteData(MouseEvent event) {
        currentParentDataController.removeSavedData(currentDataController);
        if (currentDataController.getAppLevel() == AppLevel.Task)
            currentParentDataController.takeToDisplay();
        hideRightMenu();
    }

    private void hideRightMenu() {
        rightMenuPopup.hide();
    }
}

