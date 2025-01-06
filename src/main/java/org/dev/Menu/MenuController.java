package org.dev.Menu;

import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Getter;
import org.dev.Operation.ActivityController;
import java.net.URL;
import java.util.ResourceBundle;

public abstract class MenuController implements Initializable, NativeKeyListener {
    @FXML @Getter
    protected StackPane mainMenuStackPane;
    @FXML
    protected Pane backgroundPane;
    @FXML
    protected Group menuMainGroup;
    @FXML
    protected StackPane backButton, recheckButton;
    @FXML
    protected VBox recheckContentVBox;
    @FXML
    protected Pane startRegisteringButton;
    @FXML
    protected ImageView mainImageView, recheckResultImageView;
    @FXML
    protected Label recheckResultLabel;
    protected boolean isKeyListening = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadTypeChoices();
        backgroundPane.setOnMouseClicked(this::closeMenuControllerAction);
        startRegisteringButton.setOnMouseClicked(this::startRegisteringAction);
        backButton.setOnMouseClicked(this::closeMenuControllerAction);
        recheckButton.setOnMouseClicked(this::recheck);
    }

    protected void setMenuMainGroupVisible(boolean visible) { menuMainGroup.setVisible(visible); }
    protected abstract void loadTypeChoices();
    protected abstract void closeMenuControllerAction(MouseEvent event);
    protected abstract void startRegisteringAction(MouseEvent event);
    public abstract void loadMenu(ActivityController activityController);
    protected abstract void recheck();
    protected abstract void recheck(MouseEvent event);
}
