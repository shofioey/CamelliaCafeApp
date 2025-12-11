package view;

import data.DataStore;
import model.CartItem;
import model.Category;
import model.Order;
import model.OrderStatus;
import model.Product;
import model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BuyerDashboard {
    private Stage stage;
    private User user;
    private List<CartItem> cart;
    private ListView<String> cartListView;
    private ListView<String> orderHistoryListView;
    private Label totalLabel;
    private FlowPane productContainer;
    private TextField searchField;
    private Category selectedCategory = null;

    public BuyerDashboard(Stage stage, User user) {
        this.stage = stage;
        this.user = user;
        this.cart = new ArrayList<>();
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f8f9fa;");

        // Header
        HBox header = createHeader();
        root.setTop(header);

        // Center: Category tabs + Products
        VBox centerContent = new VBox(10);
        centerContent.setPadding(new Insets(10));

        // Search bar
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchField = new TextField();
        searchField.setPromptText("Search products...");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-background-radius: 20px; -fx-padding: 8 15;");
        searchField.textProperty().addListener((obs, old, newVal) -> filterProducts());

        Button clearSearchBtn = new Button("Clear");
        clearSearchBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-background-radius: 15px;");
        clearSearchBtn.setOnAction(e -> {
            searchField.clear();
            selectedCategory = null;
            filterProducts();
        });
        searchBox.getChildren().addAll(searchField, clearSearchBtn);

        // Category tabs
        HBox categoryTabs = new HBox(10);
        categoryTabs.setAlignment(Pos.CENTER_LEFT);
        categoryTabs.setPadding(new Insets(5, 0, 5, 0));

        Button allBtn = createCategoryButton("All", null);
        Button makananBtn = createCategoryButton("Makanan", Category.MAKANAN);
        Button minumanBtn = createCategoryButton("Minuman", Category.MINUMAN);
        Button snackBtn = createCategoryButton("Snack", Category.SNACK);
        categoryTabs.getChildren().addAll(allBtn, makananBtn, minumanBtn, snackBtn);

        // Product grid
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setStyle("-fx-background: #f8f9fa; -fx-background-color: transparent;");
        productContainer = new FlowPane();
        productContainer.setPadding(new Insets(10));
        productContainer.setHgap(15);
        productContainer.setVgap(15);

        filterProducts();

        scrollPane.setContent(productContainer);
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        centerContent.getChildren().addAll(searchBox, categoryTabs, scrollPane);
        root.setCenter(centerContent);

        // Right: Cart + Order History
        VBox rightPanel = createRightPanel();
        root.setRight(rightPanel);

        Scene scene = new Scene(root, 1100, 700);
        stage.setScene(scene);
        stage.setTitle("Camellia Cafe - Order");
    }

    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2);");

        Label logo = new Label("Camellia Cafe");
        logo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label userLabel = new Label("Welcome, " + user.getUsername());
        userLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-background-radius: 20px;");
        refreshBtn.setOnAction(e -> {
            refreshOrderHistory();
            filterProducts();
        });

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle(
                "-fx-background-color: white; -fx-text-fill: #764ba2; -fx-font-weight: bold; -fx-background-radius: 20px;");
        logoutBtn.setOnAction(e -> new LoginView(stage).show());

        header.getChildren().addAll(logo, userLabel, spacer, refreshBtn, logoutBtn);
        return header;
    }

    private Button createCategoryButton(String text, Category category) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + (selectedCategory == category ? "#667eea" : "#e9ecef") +
                "; -fx-text-fill: " + (selectedCategory == category ? "white" : "#333") +
                "; -fx-background-radius: 20px; -fx-padding: 8 20;");
        btn.setOnAction(e -> {
            selectedCategory = category;
            filterProducts();
            show(); // Refresh to update button styles
        });
        return btn;
    }

    private void filterProducts() {
        productContainer.getChildren().clear();
        List<Product> products = DataStore.getInstance().getProducts();

        String search = searchField != null ? searchField.getText().toLowerCase() : "";

        for (Product p : products) {
            boolean matchesCategory = selectedCategory == null || p.getCategory() == selectedCategory;
            boolean matchesSearch = search.isEmpty() ||
                    p.getName().toLowerCase().contains(search) ||
                    p.getDescription().toLowerCase().contains(search);

            if (matchesCategory && matchesSearch) {
                productContainer.getChildren().add(createProductCard(p));
            }
        }
    }

    private VBox createProductCard(Product p) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.setPrefWidth(180);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10px;");

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.1));
        shadow.setRadius(10);
        card.setEffect(shadow);

        // Category badge
        Label categoryBadge = new Label(p.getCategory().getDisplayName());
        categoryBadge.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; " +
                "-fx-padding: 3 10; -fx-background-radius: 10px; -fx-font-size: 10px;");

        Label nameLabel = new Label(p.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        nameLabel.setWrapText(true);

        Label descLabel = new Label(p.getDescription());
        descLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");
        descLabel.setWrapText(true);

        Label priceLabel = new Label("Rp " + String.format("%,.0f", p.getPrice()));
        priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #28a745;");

        Label stockLabel = new Label("Stock: " + p.getStock());
        stockLabel.setStyle("-fx-text-fill: " + (p.getStock() > 5 ? "#666" : "#dc3545") + "; -fx-font-size: 11px;");

        Spinner<Integer> qtySpinner = new Spinner<>(1, Math.max(1, p.getStock()), 1);
        qtySpinner.setPrefWidth(70);
        qtySpinner.setEditable(true);

        Button addBtn = new Button("+ Add to Cart");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-background-radius: 5px;");
        addBtn.setDisable(p.getStock() <= 0);
        addBtn.setOnAction(e -> addToCart(p, qtySpinner.getValue()));

        card.getChildren().addAll(categoryBadge, nameLabel, descLabel, priceLabel, stockLabel, qtySpinner, addBtn);
        return card;
    }

    private VBox createRightPanel() {
        VBox rightPanel = new VBox(15);
        rightPanel.setPadding(new Insets(15));
        rightPanel.setPrefWidth(320);
        rightPanel.setStyle("-fx-background-color: white;");

        // Cart Section
        VBox cartBox = new VBox(10);
        Label cartTitle = new Label("Your Cart");
        cartTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        cartListView = new ListView<>();
        cartListView.setPrefHeight(150);
        cartListView.setStyle("-fx-background-radius: 8px;");

        HBox cartActions = new HBox(10);
        Button removeBtn = new Button("Remove Selected");
        removeBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-background-radius: 5px;");
        removeBtn.setOnAction(e -> removeFromCart());

        Button clearCartBtn = new Button("Clear All");
        clearCartBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-background-radius: 5px;");
        clearCartBtn.setOnAction(e -> {
            cart.clear();
            refreshCart();
        });
        cartActions.getChildren().addAll(removeBtn, clearCartBtn);

        totalLabel = new Label("Total: Rp 0");
        totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #28a745;");

        TextField roomField = new TextField();
        roomField.setPromptText("Room Number (e.g., 101)");
        roomField.setStyle("-fx-background-radius: 8px; -fx-padding: 10;");

        Button checkoutBtn = new Button("✓ Checkout");
        checkoutBtn.setMaxWidth(Double.MAX_VALUE);
        checkoutBtn.setPrefHeight(40);
        checkoutBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-font-size: 14px; -fx-background-radius: 8px;");
        checkoutBtn.setOnAction(e -> processCheckout(roomField.getText()));

        cartBox.getChildren().addAll(cartTitle, cartListView, cartActions, totalLabel, roomField, checkoutBtn);

        // Separator
        Separator sep = new Separator();

        // Order History Section
        VBox orderHistoryBox = new VBox(10);
        HBox orderHistoryHeader = new HBox(10);
        orderHistoryHeader.setAlignment(Pos.CENTER_LEFT);
        Label orderHistoryTitle = new Label("Order History");
        orderHistoryTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshHistoryBtn = new Button("🔄");
        refreshHistoryBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-background-radius: 15px;");
        refreshHistoryBtn.setOnAction(e -> refreshOrderHistory());
        orderHistoryHeader.getChildren().addAll(orderHistoryTitle, spacer, refreshHistoryBtn);

        orderHistoryListView = new ListView<>();
        orderHistoryListView.setPrefHeight(200);
        VBox.setVgrow(orderHistoryListView, Priority.ALWAYS);

        orderHistoryBox.getChildren().addAll(orderHistoryHeader, orderHistoryListView);

        rightPanel.getChildren().addAll(cartBox, sep, orderHistoryBox);

        refreshOrderHistory();
        return rightPanel;
    }

    private void addToCart(Product p, int qty) {
        if (p.getStock() < qty) {
            showAlert("Not enough stock!", Alert.AlertType.WARNING);
            return;
        }

        for (CartItem item : cart) {
            if (item.getProduct().getId().equals(p.getId())) {
                item.setQuantity(item.getQuantity() + qty);
                refreshCart();
                return;
            }
        }

        cart.add(new CartItem(p, qty));
        refreshCart();
    }

    private void removeFromCart() {
        int selectedIndex = cartListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < cart.size()) {
            cart.remove(selectedIndex);
            refreshCart();
        } else {
            showAlert("Please select an item to remove", Alert.AlertType.WARNING);
        }
    }

    private void refreshCart() {
        cartListView.getItems().clear();
        double total = 0;
        for (CartItem item : cart) {
            cartListView.getItems().add(
                    item.getProduct().getName() + " x" + item.getQuantity() +
                            " = Rp " + String.format("%,.0f", item.getTotal()));
            total += item.getTotal();
        }
        totalLabel.setText("Total: Rp " + String.format("%,.0f", total));
    }

    private void refreshOrderHistory() {
        orderHistoryListView.getItems().clear();
        List<Order> myOrders = DataStore.getInstance().getOrders().stream()
                .filter(o -> o.getBuyerUsername().equals(user.getUsername()))
                .collect(Collectors.toList());

        if (myOrders.isEmpty()) {
            orderHistoryListView.getItems().add("No orders yet");
        } else {
            for (Order order : myOrders) {
                String statusIcon = getStatusIcon(order.getStatus());
                String items = order.getItems().stream()
                        .map(i -> i.getProduct().getName() + " x" + i.getQuantity())
                        .collect(Collectors.joining(", "));
                orderHistoryListView.getItems().add(
                        statusIcon + " " + order.getOrderId() + " | Room: " + order.getRoomName() +
                                " | " + order.getStatusDisplay() + "\n   " + items);
            }
        }
    }

    private String getStatusIcon(OrderStatus status) {
        switch (status) {
            case PENDING:
                return "[P]";
            case PREPARING:
                return "[K]";
            case DELIVERED:
                return "[✓]";
            case CANCELLED:
                return "[X]";
            default:
                return "[?]";
        }
    }

    private void processCheckout(String roomName) {
        if (cart.isEmpty()) {
            showAlert("Cart is empty!", Alert.AlertType.WARNING);
            return;
        }
        if (roomName == null || roomName.trim().isEmpty()) {
            showAlert("Please enter room number!", Alert.AlertType.WARNING);
            return;
        }

        Order order = new Order(user.getUsername(), roomName, new ArrayList<>(cart));
        DataStore.getInstance().addOrder(order);

        for (CartItem item : cart) {
            DataStore.getInstance().updateProductStock(item.getProduct().getId(), item.getQuantity());
        }

        showAlert("Order placed successfully!\nOrder ID: " + order.getOrderId(), Alert.AlertType.INFORMATION);
        cart.clear();
        refreshCart();
        refreshOrderHistory();
        filterProducts();
    }

    private void showAlert(String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Camellia Cafe");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}
