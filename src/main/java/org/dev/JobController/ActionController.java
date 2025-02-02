package org.dev.JobController;

import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import lombok.Getter;
import lombok.Setter;
import org.dev.AppScene;
import org.dev.Enum.ActionTypes;
import org.dev.Enum.AppLevel;
import org.dev.Enum.ConditionType;
import org.dev.Enum.LogLevel;
import org.dev.Job.Action.Action;
import org.dev.Job.Condition.Condition;
import org.dev.Job.JobData;
import org.dev.JobStructure;
import org.dev.RunJob.ActionRunController;
import org.dev.RunJob.JobRunController;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ActionController implements Initializable, JobDataController, ActivityController {

    @FXML
    private Node mainActionParentNode;
    @FXML
    private TextField renameTextField;
    @FXML
    private CheckBox requiredCheckBox, previousPassCheckBox;
    @FXML
    private ImageView actionImage;
    @FXML
    private StackPane actionPane;
    @FXML
    private HBox entryConditionHBox, exitConditionHBox;
    @FXML
    private StackPane entryAddButton, exitAddButton;

    private JobStructure currentStructure;

    @Getter
    private boolean isSet;
    @Getter
    private Action action;
    @Getter @Setter
    private ActionTypes chosenActionPerform;

    private final String className = this.getClass().getSimpleName();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        actionPane.setOnMouseClicked(this::openActionMenuPane);
        entryAddButton.setOnMouseClicked(this::addNewEntryCondition);
        exitAddButton.setOnMouseClicked(this::addNewExitCondition);
        renameTextField.focusedProperty().addListener((_, _, newValue) -> {
            if (!newValue) {
                //System.out.println("TextField lost focus");
                changeActionName();
            }
        });
    }

    public void setJobStructure(JobStructure structure) {
        currentStructure = structure;
    }

    private void changeActionName() {
        String name = renameTextField.getText();
        name = name.strip();
        if (name.isBlank()) {
            renameTextField.setText(currentStructure.getName());
            return;
        }
        updateActionName(name);
    }
    private void updateActionName(String name) {
        currentStructure.changeName(name);
        renameTextField.setText(name);
        AppScene.addLog(LogLevel.DEBUG, className, "Renamed action: " + name);
    }

    public void disablePreviousOption() {
        previousPassCheckBox.setSelected(false);
        previousPassCheckBox.setVisible(false);
    }
    public void enablePreviousOption() { previousPassCheckBox.setVisible(true); }

    public void registerActionPerform(Action newAction) {
        if (newAction == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - Action is null - registerActionPerform");
            return;
        }
        isSet = true;
        action = newAction;
        displayActionImage(action.getMainDisplayImage());
    }

    private void displayActionImage(BufferedImage image) {
        actionImage.setImage(SwingFXUtils.toFXImage(image, null));
    }

    private void openActionMenuPane(MouseEvent event) {
        if (AppScene.isJobRunning) {
            AppScene.addLog(LogLevel.INFO, className, "Operation is running - cannot modify");
            return;
        }
        AppScene.openActionMenuPane(this);
    }

    private void addNewEntryCondition(MouseEvent event) {
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on add entry condition");
        if (AppScene.isJobRunning) {
            AppScene.addLog(LogLevel.INFO, className, "Operation is running - cannot modify");
            return;
        }
        if (event.getButton() == MouseButton.PRIMARY) {
            addCondition(ConditionType.Entry, null);
        }
    }

    private void addNewExitCondition(MouseEvent event) {
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on add exit condition");
        if (AppScene.isJobRunning) {
            AppScene.addLog(LogLevel.INFO, className, "Operation is running - cannot modify");
            return;
        }
        if (event.getButton() == MouseButton.PRIMARY) {
            addCondition(ConditionType.Exit, null);
        }
    }

    private void addCondition(ConditionType conditionType, JobData conditionData) {
        HBox whichPane = findWhichConditionHBox(conditionType);
        int numberOfCondition = whichPane.getChildren().size();
        int lastIndex = getLastConditionControllerIndex(conditionType);
        if (numberOfCondition > 0 && (lastIndex != -1 && currentStructure.getSubJobStructures().get(lastIndex).getCurrentController().isSet())) {
            AppScene.addLog(LogLevel.INFO, className, "Previous Condition is not set");
            return;
        }
        if (numberOfCondition >= 5) {
            AppScene.addLog(LogLevel.INFO, className, "Max number of condition's reached");
            return;
        }
        AppScene.addLog(LogLevel.TRACE, className, "Loading Condition Pane");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("conditionPane.fxml"));
            Node pane = loader.load();
            ConditionController controller = loader.getController();
            controller.setConditionType(conditionType);
            AppScene.addLog(LogLevel.DEBUG, className, "Loaded Condition Pane");
            if (conditionData != null)
                controller.loadSavedData(conditionData);
            whichPane.getChildren().add(pane);

            lastIndex = (lastIndex == -1) ? currentStructure.getSubStructureSize() : lastIndex;
            JobStructure conditionStructure = new JobStructure(this,this, controller, null);
            controller.setJobStructure(conditionStructure);
            currentStructure.addSubJobStructure(lastIndex, conditionStructure);
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading condition pane: " + e.getMessage());
        }
    }

    private int getLastConditionControllerIndex(ConditionType conditionType) {
        int count = 0;
        for (JobStructure subJobStructure : currentStructure.getSubJobStructures()) {
            if (((ConditionController) subJobStructure.getCurrentController()).getConditionType() == conditionType)
                return count;
            count++;
        }
        return -1;
    }

    private HBox findWhichConditionHBox(ConditionType conditionType) {
        return (conditionType == ConditionType.Entry) ? entryConditionHBox : exitConditionHBox;
    }

    // ------------------------------------------------------
    @Override
    public String getName() { return renameTextField.getText(); }

    @Override
    public Node getParentNode() { return mainActionParentNode; }

    @Override
    public AppLevel getAppLevel() { return AppLevel.Action; }

    @Override
    public void takeToDisplay() {
        AppScene.closeActionMenuPane();
        AppScene.closeConditionMenuPane();
        TaskController parentTaskController = (TaskController) currentStructure.getParentController();
        parentTaskController.takeToDisplay();
        AppScene.updateMainDisplayScrollValue(getParentNode());
        AppScene.addLog(LogLevel.DEBUG, className, "Take to display");
    }

    @Override
    public JobData getSavedData() {
        if (action == null)
            return null;
        action.setRequired(requiredCheckBox.isSelected());
        action.setPreviousPass(previousPassCheckBox.isSelected());
        action.setActionName(currentStructure.getName());
        List<JobData> conditionDataList = new ArrayList<>();
        for (JobStructure subJobStructure : currentStructure.getSubJobStructures())
            conditionDataList.add(subJobStructure.getCurrentController().getSavedData());
        JobData actionData = new JobData(action.getDeepCopied(), conditionDataList);
        AppScene.addLog(LogLevel.TRACE, className, "Got action data");
        return actionData;
    }

    @Override
    public void loadSavedData(JobData jobData) {
        if (jobData == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - Action data is null - cannot load from save");
            return;
        }
        Action action = (Action) jobData.getMainJob();
        registerActionPerform(action);
        updateActionName(action.getActionName());
        requiredCheckBox.setSelected(action.isRequired());
        previousPassCheckBox.setSelected(action.isPreviousPass());
        displayActionImage(action.getDisplayImage());
        List<JobData> conditionDataList = jobData.getJobDataList();
        for (JobData conditionData : conditionDataList)
            addSavedData(conditionData);
    }

    @Override
    public void addSavedData(JobData conditionData) {
        Condition condition = (Condition) conditionData.getMainJob();
        addCondition(condition.getConditionType(), conditionData);
    }

    @Override
    public void removeSavedData(JobDataController jobDataController) {
        ConditionController conditionController = (ConditionController) jobDataController;
        Node conditionNode = conditionController.getParentNode();
        if (conditionController.getConditionType() == ConditionType.Entry)
            entryConditionHBox.getChildren().remove(conditionNode);
        else if (conditionController.getConditionType() == ConditionType.Exit)
            exitConditionHBox.getChildren().remove(conditionNode);
        currentStructure.removeSubJobStructure(jobDataController);
    }

    @Override
    public void moveSavedDataUp(JobDataController jobDataController) {
        ConditionController conditionController = (ConditionController) jobDataController;
        HBox whichHBox = findWhichConditionHBox(conditionController.getConditionType());
        int numberOfConditions = whichHBox.getChildren().size();
        if (numberOfConditions < 2)
            return;
        int selectedIndex = whichHBox.getChildren().indexOf(conditionController.getParentNode());
        if (selectedIndex == 0)
            return;
        int changeIndex = selectedIndex -1;
        updateActionPaneList(whichHBox, selectedIndex, changeIndex);
        int newIndex = currentStructure.getSubStructureIndex(jobDataController) -1;
        currentStructure.updateSubJobStructure(jobDataController, newIndex);
        AppScene.addLog(LogLevel.DEBUG, className, "Moved down condition: " + changeIndex);
    }

    @Override
    public void moveSavedDataDown(JobDataController jobDataController) {
        ConditionController conditionController = (ConditionController) jobDataController;
        HBox whichHBox = findWhichConditionHBox(conditionController.getConditionType());
        int numberOfConditions = whichHBox.getChildren().size();
        if (numberOfConditions < 2)
            return;
        int selectedIndex = whichHBox.getChildren().indexOf(conditionController.getParentNode());
        int changeIndex = selectedIndex +1;
        if (changeIndex == numberOfConditions)
            return;
        updateActionPaneList(whichHBox, selectedIndex, changeIndex);
        int newIndex = currentStructure.getSubStructureIndex(jobDataController) +1;
        currentStructure.updateSubJobStructure(jobDataController, newIndex);
        AppScene.addLog(LogLevel.DEBUG, className, "Moved down condition: " + changeIndex);
    }

    private void updateActionPaneList(HBox whichHBox, int selectedIndex, int changeIndex) {
        ObservableList<Node> children = whichHBox.getChildren();
        Node conditionNode = children.get(selectedIndex);
        children.remove(conditionNode);
        children.add(changeIndex, conditionNode);
    }

    @Override
    public JobRunController getRunJob() {
        try {
            FXMLLoader loader = new FXMLLoader(AppScene.class.getResource("RunJob/actionRunPane.fxml"));
            loader.load();
            ActionRunController actionRunController = loader.getController();
            AppScene.addLog(LogLevel.DEBUG, className, "Loaded Action Run");
            return actionRunController;
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading Action Run Pane: " + e.getMessage());
            return null;
        }
    }
}