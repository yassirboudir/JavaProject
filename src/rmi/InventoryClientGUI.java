package rmi;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.Product;
import dao.EmployeeDAO;
import models.Employee;
import javafx.geometry.*;
import java.rmi.Naming;
import java.sql.*;
import java.util.List;
import java.util.logging.*;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;


public class InventoryClientGUI extends Application {
    private static Connection connection;
    private TextField usernameField;
    private PasswordField passwordField;
    private Label statusLabel;
    private Button loginButton;

    private static final Logger logger = Logger.getLogger(InventoryClientGUI.class.getName());

    static {
        try {
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
        initializeDatabaseConnection();

        // Login Page Design
        VBox loginLayout = new VBox(20);
        loginLayout.setAlignment(Pos.CENTER);
        loginLayout.setStyle("-fx-padding: 20; -fx-background-color: #f0f0f0; -fx-border-color: #d3d3d3; -fx-border-width: 2px;");

        Label titleLabel = new Label("Inventory System Login");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333;");

        usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setMaxWidth(200);

        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setMaxWidth(200);

        loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 5 20 5 20;");
        loginButton.setOnAction(event -> authenticateAndShowMainMenu(primaryStage));

        statusLabel = new Label("Please log in.");
        statusLabel.setStyle("-fx-text-fill: #888;");

        loginLayout.getChildren().addAll(titleLabel, usernameField, passwordField, loginButton, statusLabel);

        Scene loginScene = new Scene(loginLayout, 400, 300);

        primaryStage.setTitle("Inventory Client Login");
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    private void initializeDatabaseConnection() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/inventory_system", "root", "Rikiest123@");
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database connection failed. Please check your connection settings.");
        }
    }

    private void authenticateAndShowMainMenu(Stage primaryStage) {
        try {
            EmployeeDAO employeeDAO = new EmployeeDAO(connection);
            String username = usernameField.getText();
            String password = passwordField.getText();
            Employee employee = employeeDAO.authenticate(username, password);

            if (employee != null) {
                statusLabel.setText("Login successful! Welcome, " + employee.getUsername());
                logger.info("User " + username + " logged in successfully.");
                showInventoryMenu(primaryStage, employee);
            } else {
                statusLabel.setText("Invalid username or password.");
                logger.warning("Failed login attempt for username: " + username);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showInventoryMenu(Stage primaryStage, Employee employee) {
        BorderPane mainMenu = new BorderPane();
        mainMenu.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 20;");

        Label welcomeLabel = new Label("Welcome, " + employee.getUsername());
        welcomeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");
        mainMenu.setTop(welcomeLabel);
        BorderPane.setAlignment(welcomeLabel, Pos.CENTER);

        VBox menuOptions = new VBox(15);
        menuOptions.setAlignment(Pos.CENTER);

        Button viewProductsButton = new Button("View All Products");
        Button addProductButton = new Button("Add a Product");
        Button updateProductButton = new Button("Update a Product");
        Button deleteProductButton = new Button("Delete a Product");
        Button exitButton = new Button("Exit");

        styleButton(viewProductsButton, "black");
        styleButton(addProductButton, "black");
        styleButton(updateProductButton, "black");
        styleButton(deleteProductButton, "black");
        styleButton(exitButton, "black");

        if (employee.getRole().equalsIgnoreCase("admin")) {
            addProductButton.setOnAction(event -> showAddProductWindow(primaryStage));
            updateProductButton.setOnAction(event -> showUpdateProductWindow(primaryStage));
            deleteProductButton.setOnAction(event -> showDeleteProductWindow(primaryStage));
        }

        viewProductsButton.setOnAction(event -> viewAllProducts(employee));
        exitButton.setOnAction(event -> {
            logger.info("User " + employee.getUsername() + " logged out.");
            System.exit(0);
        });

        menuOptions.getChildren().addAll(viewProductsButton, addProductButton, updateProductButton, deleteProductButton, exitButton);
        mainMenu.setCenter(menuOptions);

        Scene menuScene = new Scene(mainMenu, 500, 400);
        primaryStage.setScene(menuScene);
    }

    private void styleButton(Button button, String color) {
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 14px;");
    }




    private void showDeleteProductWindow(Stage primaryStage) {
        Stage deleteProductStage = new Stage();

        // Fetch all products
        List<Product> products = getAllProducts();

        if (products == null || products.isEmpty()) {
            showError("No products available for deletion.");
            return;
        }

        // Create a list view to display all products
        ListView<String> productListView = new ListView<>();
        for (Product product : products) {
            productListView.getItems().add("ID: " + product.getId() + " | Name: " + product.getName());
        }

        Button submitButton = new Button("Delete Product");

        submitButton.setOnAction(event -> {
            try {
                String selectedProduct = productListView.getSelectionModel().getSelectedItem();
                if (selectedProduct != null) {
                    int id = Integer.parseInt(selectedProduct.split("\\|")[0].split(":")[1].trim());

                    InventoryService service = (InventoryService) Naming.lookup("rmi://localhost:1099/InventoryService");
                    service.deleteProduct(id);

                    logger.info("Product deleted: ID " + id);
                    deleteProductStage.close();
                } else {
                    showError("Please select a product to delete.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showError("An error occurred while deleting the product.");
            }
        });

        VBox layout = new VBox(10, new Label("Select Product to Delete:"), productListView, submitButton);
        layout.setStyle("-fx-padding: 20;");
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 300, 300);
        deleteProductStage.setScene(scene);
        deleteProductStage.setTitle("Delete Product");
        deleteProductStage.show();
    }




    private void viewAllProducts(Employee employee) {
        try {
            InventoryService service = (InventoryService) Naming.lookup("rmi://localhost:1099/InventoryService");
            List<Product> products = service.getAllProducts();

            Stage productStage = new Stage();
            VBox productLayout = new VBox(10);
            productLayout.setStyle("-fx-padding: 20;");

            // Create a TableView to display the products
            TableView<Product> table = new TableView<>();

            // Define columns for the table
            TableColumn<Product, Integer> idColumn = new TableColumn<>("ID");
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            idColumn.setMinWidth(100);

            TableColumn<Product, String> nameColumn = new TableColumn<>("Name");
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            nameColumn.setMinWidth(200);

            TableColumn<Product, String> categoryColumn = new TableColumn<>("Category");
            categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
            categoryColumn.setMinWidth(150);

            TableColumn<Product, Integer> quantityColumn = new TableColumn<>("Quantity");
            quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            quantityColumn.setMinWidth(100);

            TableColumn<Product, Double> priceColumn = new TableColumn<>("Price");
            priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
            priceColumn.setMinWidth(100);

            // Add columns to the TableView
            table.getColumns().addAll(idColumn, nameColumn, categoryColumn, quantityColumn, priceColumn);

            // Create an ObservableList to hold the products
            ObservableList<Product> productList = FXCollections.observableArrayList(products);
            table.setItems(productList);

            // Create a ScrollPane for the table
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setContent(table);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

            // Add the ScrollPane to the layout
            productLayout.getChildren().add(scrollPane);

            // Create a TextField for the search input

            TextField searchField = new TextField();
            searchField.setPromptText("Search by name...");

            // Create a Search button
            Button searchButton = new Button("Search");
            searchButton.setStyle("-fx-padding: 10;");

            // Implement the search functionality
            searchButton.setOnAction(e -> {
                String query = searchField.getText().toLowerCase().trim();

                // Filter the products based on the search query
                ObservableList<Product> filteredList = FXCollections.observableArrayList(
                        productList.stream()
                                .filter(product -> product.getName().toLowerCase().contains(query))
                                .collect(Collectors.toList())
                );

                // Update the table with the filtered list
                table.setItems(filteredList);
            });

            // Add the search input and button to the layout
            HBox searchLayout = new HBox(10, searchField, searchButton);
            searchLayout.setStyle("-fx-padding: 10;");

            // Add the search layout to the main layout
            productLayout.getChildren().add(searchLayout);

            // Add resize listener to adjust font size based on window size
            productStage.widthProperty().addListener((obs, oldVal, newVal) -> {
                double width = newVal.doubleValue();
                // Calculate a more conservative font size based on width with a max size limit
                double fontSize = Math.max(12, Math.min(width / 50, 18)); // Font size will range between 12 and 18px
                table.setStyle("-fx-font-size: " + fontSize + "px;");
            });

            productStage.heightProperty().addListener((obs, oldVal, newVal) -> {
                double height = newVal.doubleValue();
                // Calculate a more conservative font size based on height with a max size limit
                double fontSize = Math.max(12, Math.min(height / 40, 18)); // Font size will range between 12 and 18px
                table.setStyle("-fx-font-size: " + fontSize + "px;");
            });

            Scene productScene = new Scene(productLayout, 600, 400);
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
        layout.setAlignment(Pos.CENTER);
        Scene scene = new Scene(layout, 300, 300);

        addProductStage.setScene(scene);
        addProductStage.setTitle("Add Product");
        addProductStage.show();
    }

    private void showUpdateProductWindow(Stage primaryStage) {
        Stage updateProductStage = new Stage();

        List<Product> products = getAllProducts();

        ListView<String> productListView = new ListView<>();
        for (Product product : products) {
            productListView.getItems().add("ID: " + product.getId() + " | Name: " + product.getName());
        }

        TextField nameField = new TextField();
        TextField categoryField = new TextField();
        TextField quantityField = new TextField();
        TextField priceField = new TextField();
        Button submitButton = new Button("Update Product");

        productListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                String[] parts = newValue.split("\\|");
                if (parts.length > 0) {
                    String idPart = parts[0].trim();
                    int id = Integer.parseInt(idPart.split(":")[1].trim());

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

                String selectedProduct = productListView.getSelectionModel().getSelectedItem();
                if (selectedProduct != null) {
                    int id = Integer.parseInt(selectedProduct.split("\\|")[0].split(":")[1].trim());

                    Product product = new Product();
                    product.setId(id);

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
        layout.setAlignment(Pos.CENTER);
        Scene scene = new Scene(layout, 350, 400);

        updateProductStage.setScene(scene);
        updateProductStage.setTitle("Update Product");
        updateProductStage.show();
    }


    private List<Product> getAllProducts() {
        try {
            InventoryService service = (InventoryService) Naming.lookup("rmi://localhost:1099/InventoryService");
            return service.getAllProducts();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    private Product getProductById(int id) {
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
        alert.setContentText(message);
        alert.showAndWait();
    }
}
