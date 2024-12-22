package rmi;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.Product;
import dao.EmployeeDAO;
import models.Employee;
import javafx.geometry.Pos;
import java.rmi.Naming;
import java.sql.*;
import java.util.List;
import java.util.logging.*;

public class InventoryClientGUI extends Application {
    private static Connection connection;
    private TextField usernameField;
    private PasswordField passwordField;
    private Label statusLabel;
    private Button loginButton;

    private static Logger logger = Logger.getLogger(InventoryClientGUI.class.getName());

    static {
        try {
            // Configure the logger to write to a file with appending enabled
            FileHandler fileHandler = new FileHandler("inventory_operations.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Set up database connection
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/inventory_system", "root", "Rikiest123@");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Create the login UI components
        usernameField = new TextField();
        passwordField = new PasswordField();
        statusLabel = new Label("Please log in.");
        loginButton = new Button("Login");

        loginButton.setOnAction(event -> authenticateAndShowMainMenu(primaryStage));

        VBox loginLayout = new VBox(10, new Label("Username:"), usernameField, new Label("Password:"), passwordField, loginButton, statusLabel);
        loginLayout.setStyle("-fx-padding: 20;");
        loginLayout.setAlignment(Pos.CENTER); // Center the login components
        Scene loginScene = new Scene(loginLayout, 300, 200);

        primaryStage.setTitle("Inventory Client Login");
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    private void authenticateAndShowMainMenu(Stage primaryStage) {
        try {
            EmployeeDAO employeeDAO = new EmployeeDAO(connection);
            String username = usernameField.getText();
            String password = passwordField.getText();
            Employee employee = employeeDAO.authenticate(username, password);

            if (employee != null) {
                statusLabel.setText("Login successful! Welcome, " + employee.getUsername());

                // Log successful login
                logger.info("User " + username + " logged in successfully.");

                // Transition to the inventory menu
                showInventoryMenu(primaryStage, employee);
            } else {
                statusLabel.setText("Invalid username or password.");
                // Log failed login attempt
                logger.warning("Failed login attempt for username: " + username);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showInventoryMenu(Stage primaryStage, Employee employee) {
        // Create Inventory Menu buttons
        Button viewProductsButton = new Button("View All Products");
        Button addProductButton = new Button("Add a Product");
        Button updateProductButton = new Button("Update a Product");
        Button deleteProductButton = new Button("Delete a Product");
        Button exitButton = new Button("Exit");

        // Add action handlers
        if (employee.getRole().equalsIgnoreCase("admin")) {
            addProductButton.setOnAction(event -> {
                showAddProductWindow(primaryStage);
                logger.info("Admin " + employee.getUsername() + " opened Add Product window.");
            });
            updateProductButton.setOnAction(event -> {
                showUpdateProductWindow(primaryStage);
                logger.info("Admin " + employee.getUsername() + " opened Update Product window.");
            });
            deleteProductButton.setOnAction(event -> {
                showDeleteProductWindow(primaryStage);
                logger.info("Admin " + employee.getUsername() + " opened Delete Product window.");
            });
        }

        // Action for viewing products
        viewProductsButton.setOnAction(event -> viewAllProducts(employee));

        // Action for exiting
        exitButton.setOnAction(event -> {
            logger.info("User " + employee.getUsername() + " logged out.");
            System.exit(0);
        });

        // VBox layout to hold buttons, with dynamic resizing
        VBox menuLayout = new VBox(10, viewProductsButton, addProductButton, updateProductButton, deleteProductButton, exitButton);
        menuLayout.setStyle("-fx-padding: 20;");
        menuLayout.setAlignment(Pos.CENTER); // Center the buttons in the VBox

        Scene menuScene = new Scene(menuLayout, 300, 300);
        primaryStage.setScene(menuScene);
    }

    private void viewAllProducts(Employee employee) {
        try {
            InventoryService service = (InventoryService) Naming.lookup("rmi://localhost:1099/InventoryService");
            List<Product> products = service.getAllProducts();

            // Display products in a new window
            Stage productStage = new Stage();
            VBox productLayout = new VBox(10);
            productLayout.setStyle("-fx-padding: 20;");

            for (Product product : products) {
                productLayout.getChildren().add(new Label(
                        "ID: " + product.getId() + " | Name: " + product.getName() + " | Category: " +
                                product.getCategory() + " | Quantity: " + product.getQuantity() + " | Price: " + product.getPrice()));
            }

            Scene productScene = new Scene(productLayout, 400, 400);
            productStage.setScene(productScene);
            productStage.setTitle("Product List");
            productStage.show();

            logger.info("User " + employee.getUsername() + " viewed all products.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAddProductWindow(Stage primaryStage) {
        Stage addProductStage = new Stage();

        TextField nameField = new TextField();
        TextField categoryField = new TextField();
        TextField quantityField = new TextField();
        TextField priceField = new TextField();
        Button submitButton = new Button("Add Product");

        submitButton.setOnAction(event -> {
            try {
                InventoryService service = (InventoryService) Naming.lookup("rmi://localhost:1099/InventoryService");
                Product product = new Product();
                product.setName(nameField.getText());
                product.setCategory(categoryField.getText());
                product.setQuantity(Integer.parseInt(quantityField.getText()));
                product.setPrice(Double.parseDouble(priceField.getText()));

                service.addProduct(product);
                addProductStage.close();
                logger.info("Product added: " + product.getName() + " | Category: " + product.getCategory() + " | Quantity: " + product.getQuantity() + " | Price: " + product.getPrice());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        VBox layout = new VBox(10, new Label("Name:"), nameField, new Label("Category:"), categoryField, new Label("Quantity:"), quantityField, new Label("Price:"), priceField, submitButton);
        layout.setStyle("-fx-padding: 20;");
        layout.setAlignment(Pos.CENTER); // Centering the form fields
        Scene scene = new Scene(layout, 300, 300);

        addProductStage.setScene(scene);
        addProductStage.setTitle("Add Product");
        addProductStage.show();
    }

    private void showUpdateProductWindow(Stage primaryStage) {
        Stage updateProductStage = new Stage();

        // Create a list of all products first
        List<Product> products = getAllProducts();

        // Create a list view to display all products
        ListView<String> productListView = new ListView<>();
        for (Product product : products) {
            productListView.getItems().add("ID: " + product.getId() + " | Name: " + product.getName());
        }

        // Create input fields for editing the product
        TextField nameField = new TextField();
        TextField categoryField = new TextField();
        TextField quantityField = new TextField();
        TextField priceField = new TextField();
        Button submitButton = new Button("Update Product");

        productListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Extract the product ID from the selected product string
                String[] parts = newValue.split("\\|");
                if (parts.length > 0) {
                    String idPart = parts[0].trim(); // "ID: <id>"
                    int id = Integer.parseInt(idPart.split(":")[1].trim());

                    // Get the product details from the service and fill the fields
                    Product selectedProduct = getProductById(id);
                    if (selectedProduct != null) {
                        nameField.setText(selectedProduct.getName());
                        categoryField.setText(selectedProduct.getCategory());
                        quantityField.setText(String.valueOf(selectedProduct.getQuantity()));
                        priceField.setText(String.valueOf(selectedProduct.getPrice()));
                    }
                }
            }
        });

        submitButton.setOnAction(event -> {
            try {
                InventoryService service = (InventoryService) Naming.lookup("rmi://localhost:1099/InventoryService");

                // Get the selected product ID from the ListView
                String selectedProduct = productListView.getSelectionModel().getSelectedItem();
                if (selectedProduct != null) {
                    // Extract the product ID from the selection string
                    int id = Integer.parseInt(selectedProduct.split("\\|")[0].split(":")[1].trim());

                    Product product = new Product();
                    product.setId(id);

                    // Update only the fields that are not empty
                    if (!nameField.getText().isEmpty()) product.setName(nameField.getText());
                    if (!categoryField.getText().isEmpty()) product.setCategory(categoryField.getText());
                    if (!quantityField.getText().isEmpty()) product.setQuantity(Integer.parseInt(quantityField.getText()));
                    if (!priceField.getText().isEmpty()) product.setPrice(Double.parseDouble(priceField.getText()));

                    service.updateProduct(product);
                    updateProductStage.close();
                    logger.info("Product updated: ID " + id + " | New Name: " + product.getName() + " | New Category: " + product.getCategory());
                } else {
                    showError("Please select a product to update.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        VBox layout = new VBox(10, new Label("Select Product to Update:"), productListView,
                new Label("New Name:"), nameField, new Label("New Category:"), categoryField,
                new Label("New Quantity:"), quantityField, new Label("New Price:"), priceField, submitButton);
        layout.setStyle("-fx-padding: 20;");
        layout.setAlignment(Pos.CENTER); // Center the form fields
        Scene scene = new Scene(layout, 350, 400);

        updateProductStage.setScene(scene);
        updateProductStage.setTitle("Update Product");
        updateProductStage.show();
    }

    private void showDeleteProductWindow(Stage primaryStage) {
        Stage deleteProductStage = new Stage();

        // Create a list of all products first
        List<Product> products = getAllProducts();

        // Create a list view to display all products
        ListView<String> productListView = new ListView<>();
        for (Product product : products) {
            productListView.getItems().add("ID: " + product.getId() + " | Name: " + product.getName());
        }

        Button submitButton = new Button("Delete Product");

        submitButton.setOnAction(event -> {
            try {
                InventoryService service = (InventoryService) Naming.lookup("rmi://localhost:1099/InventoryService");

                // Get the selected product ID from the ListView
                String selectedProduct = productListView.getSelectionModel().getSelectedItem();
                if (selectedProduct != null) {
                    // Extract the product ID from the selection string
                    int id = Integer.parseInt(selectedProduct.split("\\|")[0].split(":")[1].trim());

                    service.deleteProduct(id);
                    deleteProductStage.close();
                    logger.info("Product deleted: ID " + id);
                } else {
                    showError("Please select a product to delete.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        VBox layout = new VBox(10, new Label("Select Product to Delete:"), productListView, submitButton);
        layout.setStyle("-fx-padding: 20;");
        layout.setAlignment(Pos.CENTER); // Center the form fields
        Scene scene = new Scene(layout, 300, 300);

        deleteProductStage.setScene(scene);
        deleteProductStage.setTitle("Delete Product");
        deleteProductStage.show();
    }

    private List<Product> getAllProducts() {
        // Get all products from the server via RMI
        try {
            InventoryService service = (InventoryService) Naming.lookup("rmi://localhost:1099/InventoryService");
            return service.getAllProducts();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Product getProductById(int id) {
        // Get a product by ID
        try {
            InventoryService service = (InventoryService) Naming.lookup("rmi://localhost:1099/InventoryService");
            return service.getProductById(id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
