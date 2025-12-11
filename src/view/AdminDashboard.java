package view;

import data.DataStore;
import model.Category;
import model.Order;
import model.OrderStatus;
import model.Product;
import model.User;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class AdminDashboard {
    private Stage stage;
    private User user;
    private TableView<Product> productTable;
    private TableView<User> userTable;
    private TabPane tabPane;

    public AdminDashboard(Stage stage, User user) {
        this.stage = stage;
        this.user = user;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f8f9fa;");

        // Header
        HBox header = createHeader();
        root.setTop(header);

        // Tab pane for different sections
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: transparent;");

        Tab productsTab = new Tab("Products", createProductsTab());
        Tab usersTab = new Tab("Users", createUsersTab());
        Tab statsTab = new Tab("Statistics", createStatsTab());

        tabPane.getTabs().addAll(productsTab, usersTab, statsTab);
        root.setCenter(tabPane);

        Scene scene = new Scene(root, 1100, 700);
        stage.setScene(scene);
        stage.setTitle("Admin Dashboard");
    }

    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: linear-gradient(to right, #2c3e50, #4ca1af);");

        Label logo = new Label("Admin Dashboard");
        logo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label userLabel = new Label("Admin: " + user.getUsername());
        userLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle(
                "-fx-background-color: white; -fx-text-fill: #2c3e50; -fx-font-weight: bold; -fx-background-radius: 20px;");
        logoutBtn.setOnAction(e -> new LoginView(stage).show());

        header.getChildren().addAll(logo, userLabel, spacer, logoutBtn);
        return header;
    }

    private VBox createProductsTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        HBox mainBox = new HBox(20);

        // Product Table
        VBox tableBox = new VBox(10);
        tableBox.setStyle("-fx-background-color: white; -fx-background-radius: 10px; -fx-padding: 15;");
        HBox.setHgrow(tableBox, Priority.ALWAYS);

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.1));
        shadow.setRadius(10);
        tableBox.setEffect(shadow);

        Label tableTitle = new Label("Product List");
        tableTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        productTable = new TableView<>();
        VBox.setVgrow(productTable, Priority.ALWAYS);

        TableColumn<Product, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);

        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(120);

        TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(80);

        TableColumn<Product, Integer> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
        stockCol.setPrefWidth(60);

        TableColumn<Product, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getCategory().getDisplayName()));
        categoryCol.setPrefWidth(80);

        TableColumn<Product, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(200);

        productTable.getColumns().addAll(idCol, nameCol, priceCol, stockCol, categoryCol, descCol);
        refreshProductTable();

        tableBox.getChildren().addAll(tableTitle, productTable);

        // Form sidebar
        VBox formBox = createProductForm();

        mainBox.getChildren().addAll(tableBox, formBox);
        VBox.setVgrow(mainBox, Priority.ALWAYS);
        content.getChildren().add(mainBox);
        return content;
    }

    private VBox createProductForm() {
        VBox formBox = new VBox(12);
        formBox.setPadding(new Insets(20));
        formBox.setPrefWidth(280);
        formBox.setStyle("-fx-background-color: white; -fx-background-radius: 10px;");

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.1));
        shadow.setRadius(10);
        formBox.setEffect(shadow);

        Label formTitle = new Label("Product Form");
        formTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextField idField = new TextField();
        idField.setPromptText("Product ID (e.g., P009)");
        styleTextField(idField);

        TextField nameField = new TextField();
        nameField.setPromptText("Product Name");
        styleTextField(nameField);

        TextField priceField = new TextField();
        priceField.setPromptText("Price (e.g., 15000)");
        styleTextField(priceField);

        TextField stockField = new TextField();
        stockField.setPromptText("Stock");
        styleTextField(stockField);

        TextField descField = new TextField();
        descField.setPromptText("Description");
        styleTextField(descField);

        ComboBox<Category> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(Category.values());
        categoryCombo.setPromptText("Select Category");
        categoryCombo.setMaxWidth(Double.MAX_VALUE);

        // Load selected product
        Button loadBtn = new Button("Load Selected");
        loadBtn.setMaxWidth(Double.MAX_VALUE);
        loadBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-background-radius: 5px;");
        loadBtn.setOnAction(e -> {
            Product selected = productTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                idField.setText(selected.getId());
                nameField.setText(selected.getName());
                priceField.setText(String.valueOf(selected.getPrice()));
                stockField.setText(String.valueOf(selected.getStock()));
                descField.setText(selected.getDescription());
                categoryCombo.setValue(selected.getCategory());
            }
        });

        Button addBtn = new Button("Add New");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setStyle(
                "-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");
        addBtn.setOnAction(e -> {
            try {
                Product newProduct = new Product(
                        idField.getText(),
                        nameField.getText(),
                        Double.parseDouble(priceField.getText()),
                        descField.getText(),
                        Integer.parseInt(stockField.getText()),
                        categoryCombo.getValue());
                DataStore.getInstance().addProduct(newProduct);
                refreshProductTable();
                clearFields(idField, nameField, priceField, stockField, descField);
                showAlert("Product added successfully!", Alert.AlertType.INFORMATION);
            } catch (Exception ex) {
                showAlert("Invalid input! Please check all fields.", Alert.AlertType.ERROR);
            }
        });

        Button updateBtn = new Button("Update Selected");
        updateBtn.setMaxWidth(Double.MAX_VALUE);
        updateBtn.setStyle(
                "-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");
        updateBtn.setOnAction(e -> {
            Product selected = productTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    selected.setId(idField.getText());
                    selected.setName(nameField.getText());
                    selected.setPrice(Double.parseDouble(priceField.getText()));
                    selected.setStock(Integer.parseInt(stockField.getText()));
                    selected.setDescription(descField.getText());
                    selected.setCategory(categoryCombo.getValue());
                    DataStore.getInstance().saveAllData();
                    productTable.getItems().clear();
                    refreshProductTable();
                    showAlert("Product updated successfully!", Alert.AlertType.INFORMATION);
                } catch (Exception ex) {
                    showAlert("Invalid input!", Alert.AlertType.ERROR);
                }
            } else {
                showAlert("Please select a product first!", Alert.AlertType.WARNING);
            }
        });

        Button deleteBtn = new Button("Delete Selected");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        deleteBtn.setStyle(
                "-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");
        deleteBtn.setOnAction(e -> {
            Product selected = productTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                DataStore.getInstance().removeProduct(selected);
                refreshProductTable();
                clearFields(idField, nameField, priceField, stockField, descField);
                showAlert("Product deleted!", Alert.AlertType.INFORMATION);
            }
        });

        formBox.getChildren().addAll(formTitle, idField, nameField, priceField, stockField, descField,
                categoryCombo, loadBtn, addBtn, updateBtn, deleteBtn);
        return formBox;
    }

    private VBox createUsersTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        HBox mainBox = new HBox(20);

        // User Table
        VBox tableBox = new VBox(10);
        tableBox.setStyle("-fx-background-color: white; -fx-background-radius: 10px; -fx-padding: 15;");
        HBox.setHgrow(tableBox, Priority.ALWAYS);

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.1));
        shadow.setRadius(10);
        tableBox.setEffect(shadow);

        Label tableTitle = new Label("User List");
        tableTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        userTable = new TableView<>();
        VBox.setVgrow(userTable, Priority.ALWAYS);

        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameCol.setPrefWidth(150);

        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setPrefWidth(100);

        userTable.getColumns().addAll(usernameCol, roleCol);
        refreshUserTable();

        tableBox.getChildren().addAll(tableTitle, userTable);

        // User Form
        VBox formBox = createUserForm();

        mainBox.getChildren().addAll(tableBox, formBox);
        VBox.setVgrow(mainBox, Priority.ALWAYS);
        content.getChildren().add(mainBox);
        return content;
    }

    private VBox createUserForm() {
        VBox formBox = new VBox(12);
        formBox.setPadding(new Insets(20));
        formBox.setPrefWidth(280);
        formBox.setStyle("-fx-background-color: white; -fx-background-radius: 10px;");

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.1));
        shadow.setRadius(10);
        formBox.setEffect(shadow);

        Label formTitle = new Label("User Form");
        formTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        styleTextField(usernameField);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        styleTextField(passwordField);

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("ADMIN", "SELLER", "BUYER");
        roleCombo.setPromptText("Select Role");
        roleCombo.setMaxWidth(Double.MAX_VALUE);

        Button addBtn = new Button("Add User");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setStyle(
                "-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");
        addBtn.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            String role = roleCombo.getValue();

            if (username.isEmpty() || password.isEmpty() || role == null) {
                showAlert("Please fill all fields!", Alert.AlertType.WARNING);
                return;
            }
            if (DataStore.getInstance().usernameExists(username)) {
                showAlert("Username already exists!", Alert.AlertType.ERROR);
                return;
            }

            DataStore.getInstance().addUser(new User(username, password, role));
            refreshUserTable();
            usernameField.clear();
            passwordField.clear();
            roleCombo.setValue(null);
            showAlert("User added successfully!", Alert.AlertType.INFORMATION);
        });

        Button deleteBtn = new Button("Delete Selected");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        deleteBtn.setStyle(
                "-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");
        deleteBtn.setOnAction(e -> {
            User selected = userTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (selected.getUsername().equals(user.getUsername())) {
                    showAlert("Cannot delete yourself!", Alert.AlertType.ERROR);
                    return;
                }
                DataStore.getInstance().removeUser(selected);
                refreshUserTable();
                showAlert("User deleted!", Alert.AlertType.INFORMATION);
            }
        });

        formBox.getChildren().addAll(formTitle, usernameField, passwordField, roleCombo, addBtn, deleteBtn);
        return formBox;
    }

    private VBox createStatsTab() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Sales Statistics");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Stats cards
        HBox statsRow = new HBox(30);
        statsRow.setAlignment(Pos.CENTER);

        double totalSales = DataStore.getInstance().getTotalSales();
        int deliveredOrders = DataStore.getInstance().getTotalOrdersDelivered();
        int pendingOrders = DataStore.getInstance().getPendingOrdersCount();
        int totalProducts = DataStore.getInstance().getProducts().size();
        int totalUsers = DataStore.getInstance().getUsers().size();

        statsRow.getChildren().addAll(
                createBigStatCard("Total Sales", "Rp " + String.format("%,.0f", totalSales), "#28a745"),
                createBigStatCard("Delivered", String.valueOf(deliveredOrders), "#17a2b8"),
                createBigStatCard("Pending", String.valueOf(pendingOrders), "#ffc107"),
                createBigStatCard("Products", String.valueOf(totalProducts), "#6c757d"),
                createBigStatCard("Users", String.valueOf(totalUsers), "#764ba2"));

        Button refreshBtn = new Button("Refresh Statistics");
        refreshBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 8px; -fx-padding: 10 25;");
        refreshBtn.setOnAction(e -> {
            // Refresh by recreating the tab
            tabPane.getTabs().set(2, new Tab("Statistics", createStatsTab()));
            tabPane.getSelectionModel().select(2);
        });

        content.getChildren().addAll(title, statsRow, refreshBtn);
        return content;
    }

    private VBox createBigStatCard(String title, String value, String color) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(25, 35, 25, 35));
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15px;");

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.15));
        shadow.setRadius(15);
        card.setEffect(shadow);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

        card.getChildren().addAll(valueLabel, titleLabel);
        return card;
    }

    private void styleTextField(TextField field) {
        field.setStyle("-fx-background-radius: 5px; -fx-border-radius: 5px; -fx-border-color: #ddd; -fx-padding: 8;");
    }

    private void refreshProductTable() {
        productTable.setItems(FXCollections.observableArrayList(DataStore.getInstance().getProducts()));
    }

    private void refreshUserTable() {
        userTable.setItems(FXCollections.observableArrayList(DataStore.getInstance().getUsers()));
    }

    private void clearFields(TextField... fields) {
        for (TextField f : fields) {
            f.clear();
        }
    }

    private void showAlert(String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Admin Dashboard");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}
