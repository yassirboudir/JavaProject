package rmi;

import dao.ProductDAO;
import models.Product;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class InventoryServiceImpl extends UnicastRemoteObject implements InventoryService {
    private final ProductDAO productDAO;

    public InventoryServiceImpl() throws RemoteException {
        super();
        this.productDAO = new ProductDAO();
    }

    @Override
    public List<Product> getAllProducts() throws RemoteException {
        try {
            return productDAO.getAllProducts();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Error fetching products.", e);
        }
    }

    @Override
    public Product getProductById(int id) throws RemoteException {
        try {
            return productDAO.getProductById(id);  // Fetch the product by ID from the database
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Error fetching product by ID.", e);
        }
    }

    @Override
    public void addProduct(Product product) throws RemoteException {
        try {
            productDAO.addProduct(product);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Error adding product.", e);
        }
    }

    @Override
    public void updateProduct(Product product) throws RemoteException {
        try {
            productDAO.updateProduct(product);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Error updating product.", e);
        }
    }

    @Override
    public void deleteProduct(int productId) throws RemoteException {
        try {
            productDAO.deleteProduct(productId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Error deleting product.", e);
        }
    }
}
