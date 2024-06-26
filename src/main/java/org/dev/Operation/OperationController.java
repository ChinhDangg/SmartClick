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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class OperationController implements Initializable {
    @FXML
    private VBox mainTaskVBox;
    @FXML
    private Label operationNameLabel;
    @FXML
    private Group renameOptionGroup;
    @FXML
    private StackPane renameButton;
    @FXML
    private TextField renameTextField;
    @FXML
    private StackPane removeTaskButton, moveTaskUpButton, moveTaskDownButton;
    @FXML
    private VBox operationVBox;
    @FXML
    private HBox addTaskButton;
    private final List<MinimizedTaskController> taskList = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        operationNameLabel.setOnMouseClicked(this::toggleRenameOperationPane);
        renameOptionGroup.setVisible(false);
        renameButton.setOnMouseClicked(this::changeOperationName);
        addTaskButton.setOnMouseClicked(this::addNewMinimizedTask);
        removeTaskButton.setOnMouseClicked(this::removeSelectedTaskPane);
        moveTaskUpButton.setOnMouseClicked(this::moveTaskUp);
        moveTaskDownButton.setOnMouseClicked(this::moveTaskDown);
    }

    private void toggleRenameOperationPane(MouseEvent event) {
        renameTextField.setText(operationNameLabel.getText());
        renameOptionGroup.setVisible(!renameOptionGroup.isVisible());
    }
    private void changeOperationName(MouseEvent event) {
        String name = renameTextField.getText();
        name = name.replace("\n", "");
        if (name.isBlank())
            return;
        operationNameLabel.setText(name);
        renameOptionGroup.setVisible(false);
    }

    // ------------------------------------------------------
    private void addNewMinimizedTask(MouseEvent event) {
        try {
            if (!taskList.isEmpty() && !taskList.getFirst().isSet()) {
                System.out.println("Recent minimized task is not set");
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("minimizedTaskPane.fxml"));
            StackPane taskPane = loader.load();
            taskPane.getChildren().getFirst().setOnMouseClicked(this::selectTheTaskPane);
            MinimizedTaskController controller = loader.getController();
            int numberOfTask = operationVBox.getChildren().size();
            if (numberOfTask == 0)
                controller.disablePreviousOption();
            controller.setTaskIndex(numberOfTask+1);
            operationVBox.getChildren().add(numberOfTask, taskPane);
            taskList.add(controller);
        } catch (Exception e) {
            System.out.println("Fail loading minimized task pane");
        }
    }

    // ------------------------------------------------------
    private Pane currentSelectedTaskPane = null;
    private void selectTheTaskPane(MouseEvent event) {
        try {
            if (currentSelectedTaskPane != null)
                setUnselected(currentSelectedTaskPane);
            currentSelectedTaskPane = (Pane) event.getSource();
            setSelected(currentSelectedTaskPane);
        } catch (Exception e) {
            System.out.println("Fail assigning current selected task pane");
        }
    }
    private void setSelected(Pane taskPane) { taskPane.setStyle("-fx-border-color: black; -fx-border-width: 1px;"); }
    private void setUnselected(Pane taskPane) { taskPane.setStyle(""); }

    // ------------------------------------------------------
    private void removeSelectedTaskPane(MouseEvent event) {
        if (currentSelectedTaskPane == null)
            return;
        int changeIndex = operationVBox.getChildren().indexOf(currentSelectedTaskPane.getParent());
        taskList.remove(changeIndex);
        if (changeIndex == 0 && !taskList.isEmpty())
            taskList.getFirst().disablePreviousOption();
        operationVBox.getChildren().remove(currentSelectedTaskPane.getParent());
        System.out.println("Removed the selected task pane");
        currentSelectedTaskPane = null;
        updateTaskIndex(changeIndex);
    }
    private void moveTaskUp(MouseEvent event) {
        startOperation();
        if (currentSelectedTaskPane == null)
            return;
        ObservableList<Node> children = operationVBox.getChildren();
        int numberOfTasks = children.size();
        if (numberOfTasks < 2)
            return;
        Node selectedNode = currentSelectedTaskPane.getParent();
        int selectedTaskPaneIndex = children.indexOf(selectedNode);
        int changeIndex = selectedTaskPaneIndex+1;
        if (changeIndex == numberOfTasks)
            return;
        taskList.add(changeIndex, taskList.remove(selectedTaskPaneIndex));
        changeTaskPreviousOption(changeIndex);
        children.remove(selectedNode);
        children.add(changeIndex, selectedNode);
        updateTaskIndex(changeIndex-1);
    }
    private void moveTaskDown(MouseEvent event) {
        if (currentSelectedTaskPane == null)
            return;
        ObservableList<Node> children = operationVBox.getChildren();
        int numberOfTasks = children.size();
        if (numberOfTasks < 2)
            return;
        Node selectedNode = currentSelectedTaskPane.getParent();
        int selectedTaskPaneIndex = children.indexOf(selectedNode);
        if (selectedTaskPaneIndex == 0)
            return;
        int changeIndex = selectedTaskPaneIndex-1;
        taskList.add(changeIndex, taskList.remove(selectedTaskPaneIndex));
        changeTaskPreviousOption(changeIndex);
        children.remove(selectedNode);
        children.add(changeIndex, selectedNode);
        updateTaskIndex(changeIndex);
    }
    private void changeTaskPreviousOption(int index) {
        if (index < 2) {
            taskList.get(0).disablePreviousOption();
            taskList.get(1).enablePreviousOption();
        }
    }
    private void updateTaskIndex(int start) {
        for (int j = start; j < taskList.size(); j++)
            taskList.get(j).setTaskIndex(j+1);
    }

    // ------------------------------------------------------
    public void startOperation() {
        Thread thread = new Thread(this::runOperation);
        thread.start();
    }
    private void runOperation() {
        System.out.println(STR."Start running operation: \{operationNameLabel.getText()}");
        boolean pass = false;
        for (MinimizedTaskController taskController : taskList) {
            String taskName = taskController.getTaskName();
            if (pass && taskController.isPreviousPass()) {
                System.out.println(STR."Skipping task \{taskName} as previous is passed");
                continue;
            }
            pass = taskController.runTask();
            if (!taskController.isRequired())
                pass = true;
            else if (!pass) { // task is required but failed
                System.out.println(STR."Fail performing task: \{taskName}");
                break;
            }
        }
    }
}