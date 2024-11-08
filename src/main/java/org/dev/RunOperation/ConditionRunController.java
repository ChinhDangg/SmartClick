package org.dev.RunOperation;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.dev.Operation.Condition.Condition;

import java.net.URL;
import java.util.ResourceBundle;

public class ConditionRunController extends RunActivity implements Initializable {

    @FXML
    private Label expectedResultLabel, readResultLabel;
    @FXML
    private Pane conditionExpectedPane, conditionReadPane;
    @FXML
    private ImageView conditionExpectedImageView, conditionReadImageView;
    @FXML
    private StackPane stackPaneImageViewContainer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        double width = stackPaneImageViewContainer.getPrefWidth();
        double height = stackPaneImageViewContainer.getPrefHeight();
        conditionExpectedImageView.setFitWidth(width);
        conditionExpectedImageView.setFitWidth(height);
        conditionReadImageView.setFitWidth(width);
        conditionReadImageView.setFitHeight(height);
    }

    public boolean checkCondition(Condition condition) {
        updateImageView(conditionExpectedImageView, condition.getMainDisplayImage());
        changeLabelText(expectedResultLabel, condition.getExpectedResult());
        boolean passed = condition.checkCondition();
        changeLabelText(readResultLabel, condition.getActualResult());
        updateImageView(conditionReadImageView, condition.getMainImageBoundingBox());

        updatePaneStatusColor(conditionReadPane, passed);
        if (condition.isRequired())
            updatePaneStatusColor(conditionExpectedPane, passed);
        return passed;
    }


}
