package data;

import model.Product;
import model.User;
import model.Order;
import model.Category;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DataStore {
    private static DataStore instance;
    private List<User> users;
    private List<Product> products;
    private List<Order> orders;

    private static final String DATA_DIR = "data";
    private static final String USERS_FILE = DATA_DIR + "/users.json";
    private static final String PRODUCTS_FILE = DATA_DIR + "/products.json";
    private static final String ORDERS_FILE = DATA_DIR + "/orders.json";

    private DataStore() {
        users = new ArrayList<>();
        products = new ArrayList<>();
        orders = new ArrayList<>();

        // Create data directory if not exists
        new File(DATA_DIR).mkdirs();

        // Try to load existing data, otherwise seed
        if (!loadAllData()) {
            seedData();
            saveAllData();
        }
    }

    public static DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    // ==================== Data Persistence (JSON) ====================

    private boolean loadAllData() {
        try {
            File usersFile = new File(USERS_FILE);
            File productsFile = new File(PRODUCTS_FILE);
            File ordersFile = new File(ORDERS_FILE);

            if (!usersFile.exists() || !productsFile.exists()) {
                return false; // No saved data, need to seed
            }

            // Load users
            users = JsonHelper.readUsers(USERS_FILE);

            // Load products
            products = JsonHelper.readProducts(PRODUCTS_FILE);

            // Load orders (may not exist yet)
            if (ordersFile.exists()) {
                orders = JsonHelper.readOrders(ORDERS_FILE, products);
            }

            System.out.println("Data loaded from JSON files successfully!");
            return true;
        } catch (Exception e) {
            System.out.println("Could not load data: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void saveAllData() {
        try {
            // Save users
            JsonHelper.writeUsers(users, USERS_FILE);

            // Save products
            JsonHelper.writeProducts(products, PRODUCTS_FILE);

            // Save orders
            JsonHelper.writeOrders(orders, ORDERS_FILE);

            System.out.println("Data saved to JSON files!");
        } catch (Exception e) {
            System.err.println("Error saving data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== Seed Data ====================

    private void seedData() {
        // Users
        users.add(new User("admin", "admin", "ADMIN"));
        users.add(new User("seller", "seller", "SELLER"));
        users.add(new User("buyer", "buyer", "BUYER"));

        // Products with categories
        products.add(
                new Product("P001", "Nasi Goreng", 15000, "Nasi goreng spesial dengan telur", 20, Category.MAKANAN));
        products.add(new Product("P002", "Mie Goreng", 12000, "Mie goreng pedas manis", 15, Category.MAKANAN));
        products.add(new Product("P003", "Ayam Bakar", 25000, "Ayam bakar bumbu rujak", 10, Category.MAKANAN));
        products.add(new Product("P004", "Es Teh Manis", 5000, "Teh manis dingin segar", 50, Category.MINUMAN));
        products.add(new Product("P005", "Es Jeruk", 7000, "Jeruk peras segar", 40, Category.MINUMAN));
        products.add(new Product("P006", "Kopi Susu", 12000, "Kopi susu gula aren", 30, Category.MINUMAN));
        products.add(new Product("P007", "Kentang Goreng", 10000, "Kentang goreng krispy", 25, Category.SNACK));
        products.add(new Product("P008", "Pisang Goreng", 8000, "Pisang goreng keju coklat", 20, Category.SNACK));

        System.out.println("Initial data seeded!");
    }

    // ==================== Authentication ====================

    public User authenticate(String username, String password) {
        return users.stream()
                .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
                .findFirst()
                .orElse(null);
    }

    // ==================== Product Methods ====================

    public List<Product> getProducts() {
        return products;
    }

    public List<Product> getProductsByCategory(Category category) {
        List<Product> result = new ArrayList<>();
        for (Product p : products) {
            if (p.getCategory() == category) {
                result.add(p);
            }
        }
        return result;
    }

    public List<Product> searchProducts(String keyword) {
        List<Product> result = new ArrayList<>();
        String lower = keyword.toLowerCase();
        for (Product p : products) {
            if (p.getName().toLowerCase().contains(lower) ||
                    p.getDescription().toLowerCase().contains(lower)) {
                result.add(p);
            }
        }
        return result;
    }

    public void addProduct(Product p) {
        products.add(p);
        saveAllData();
    }

    public void removeProduct(Product p) {
        products.remove(p);
        saveAllData();
    }

    public void updateProduct(Product oldProduct, Product newProduct) {
        int index = products.indexOf(oldProduct);
        if (index >= 0) {
            products.set(index, newProduct);
            saveAllData();
        }
    }

    public void updateProductStock(String productId, int quantitySold) {
        Optional<Product> p = products.stream().filter(prod -> prod.getId().equals(productId)).findFirst();
        p.ifPresent(product -> {
            product.setStock(product.getStock() - quantitySold);
            saveAllData();
        });
    }

    // ==================== Order Methods ====================

    public List<Order> getOrders() {
        return orders;
    }

    public void addOrder(Order o) {
        orders.add(o);
        saveAllData();
    }

    public void updateOrderStatus() {
        saveAllData();
    }

    // ==================== User Management ====================

    public List<User> getUsers() {
        return users;
    }

    public void addUser(User u) {
        users.add(u);
        saveAllData();
    }

    public void removeUser(User u) {
        users.remove(u);
        saveAllData();
    }

    public void updateUser(User oldUser, User newUser) {
        int index = users.indexOf(oldUser);
        if (index >= 0) {
            users.set(index, newUser);
            saveAllData();
        }
    }

    public boolean usernameExists(String username) {
        return users.stream().anyMatch(u -> u.getUsername().equals(username));
    }

    // ==================== Statistics ====================

    public double getTotalSales() {
        return orders.stream()
                .filter(o -> o.getStatus() == model.OrderStatus.DELIVERED)
                .mapToDouble(Order::getTotalAmount)
                .sum();
    }

    public int getTotalOrdersDelivered() {
        return (int) orders.stream()
                .filter(o -> o.getStatus() == model.OrderStatus.DELIVERED)
                .count();
    }

    public int getPendingOrdersCount() {
        return (int) orders.stream()
                .filter(o -> o.getStatus() == model.OrderStatus.PENDING)
                .count();
    }
}
