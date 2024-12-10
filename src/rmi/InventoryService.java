package rmi;

import models.Product;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface InventoryService extends Remote {
    List<Product> getAllProducts() throws RemoteException;
    void addProduct(Product product) throws RemoteException;
    void updateProduct(Product product) throws RemoteException;
    void deleteProduct(int productId) throws RemoteException;
    Product getProductById(int id) throws RemoteException;

}
