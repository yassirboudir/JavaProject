package rmi;

import java.rmi.Naming;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Scanner;
import java.io.Console;
import java.util.logging.*;
import models.Product;
import dao.EmployeeDAO;
import models.Employee;

public class InventoryClient {
    private static Connection connection;
    private static final Logger logger = Logger.getLogger(InventoryClient.class.getName());

    static {
        try {
            // Set up file handler for logging
            FileHandler fileHandler = new FileHandler("inventory_operations.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);

            // Remove default console handler
            Logger rootLogger = Logger.getLogger("");
            Handler[] handlers = rootLogger.getHandlers();
            for (Handler handler : handlers) {
                if (handler instanceof ConsoleHandler) {
                    rootLogger.removeHandler(handler);
                }
            }

            // Optionally set the logging level to INFO or higher
            logger.setLevel(Level.INFO);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            // Set up the database connection
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/inventory_system", "root", "Rikiest123@");

            // Create the EmployeeDAO object for authentication
            EmployeeDAO employeeDAO = new EmployeeDAO(connection);

            // Prompt user for login credentials
            Console console = System.console();
            String username;
            String password;

            if (console != null) {
                username = console.readLine("Enter username: ");
                char[] passwordChars = console.readPassword("Enter password: ");
                password = new String(passwordChars);
            } else {
                // Fallback for environments where Console is not available (e.g., IDEs)
                Scanner scanner = new Scanner(System.in);
                System.out.print("Enter username: ");
                username = scanner.nextLine();
                System.out.print("Enter password (visible): ");
                password = scanner.nextLine();
            }

            // Authenticate the user
            Employee employee = employeeDAO.authenticate(username, password);

            if (employee != null) {
                System.out.println("Login successful! Welcome, " + employee.getUsername());

                // Log successful login
                logger.info("Login successful for user: " + username);

                // Proceed with inventory system
                InventoryService service = (InventoryService) Naming.lookup("rmi://localhost:1099/InventoryService");

                while (true) {
                    System.out.println("\nInventory Management System");
                    System.out.println("1. View All Products");
                    if (employee.getRole().equalsIgnoreCase("admin")) {
                        System.out.println("2. Add a Product");
                        System.out.println("3. Update a Product");
                        System.out.println("4. Delete a Product");
                    }
                    System.out.println("5. Exit");
                    System.out.print("Choose an option: ");
                    int choice = new Scanner(System.in).nextInt();

                    switch (choice) {
                        case 1:
                            List<Product> products = service.getAllProducts();
                            if (products.isEmpty()) {
                                System.out.println("No products found!");
                            } else {
                                System.out.println("Product List:");
                                System.out.println("---------------------------------------------------------");
                                System.out.printf("|   %-4s | %-15s | %-12s | %-4s | %-6s |\n", "ID", "Name", "Category", "Qty", "Price");
                                System.out.println("---------------------------------------------------------");
                                for (Product product : products) {
                                    System.out.printf("|   %-4d | %-15s | %-12s | %-4d | %-6.2f |\n",
                                            product.getId(), product.getName(), product.getCategory(), product.getQuantity(), product.getPrice());
                                }
                                System.out.println("---------------------------------------------------------");
                            }
                            break;

                        case 2:
                            if (!employee.getRole().equalsIgnoreCase("admin")) {
                                System.out.println("You do not have permission to perform this action.");
                                break;
                            }
                            System.out.print("Enter Product Name: ");
                            String name = new Scanner(System.in).next();
                            System.out.print("Enter Product Category: ");
                            String category = new Scanner(System.in).next();
                            System.out.print("Enter Product Quantity: ");
                            int quantity = new Scanner(System.in).nextInt();
                            System.out.print("Enter Product Price: ");
                            double price = new Scanner(System.in).nextDouble();

                            Product newProduct = new Product(0, name, category, quantity, price);
                            service.addProduct(newProduct);
                            System.out.println("Product added successfully!");

                            // Log the product addition
                            logger.info("Product added: " + newProduct);
                            break;

                        case 3:
                            if (!employee.getRole().equalsIgnoreCase("admin")) {
                                System.out.println("You do not have permission to perform this action.");
                                break;
                            }
                            System.out.print("Enter Product ID to Update: ");
                            int updateId = new Scanner(System.in).nextInt();
                            System.out.print("Enter New Product Name: ");
                            String newName = new Scanner(System.in).next();
                            System.out.print("Enter New Product Category: ");
                            String newCategory = new Scanner(System.in).next();
                            System.out.print("Enter New Product Quantity: ");
                            int newQuantity = new Scanner(System.in).nextInt();
                            System.out.print("Enter New Product Price: ");
                            double newPrice = new Scanner(System.in).nextDouble();

                            Product updatedProduct = new Product(updateId, newName, newCategory, newQuantity, newPrice);
                            service.updateProduct(updatedProduct);
                            System.out.println("Product updated successfully!");

                            // Log the product update
                            logger.info("Product updated: " + updatedProduct);
                            break;

                        case 4:
                            if (!employee.getRole().equalsIgnoreCase("admin")) {
                                System.out.println("You do not have permission to perform this action.");
                                break;
                            }
                            System.out.print("Enter Product ID to Delete: ");
                            int deleteId = new Scanner(System.in).nextInt();
                            service.deleteProduct(deleteId);
                            System.out.println("Product deleted successfully!");

                            // Log the product deletion
                            logger.info("Product deleted with ID: " + deleteId);
                            break;

                        case 5:
                            System.out.println("Exiting Inventory Management System...");
                            // Log exit
                            logger.info("User " + username + " exited the system.");
                            System.exit(0);
                            break;

                        default:
                            System.out.println("Invalid choice. Please try again.");
                    }
                }
            } else {
                System.out.println("Invalid username or password.");
                // Log failed login attempt
                logger.warning("Failed login attempt for username: " + username);
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Log exception
            logger.severe("Exception occurred: " + e.getMessage());
        }
    }
}
