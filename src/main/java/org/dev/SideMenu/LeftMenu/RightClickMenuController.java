package org.dev.SideMenu.LeftMenu;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import org.dev.AppScene;
import org.dev.Enum.AppLevel;
import org.dev.JobController.JobDataController;
import org.dev.JobStructure;

import java.net.URL;
import java.util.ResourceBundle;

public class RightClickMenuController implements Initializable {
    @FXML
    private VBox parentNode;
    @FXML
    private HBox newTaskGroupSection, newTaskSection, newActionSection;
    @FXML
    private HBox runSection;
    @FXML
    private HBox copySection, pasteSection, deleteSection;
    @FXML
    private HBox moveUpSection, moveDownSection;

    private Popup rightMenuPopup;
    private JobStructure currentJobStructure;
    private JobDataController copiedJobDataController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        rightMenuPopup = new Popup();
        rightMenuPopup.getContent().add(parentNode);
        rightMenuPopup.setAutoHide(true);
        newActionSection.setOnMouseClicked(this::addNewAction);
        newTaskSection.setOnMouseClicked(this::addNewTask);
        newTaskGroupSection.setOnMouseClicked(this::addNewTaskGroup);
        runSection.setOnMouseClicked(this::runJob);
        copySection.setOnMouseClicked(this::copyData);
        pasteSection.setOnMouseClicked(this::pasteData);
        deleteSection.setOnMouseClicked(this::deleteData);
        moveUpSection.setOnMouseClicked(this::moveUp);
        moveDownSection.setOnMouseClicked(this::moveDown);
    }

    public void showRightMenu(MouseEvent event, JobStructure jobStructure) {
        disableAllSelectOption();
        currentJobStructure = jobStructure;
        enableNewLevelOption(currentJobStructure.getCurrentController().getAppLevel());
        enableSelectOption(currentJobStructure.getCurrentController().getAppLevel());
        rightMenuPopup.show((Node) event.getSource(), event.getScreenX(), event.getScreenY());
    }

    private void enableNewLevelOption(AppLevel appLevel) {
        if (appLevel == AppLevel.Operation)
            newTaskGroupSection.setDisable(false);
        else if (appLevel == AppLevel.TaskGroup) {
            newTaskGroupSection.setDisable(false);
            newTaskSection.setDisable(false);
        }
        else if (appLevel == AppLevel.Task) {
            newTaskSection.setDisable(false);
            newActionSection.setDisable(false);
        }
        else if (appLevel == AppLevel.Action)
            newActionSection.setDisable(false);
    }

    private void enableSelectOption(AppLevel appLevel) {
        if (appLevel == AppLevel.TaskGroup || appLevel == AppLevel.Task || appLevel == AppLevel.Action || appLevel == AppLevel.Condition) {
            copySection.setDisable(false);
            deleteSection.setDisable(false);
            moveUpSection.setDisable(false);
            moveDownSection.setDisable(false);
        }
        if (appLevel != AppLevel.Condition)
            runSection.setDisable(false);
        if (copiedJobDataController != null) {
            int order = copiedJobDataController.getAppLevel().getOrder() - appLevel.getOrder();
            if (order == 0 || order == 1)
                pasteSection.setDisable(false);
        }
    }

    private void disableAllSelectOption() {
        boolean disable = true;
        newTaskGroupSection.setDisable(disable);
        newTaskSection.setDisable(disable);
        newActionSection.setDisable(disable);
        runSection.setDisable(disable);
        copySection.setDisable(disable);
        pasteSection.setDisable(disable);
        deleteSection.setDisable(disable);
        moveUpSection.setDisable(disable);
        moveDownSection.setDisable(disable);
    }

    private void addNewTaskGroup(MouseEvent event) {
        if (currentJobStructure.getCurrentController().getAppLevel() == AppLevel.TaskGroup)
            currentJobStructure.getParentController().addSavedData(null);
        else
            currentJobStructure.getCurrentController().addSavedData(null);
        hideRightMenu();
    }

    private void addNewTask(MouseEvent event) {
        if (currentJobStructure.getCurrentController().getAppLevel() == AppLevel.Task)
            currentJobStructure.getParentController().addSavedData(null);
        else
            currentJobStructure.getCurrentController().addSavedData(null);
        hideRightMenu();
    }

    private void addNewAction(MouseEvent event) {
        if (currentJobStructure.getCurrentController().getAppLevel() == AppLevel.Action)
            currentJobStructure.getParentController().addSavedData(null);
        else
            currentJobStructure.getCurrentController().addSavedData(null);
        hideRightMenu();
    }

    private void runJob(MouseEvent event) {
        AppScene.startJobRun(currentJobStructure.getCurrentController());
        hideRightMenu();
    }

    private void copyData(MouseEvent event) {
        copiedJobDataController = currentJobStructure.getCurrentController();
        hideRightMenu();
    }

    private void pasteData(MouseEvent event) {
        if (copiedJobDataController.getAppLevel().getOrder() - currentJobStructure.getCurrentController().getAppLevel().getOrder() == 0)
            currentJobStructure.getParentController().addSavedData(copiedJobDataController.getSavedData());
        else
            currentJobStructure.getCurrentController().addSavedData(copiedJobDataController.getSavedData());
        hideRightMenu();
    }

    private void deleteData(MouseEvent event) {
        currentJobStructure.getParentController().removeSavedData(currentJobStructure.getCurrentController());
        if (currentJobStructure.getCurrentController().getAppLevel() == AppLevel.Task)
            currentJobStructure.getParentController().takeToDisplay();
        hideRightMenu();
    }

    private void moveUp(MouseEvent event) {
        currentJobStructure.getParentController().moveSavedDataUp(currentJobStructure.getCurrentController());
    }

    private void moveDown(MouseEvent event) {
        currentJobStructure.getParentController().moveSavedDataDown(currentJobStructure.getCurrentController());
    }

    private void hideRightMenu() {
        rightMenuPopup.hide();
    }
}

