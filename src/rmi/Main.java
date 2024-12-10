package rmi;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class Main {
    public static void main(String[] args) {
        try {
            // Start RMI Registry
            System.out.println("Starting RMI registry...");
            LocateRegistry.createRegistry(1099);

            // Start RMI Server
            System.out.println("Starting RMI server...");
            InventoryServiceImpl inventoryService = new InventoryServiceImpl();
            Naming.rebind("rmi://localhost/InventoryService", inventoryService);
            System.out.println("RMI server started successfully!");

            // Launch the GUI Client
            System.out.println("Launching the Inventory Client GUI...");
            InventoryClientGUI.main(args); // Launch JavaFX GUI

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
