package view;

import data.DataStore;
import model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class LoginView {
    private Stage stage;

    public LoginView(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        // Main container with gradient background
        StackPane mainContainer = new StackPane();
        mainContainer.setStyle("-fx-background-color: linear-gradient(to bottom right, #667eea, #764ba2);");

        // Login card
        VBox loginCard = new VBox(20);
        loginCard.setPadding(new Insets(40));
        loginCard.setAlignment(Pos.CENTER);
        loginCard.setMaxWidth(400);
        loginCard.setMaxHeight(500);
        loginCard.setStyle("-fx-background-color: white; -fx-background-radius: 15px;");

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.3));
        shadow.setRadius(25);
        shadow.setOffsetY(10);
        loginCard.setEffect(shadow);

        // Logo/Icon
        ImageView logoView = new ImageView(new Image("file:src/images/Logo_Cafe.png"));
        logoView.setFitWidth(100);
        logoView.setFitHeight(100);
        logoView.setPreserveRatio(true);

        // Title
        Label titleLabel = new Label("Camellia Cafe");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.web("#333"));

        Label subtitleLabel = new Label("Welcome back! Please login.");
        subtitleLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 14px;");

        // Form fields
        VBox formBox = new VBox(15);
        formBox.setAlignment(Pos.CENTER);
        formBox.setPadding(new Insets(20, 0, 0, 0));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(300);
        usernameField.setPrefHeight(45);
        usernameField.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 8px; " +
                "-fx-border-color: #ddd; -fx-border-radius: 8px; -fx-font-size: 14px;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(300);
        passwordField.setPrefHeight(45);
        passwordField.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 8px; " +
                "-fx-border-color: #ddd; -fx-border-radius: 8px; -fx-font-size: 14px;");

        Button loginButton = new Button("Login");
        loginButton.setMaxWidth(300);
        loginButton.setPrefHeight(45);
        loginButton.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2); " +
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; " +
                "-fx-background-radius: 8px; -fx-cursor: hand;");

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 13px;");

        // Demo credentials info
        Label infoLabel = new Label("Demo: admin/admin, seller/seller, buyer/buyer");
        infoLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");

        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            User user = DataStore.getInstance().authenticate(username, password);
            if (user != null) {
                redirect(user);
            } else {
                messageLabel.setText("Invalid username or password!");
            }
        });

        // Enter key support
        passwordField.setOnAction(e -> loginButton.fire());

        formBox.getChildren().addAll(usernameField, passwordField, loginButton, messageLabel, infoLabel);
        loginCard.getChildren().addAll(logoView, titleLabel, subtitleLabel, formBox);
        mainContainer.getChildren().add(loginCard);

        Scene scene = new Scene(mainContainer, 500, 600);
        stage.setScene(scene);
        stage.setTitle("Login - Camellia Cafe");
        stage.show();
    }

    private void redirect(User user) {
        switch (user.getRole()) {
            case "ADMIN":
                new AdminDashboard(stage, user).show();
                break;
            case "SELLER":
                new SellerDashboard(stage, user).show();
                break;
            case "BUYER":
                new BuyerDashboard(stage, user).show();
                break;
        }
    }
}
