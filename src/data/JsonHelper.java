package data;

import model.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Simple JSON helper class for reading/writing data without external libraries.
 * Provides basic JSON serialization for the cafe application.
 */
public class JsonHelper {

    // ==================== WRITE METHODS ====================

    public static void writeUsers(List<User> users, String filePath) throws IOException {
        StringBuilder json = new StringBuilder();
        json.append("[\n");
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            json.append("  {\n");
            json.append("    \"username\": \"").append(escapeJson(u.getUsername())).append("\",\n");
            json.append("    \"password\": \"").append(escapeJson(u.getPassword())).append("\",\n");
            json.append("    \"role\": \"").append(escapeJson(u.getRole())).append("\"\n");
            json.append("  }");
            if (i < users.size() - 1)
                json.append(",");
            json.append("\n");
        }
        json.append("]");
        Files.writeString(Path.of(filePath), json.toString());
    }

    public static void writeProducts(List<Product> products, String filePath) throws IOException {
        StringBuilder json = new StringBuilder();
        json.append("[\n");
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            json.append("  {\n");
            json.append("    \"id\": \"").append(escapeJson(p.getId())).append("\",\n");
            json.append("    \"name\": \"").append(escapeJson(p.getName())).append("\",\n");
            json.append("    \"price\": ").append(p.getPrice()).append(",\n");
            json.append("    \"description\": \"").append(escapeJson(p.getDescription())).append("\",\n");
            json.append("    \"stock\": ").append(p.getStock()).append(",\n");
            json.append("    \"category\": \"").append(p.getCategory().name()).append("\"\n");
            json.append("  }");
            if (i < products.size() - 1)
                json.append(",");
            json.append("\n");
        }
        json.append("]");
        Files.writeString(Path.of(filePath), json.toString());
    }

    public static void writeOrders(List<Order> orders, String filePath) throws IOException {
        StringBuilder json = new StringBuilder();
        json.append("[\n");
        for (int i = 0; i < orders.size(); i++) {
            Order o = orders.get(i);
            json.append("  {\n");
            json.append("    \"orderId\": \"").append(escapeJson(o.getOrderId())).append("\",\n");
            json.append("    \"buyerUsername\": \"").append(escapeJson(o.getBuyerUsername())).append("\",\n");
            json.append("    \"roomName\": \"").append(escapeJson(o.getRoomName())).append("\",\n");
            json.append("    \"totalAmount\": ").append(o.getTotalAmount()).append(",\n");
            json.append("    \"status\": \"").append(o.getStatus().name()).append("\",\n");
            json.append("    \"createdTime\": ").append(o.getCreatedTime()).append(",\n");
            json.append("    \"items\": [\n");
            List<CartItem> items = o.getItems();
            for (int j = 0; j < items.size(); j++) {
                CartItem item = items.get(j);
                json.append("      {\n");
                json.append("        \"productId\": \"").append(escapeJson(item.getProduct().getId())).append("\",\n");
                json.append("        \"productName\": \"").append(escapeJson(item.getProduct().getName()))
                        .append("\",\n");
                json.append("        \"productPrice\": ").append(item.getProduct().getPrice()).append(",\n");
                json.append("        \"quantity\": ").append(item.getQuantity()).append("\n");
                json.append("      }");
                if (j < items.size() - 1)
                    json.append(",");
                json.append("\n");
            }
            json.append("    ]\n");
            json.append("  }");
            if (i < orders.size() - 1)
                json.append(",");
            json.append("\n");
        }
        json.append("]");
        Files.writeString(Path.of(filePath), json.toString());
    }

    // ==================== READ METHODS ====================

    public static List<User> readUsers(String filePath) throws IOException {
        List<User> users = new ArrayList<>();
        String content = Files.readString(Path.of(filePath));
        List<Map<String, String>> items = parseJsonArray(content);
        for (Map<String, String> item : items) {
            users.add(new User(
                    item.get("username"),
                    item.get("password"),
                    item.get("role")));
        }
        return users;
    }

    public static List<Product> readProducts(String filePath) throws IOException {
        List<Product> products = new ArrayList<>();
        String content = Files.readString(Path.of(filePath));
        List<Map<String, String>> items = parseJsonArray(content);
        for (Map<String, String> item : items) {
            products.add(new Product(
                    item.get("id"),
                    item.get("name"),
                    Double.parseDouble(item.get("price")),
                    item.get("description"),
                    Integer.parseInt(item.get("stock")),
                    Category.valueOf(item.get("category"))));
        }
        return products;
    }

    public static List<Order> readOrders(String filePath, List<Product> products) throws IOException {
        List<Order> orders = new ArrayList<>();
        String content = Files.readString(Path.of(filePath));

        // Simple parsing for orders with nested items
        List<String> orderBlocks = splitOrderBlocks(content);
        for (String block : orderBlocks) {
            Map<String, String> orderData = parseJsonObject(block);

            // Parse items
            List<CartItem> cartItems = new ArrayList<>();
            String itemsSection = extractItemsArray(block);
            if (itemsSection != null && !itemsSection.isEmpty()) {
                List<Map<String, String>> itemsList = parseJsonArray("[" + itemsSection + "]");
                for (Map<String, String> itemData : itemsList) {
                    // Find or create product
                    Product product = findProductById(products, itemData.get("productId"));
                    if (product == null) {
                        // Create a placeholder product if not found
                        product = new Product(
                                itemData.get("productId"),
                                itemData.get("productName"),
                                Double.parseDouble(itemData.getOrDefault("productPrice", "0")),
                                "",
                                0,
                                Category.MAKANAN);
                    }
                    int quantity = Integer.parseInt(itemData.get("quantity"));
                    cartItems.add(new CartItem(product, quantity));
                }
            }

            Order order = new Order(
                    orderData.get("buyerUsername"),
                    orderData.get("roomName"),
                    cartItems);
            // Set saved values
            order.setStatus(OrderStatus.valueOf(orderData.get("status")));
            orders.add(order);
        }
        return orders;
    }

    // ==================== HELPER METHODS ====================

    private static String escapeJson(String s) {
        if (s == null)
            return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static List<Map<String, String>> parseJsonArray(String json) {
        List<Map<String, String>> result = new ArrayList<>();
        json = json.trim();
        if (!json.startsWith("["))
            return result;

        int depth = 0;
        int start = -1;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') {
                if (depth == 1)
                    start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 1 && start >= 0) {
                    String obj = json.substring(start, i + 1);
                    result.add(parseJsonObject(obj));
                    start = -1;
                }
            } else if (c == '[') {
                depth++;
            } else if (c == ']') {
                depth--;
            }
        }
        return result;
    }

    private static Map<String, String> parseJsonObject(String json) {
        Map<String, String> map = new HashMap<>();
        json = json.trim();
        if (!json.startsWith("{"))
            return map;

        // Remove outer braces
        json = json.substring(1, json.lastIndexOf('}'));

        // Simple key-value parsing (doesn't handle nested objects well)
        String[] lines = json.split(",\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("\"items\""))
                continue;

            int colonPos = line.indexOf(':');
            if (colonPos > 0) {
                String key = line.substring(0, colonPos).trim().replace("\"", "");
                String value = line.substring(colonPos + 1).trim();
                // Remove trailing comma
                if (value.endsWith(","))
                    value = value.substring(0, value.length() - 1);
                // Remove quotes
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                map.put(key, value);
            }
        }
        return map;
    }

    private static List<String> splitOrderBlocks(String content) {
        List<String> blocks = new ArrayList<>();
        int depth = 0;
        int start = -1;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') {
                if (depth == 1)
                    start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 1 && start >= 0) {
                    blocks.add(content.substring(start, i + 1));
                    start = -1;
                }
            } else if (c == '[') {
                depth++;
            } else if (c == ']') {
                depth--;
            }
        }
        return blocks;
    }

    private static String extractItemsArray(String orderBlock) {
        int itemsStart = orderBlock.indexOf("\"items\"");
        if (itemsStart < 0)
            return null;

        int bracketStart = orderBlock.indexOf('[', itemsStart);
        if (bracketStart < 0)
            return null;

        int depth = 0;
        for (int i = bracketStart; i < orderBlock.length(); i++) {
            char c = orderBlock.charAt(i);
            if (c == '[')
                depth++;
            else if (c == ']') {
                depth--;
                if (depth == 0) {
                    return orderBlock.substring(bracketStart + 1, i);
                }
            }
        }
        return null;
    }

    private static Product findProductById(List<Product> products, String id) {
        for (Product p : products) {
            if (p.getId().equals(id))
                return p;
        }
        return null;
    }
}
