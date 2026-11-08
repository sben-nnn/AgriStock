package com.agristock;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class MainApp extends Application {
    private Stage stage;

    @Override
    public void start(Stage primary) {
        stage = primary;
        stage.setTitle("AgriStock - Agricultural Stock Management System");
        stage.setMinWidth(1100); stage.setMinHeight(700);
        showLogin();
        stage.show();
    }

    // login UI
    private void showLogin() {
        Label title = new Label("AgriStock");
        Label sub = new Label("Agricultural Stock Management System");
        TextField user = new TextField(); user.setPromptText("Username");
        PasswordField pass = new PasswordField(); pass.setPromptText("Password");
        Label msg = new Label(); msg.setWrapText(true);   // error message area
        Button login = new Button("Sign in");
        login.setMaxWidth(Double.MAX_VALUE);
        login.setDefaultButton(true);

        VBox card = new VBox(12, title, sub, new Separator(), user, pass, login, msg);
        card.setMaxWidth(380);
        StackPane root = new StackPane(card);
        stage.setScene(new Scene(root, 1200, 780));
        user.requestFocus();
    }

    // launches the app
    public static void main(String[] args) { launch(args); }
}