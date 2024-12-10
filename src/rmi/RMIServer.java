package rmi;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class RMIServer {
    public static void main(String[] args) {
        try {
            // Start the RMI registry on port 1099
            LocateRegistry.createRegistry(1099);
            System.out.println("RMI registry started on port 1099.");

            // Register the InventoryService
            InventoryService inventoryService = new InventoryServiceImpl();
            Naming.rebind("rmi://localhost:1099/InventoryService", inventoryService);
            System.out.println("InventoryService registered and ready for remote calls.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
