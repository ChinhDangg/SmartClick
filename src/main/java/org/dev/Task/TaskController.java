package org.dev.Task;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class TaskController implements Initializable {
    @FXML
    private VBox taskVBox;
    @FXML
    private StackPane removeActionButton, moveActionUpButton, moveActionDownButton, addNewActionButton;
    private final List<ActionController> actionList = new ArrayList<>();
    private List<ActionController> getListOfActions() { return new ArrayList<>(actionList);}

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addNewActionButton.setOnMouseClicked(this::addNewAction);
        removeActionButton.setOnMouseClicked(this::removeSelectedActionPane);
        moveActionDownButton.setOnMouseClicked(this::moveActionDown);
        moveActionUpButton.setOnMouseClicked(this::moveActionUp);

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
            int numberOfActions = taskVBox.getChildren().size()-1;
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
        runTask();
        if (currentSelectedActionPane == null)
            return;
        ObservableList<Node> children = taskVBox.getChildren();
        int numberOfActions = children.size()-1;
        if (numberOfActions < 2)
            return;
        int selectedActionPaneIndex = children.indexOf(currentSelectedActionPane);
        int changeIndex = selectedActionPaneIndex+1;
        if (changeIndex == numberOfActions)
            return;;
        actionList.add(changeIndex, actionList.remove(selectedActionPaneIndex));
        changeActionPreviousOptions(changeIndex);
        children.remove(currentSelectedActionPane);
        children.add(changeIndex, currentSelectedActionPane);
    }
    private void moveActionDown(MouseEvent event) {
        if (currentSelectedActionPane == null)
            return;
        ObservableList<Node> children = taskVBox.getChildren();
        int numberOfActions = children.size()-1;
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

    public void runTask() {
        for (ActionController actionController : actionList) {

        }
    }
}
