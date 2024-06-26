package org.dev.Operation;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import org.dev.App;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class TaskController implements Initializable {
    @FXML
    private Group mainTaskGroup;
    @FXML
    private VBox mainTaskOuterVBox, taskVBox;
    @FXML
    private Label taskNameLabel;
    @FXML
    private Group renameOptionGroup, backButton;
    @FXML
    private TextField renameTextField;
    @FXML
    private StackPane renameButton, removeActionButton, moveActionUpButton, moveActionDownButton, addNewActionButton;
    private final List<ActionController> actionList = new ArrayList<>();

    private double currentGlobalScale = 1;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        taskNameLabel.setOnMouseClicked(this::toggleRenameTaskPane);
        renameButton.setOnMouseClicked(this::changeTaskName);
        renameOptionGroup.setVisible(false);
        addNewActionButton.setOnMouseClicked(this::addNewAction);
        removeActionButton.setOnMouseClicked(this::removeSelectedActionPane);
        moveActionDownButton.setOnMouseClicked(this::moveActionDown);
        moveActionUpButton.setOnMouseClicked(this::moveActionUp);
        backButton.setOnMouseClicked(this::backToPrevious);
    }

    public boolean isSet() { return (!actionList.isEmpty() && actionList.getFirst().isSet()); }
    public Node getTaskPane() {
        if (currentGlobalScale != App.currentGlobalScale) {
            currentGlobalScale = App.currentGlobalScale;
            mainTaskOuterVBox.getTransforms().add(new Scale(currentGlobalScale, currentGlobalScale, 0, 0));
        }
        return mainTaskGroup;
    }
    public String getTaskName() { return taskNameLabel.getText(); }
    private void backToPrevious(MouseEvent event) { App.backToPrevious(mainTaskGroup); }

    private void toggleRenameTaskPane(MouseEvent event) {
        renameTextField.setText(taskNameLabel.getText());
        renameOptionGroup.setVisible(!renameOptionGroup.isVisible());
    }
    private void changeTaskName(MouseEvent event) {
        String name = renameTextField.getText();
        name = name.replace("\n", "");
        if (name.isBlank())
            return;
        taskNameLabel.setText(name);
        renameOptionGroup.setVisible(false);
    }

    private void addNewAction(MouseEvent event) {
        try {
            if (!actionList.isEmpty() && !actionList.getLast().isSet()) {
                System.out.println("Recent action is not set");
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("actionPane.fxml"));
            Pane actionPane = loader.load();
            actionPane.setOnMouseClicked(this::selectTheActionPane);
            ActionController actionController = loader.getController();
            int numberOfActions = taskVBox.getChildren().size();
            if (numberOfActions == 0)
                actionController.disablePreviousOptions();
            taskVBox.getChildren().add(numberOfActions, actionPane);
            actionList.add(actionController);
        } catch (IOException e) {
            System.out.println("Fail loading action pane in task controller");
        }
    }

    private Pane currentSelectedActionPane = null;
    private void selectTheActionPane(MouseEvent event) { 
        try {
            if (currentSelectedActionPane != null)
                setUnSelected(currentSelectedActionPane);
            currentSelectedActionPane = (Pane) event.getSource();
            setSelected(currentSelectedActionPane);
        } catch (Exception e) {
            System.out.println("Fail assigning current selected action pane");
        }
    }
    private void removeSelectedActionPane(MouseEvent event) {
        if (currentSelectedActionPane != null) {
            int changeIndex = taskVBox.getChildren().indexOf(currentSelectedActionPane);
            actionList.remove(changeIndex);
            if (changeIndex == 0 && !actionList.isEmpty())
                actionList.getFirst().disablePreviousOptions();
            taskVBox.getChildren().remove(currentSelectedActionPane);
            System.out.println("Removed the selected action pane");
            currentSelectedActionPane = null;
        }
    }
    private void setSelected(Pane actionPane) { actionPane.setStyle("-fx-border-color: black; -fx-border-width: 1px;"); }
    private void setUnSelected(Pane actionPane) { actionPane.setStyle(""); }
    private void moveActionUp(MouseEvent event) {
        if (currentSelectedActionPane == null)
            return;
        ObservableList<Node> children = taskVBox.getChildren();
        int numberOfActions = children.size();
        if (numberOfActions < 2)
            return;
        int selectedActionPaneIndex = children.indexOf(currentSelectedActionPane);
        int changeIndex = selectedActionPaneIndex+1;
        if (changeIndex == numberOfActions)
            return;
        actionList.add(changeIndex, actionList.remove(selectedActionPaneIndex));
        changeActionPreviousOptions(changeIndex);
        children.remove(currentSelectedActionPane);
        children.add(changeIndex, currentSelectedActionPane);
    }
    private void moveActionDown(MouseEvent event) {
        if (currentSelectedActionPane == null)
            return;
        ObservableList<Node> children = taskVBox.getChildren();
        int numberOfActions = children.size();
        if (numberOfActions < 2)
            return;
        int selectedActionPaneIndex = children.indexOf(currentSelectedActionPane);
        if (selectedActionPaneIndex == 0)
            return;
        int changeIndex = selectedActionPaneIndex-1;
        actionList.add(changeIndex, actionList.remove(selectedActionPaneIndex));
        changeActionPreviousOptions(changeIndex);
        children.remove(currentSelectedActionPane);
        children.add(changeIndex, currentSelectedActionPane);
    }
    private void changeActionPreviousOptions(int index) {
        if (index < 2) {
            actionList.get(0).disablePreviousOptions();
            actionList.get(1).enablePreviousOptions();
        }
    }

    public boolean runTask() {
        System.out.println(STR."Start running task: \{taskNameLabel.getText()}");
        boolean pass = false;
        for (ActionController actionController : actionList) {
            String actionName = actionController.getActionNameLabel().getText();
            if (pass && actionController.isPreviousPass()) {
                System.out.println(STR."Skipping action '\{actionName}' as previous is passed");
                continue;
            }
            System.out.println(STR."Performing action: \{actionName}");
            pass = actionController.performAction();
            if (!actionController.isRequired())
                pass = true;
            else if (!pass) { // action is required but failed
                System.out.println(STR."Fail performing action: \{actionName}");
                return false;
            }
        }
        return true;
    }
}