package view;

import data.DataStore;
import model.Order;
import model.OrderStatus;
import model.User;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.stream.Collectors;

public class SellerDashboard {
    private Stage stage;
    private User user;
    private TableView<Order> table;
    private Label statsLabel;
    private Timeline autoRefresh;

    public SellerDashboard(Stage stage, User user) {
        this.stage = stage;
        this.user = user;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f8f9fa;");

        // Header
        HBox header = createHeader();
        root.setTop(header);

        // Content
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Stats cards
        HBox statsBox = createStatsBox();
        content.getChildren().add(statsBox);

        // Table
        VBox tableBox = createTableBox();
        VBox.setVgrow(tableBox, Priority.ALWAYS);
        content.getChildren().add(tableBox);

        root.setCenter(content);

        // Start auto-refresh
        startAutoRefresh();

        Scene scene = new Scene(root, 1000, 700);
        stage.setScene(scene);
        stage.setTitle("Seller Dashboard - Kitchen");
        stage.setOnCloseRequest(e -> stopAutoRefresh());
    }

    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: linear-gradient(to right, #ff7e5f, #feb47b);");

        Label logo = new Label("Kitchen Dashboard");
        logo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label userLabel = new Label("Seller: " + user.getUsername());
        userLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-background-radius: 20px;");
        refreshBtn.setOnAction(e -> refreshTable());

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle(
                "-fx-background-color: white; -fx-text-fill: #ff7e5f; -fx-font-weight: bold; -fx-background-radius: 20px;");
        logoutBtn.setOnAction(e -> {
            stopAutoRefresh();
            new LoginView(stage).show();
        });

        header.getChildren().addAll(logo, userLabel, spacer, refreshBtn, logoutBtn);
        return header;
    }

    private HBox createStatsBox() {
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER_LEFT);

        int pending = (int) DataStore.getInstance().getOrders().stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING).count();
        int preparing = (int) DataStore.getInstance().getOrders().stream()
                .filter(o -> o.getStatus() == OrderStatus.PREPARING).count();
        int delivered = (int) DataStore.getInstance().getOrders().stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();

        statsBox.getChildren().addAll(
                createStatCard("Pending", String.valueOf(pending), "#ffc107"),
                createStatCard("Preparing", String.valueOf(preparing), "#17a2b8"),
                createStatCard("Delivered", String.valueOf(delivered), "#28a745"));

        return statsBox;
    }

    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(15, 25, 15, 25));
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10px;");

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.1));
        shadow.setRadius(10);
        card.setEffect(shadow);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        card.getChildren().addAll(valueLabel, titleLabel);
        return card;
    }

    private VBox createTableBox() {
        VBox tableBox = new VBox(10);
        tableBox.setStyle("-fx-background-color: white; -fx-background-radius: 10px; -fx-padding: 15;");

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.1));
        shadow.setRadius(10);
        tableBox.setEffect(shadow);

        Label tableTitle = new Label("Order Queue");
        tableTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        table = new TableView<>();
        table.setStyle("-fx-background-radius: 8px;");
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Order, String> idCol = new TableColumn<>("Order ID");
        idCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOrderId()));
        idCol.setPrefWidth(100);

        TableColumn<Order, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRoomName()));
        roomCol.setPrefWidth(80);

        TableColumn<Order, String> itemsCol = new TableColumn<>("Items");
        itemsCol.setCellValueFactory(cellData -> {
            String items = cellData.getValue().getItems().stream()
                    .map(i -> i.getProduct().getName() + " x" + i.getQuantity())
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(items);
        });
        itemsCol.setPrefWidth(300);

        TableColumn<Order, String> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                "Rp " + String.format("%,.0f", cellData.getValue().getTotalAmount())));
        totalCol.setPrefWidth(100);

        TableColumn<Order, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatusDisplay()));
        statusCol.setCellFactory(col -> new TableCell<Order, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    Order order = getTableView().getItems().get(getIndex());
                    setStyle("-fx-background-color: " + order.getStatusColor() +
                            "; -fx-text-fill: white; -fx-font-weight: bold; -fx-alignment: center;");
                }
            }
        });
        statusCol.setPrefWidth(100);

        table.getColumns().addAll(idCol, roomCol, itemsCol, totalCol, statusCol);
        refreshTable();

        // Action buttons
        HBox actions = new HBox(15);
        actions.setPadding(new Insets(10, 0, 0, 0));
        actions.setAlignment(Pos.CENTER_LEFT);

        Button prepareBtn = new Button("Start Preparing");
        prepareBtn.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 8px; -fx-padding: 10 20;");
        prepareBtn.setOnAction(e -> updateOrderStatus(OrderStatus.PREPARING));

        Button deliverBtn = new Button("Mark Delivered");
        deliverBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 8px; -fx-padding: 10 20;");
        deliverBtn.setOnAction(e -> updateOrderStatus(OrderStatus.DELIVERED));

        Button cancelBtn = new Button("Cancel Order");
        cancelBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 8px; -fx-padding: 10 20;");
        cancelBtn.setOnAction(e -> updateOrderStatus(OrderStatus.CANCELLED));

        Button detailBtn = new Button("View Detail");
        detailBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 8px; -fx-padding: 10 20;");
        detailBtn.setOnAction(e -> showOrderDetail());

        actions.getChildren().addAll(prepareBtn, deliverBtn, cancelBtn, detailBtn);

        tableBox.getChildren().addAll(tableTitle, table, actions);
        return tableBox;
    }

    private void updateOrderStatus(OrderStatus newStatus) {
        Order selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (newStatus == OrderStatus.PREPARING && selected.getStatus() != OrderStatus.PENDING) {
                showAlert("Can only prepare PENDING orders!", Alert.AlertType.WARNING);
                return;
            }
            if (newStatus == OrderStatus.DELIVERED && selected.getStatus() != OrderStatus.PREPARING) {
                showAlert("Can only deliver orders that are PREPARING!", Alert.AlertType.WARNING);
                return;
            }
            selected.setStatus(newStatus);
            DataStore.getInstance().saveAllData();
            refreshTable();
            show(); // Refresh stats
        } else {
            showAlert("Please select an order first!", Alert.AlertType.WARNING);
        }
    }

    private void showOrderDetail() {
        Order selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select an order first!", Alert.AlertType.WARNING);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Order Detail");
        alert.setHeaderText("Order #" + selected.getOrderId());

        StringBuilder content = new StringBuilder();
        content.append("Room: ").append(selected.getRoomName()).append("\n");
        content.append("Status: ").append(selected.getStatusDisplay()).append("\n");
        content.append("Buyer: ").append(selected.getBuyerUsername()).append("\n\n");
        content.append("Items:\n");
        for (var item : selected.getItems()) {
            content.append("  • ").append(item.getProduct().getName())
                    .append(" x").append(item.getQuantity())
                    .append(" = Rp ").append(String.format("%,.0f", item.getTotal()))
                    .append("\n");
        }
        content.append("\nTotal: Rp ").append(String.format("%,.0f", selected.getTotalAmount()));

        alert.setContentText(content.toString());
        alert.showAndWait();
    }

    private void refreshTable() {
        table.setItems(FXCollections.observableArrayList(DataStore.getInstance().getOrders()));
    }

    private void startAutoRefresh() {
        autoRefresh = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
            refreshTable();
        }));
        autoRefresh.setCycleCount(Timeline.INDEFINITE);
        autoRefresh.play();
    }

    private void stopAutoRefresh() {
        if (autoRefresh != null) {
            autoRefresh.stop();
        }
    }

    private void showAlert(String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Kitchen Dashboard");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}
