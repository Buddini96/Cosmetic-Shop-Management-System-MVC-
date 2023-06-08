package lk.ijse.cosmeticshop.controller;

import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import lk.ijse.cosmeticshop.model.CustomerModel;
import lk.ijse.cosmeticshop.to.Customer;
import lk.ijse.cosmeticshop.util.CrudUtil;
import lk.ijse.cosmeticshop.util.Navigation;
import lk.ijse.cosmeticshop.util.Routes;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomerFormController implements Initializable {
    public AnchorPane pane;
    public TextField txtCustomerId;
    public TextField txtName;
    public TextField txtAddress;
    public TextField txtContact;
    public TableView tblCustomer;
    public JFXTextField txtSearch;
    public TableColumn colCustomerId;
    public TableColumn colName;
    public TableColumn colAddress;
    public TableColumn colContact;

    private Matcher userId;
    private Matcher userName;
    private Matcher userAddress;
    private Matcher userContact;
    private String searchText="";

    ObservableList<Customer> cusList = FXCollections.observableArrayList();
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){
        colCustomerId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contact"));

        //Search bar
        txtSearch.textProperty()
                .addListener((observable, oldValue, newValue) ->{
                    loadAllCustomers(newValue);
                });
        loadAllCustomers("");
    }

    private void loadAllCustomers(String text) {
        ObservableList<Customer> cusList = FXCollections.observableArrayList();

        try{
            ArrayList<Customer> customersData = CustomerModel.getCustomerData();
            for (Customer customer:customersData){
                if(customer.getId().contains(text) || customer.getName().contains(text)){
                    Customer c = new Customer(customer.getId(), customer.getName(), customer.getAddress(), customer.getContact());
                    cusList.add(c);
                }
            }
        }catch(SQLException | ClassNotFoundException e){
            System.out.println(e);
        }

        tblCustomer.setItems(cusList);
    }

    public void btnCustomerOnAction(ActionEvent actionEvent) throws IOException {
        Navigation.navigate(Routes.CUSTOMER, pane);
    }

    public void btnProductsOnAction(ActionEvent actionEvent) throws IOException {
        Navigation.navigate(Routes.PRODUCT_ADMIN, pane);
    }

    public void btnOrderOnAction(ActionEvent actionEvent) throws IOException {
        Navigation.navigate(Routes.ORDER, pane);
    }

    public void btnPlaceOrderOnAction(ActionEvent actionEvent) throws IOException {
        Navigation.navigate(Routes.PLACE_ORDER, pane);
    }

    public void btnLogOutOnAction(ActionEvent actionEvent) throws IOException {
        Navigation.navigate(Routes.HOME, pane);
    }

    public void btnDelveryOnAction(ActionEvent actionEvent) throws IOException {
        Navigation.navigate(Routes.DELIVERY, pane);
    }

    //Form

    public void btnAddOnAction(ActionEvent actionEvent) {
        String id = txtCustomerId.getText();
        String name = txtName.getText();
        String address = txtAddress.getText();
        int contact = Integer.parseInt(txtContact.getText());

        Pattern pattern = Pattern.compile("(C0)([1-9]{1,2})");
        Matcher matcher = pattern.matcher(txtCustomerId.getText());

        boolean isMatcheId = matcher.matches();
        if(!isMatcheId){
            new Alert(Alert.AlertType.WARNING, "Invalid Customer Id!").show();
        }

        Customer customer = new Customer(id,name,address,contact);
            try{
            boolean isAdded = CustomerModel.save(customer);
            if (isAdded){
                new Alert(Alert.AlertType.CONFIRMATION, "Customer Added Successfully!").show();
            }else {
                new Alert(Alert.AlertType.WARNING, "Customer not Added!").show();
            }
        }catch (SQLException | ClassNotFoundException e){
                throw new RuntimeException(e);
            }

          ObservableList<Customer> customers = tblCustomer.getItems();
            customers.add(customer);
            tblCustomer.setItems(customers);

    }

    public void btnUpdateOnAction(ActionEvent actionEvent) {
        String id = txtCustomerId.getText();
        String name = txtName.getText();
        String address = txtAddress.getText();
        int contact = Integer.parseInt(txtContact.getText());

        try{
            Customer customer = new Customer(id,name,address,contact);
            boolean isUpdated = CustomerModel.update(customer, id);
            if (isUpdated){
                new Alert(Alert.AlertType.CONFIRMATION, "Customer Updated Successfully!").show();
                colCustomerId.setCellValueFactory(new PropertyValueFactory<>("id"));
                colName.setCellValueFactory(new PropertyValueFactory<>("name"));
                colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
                colContact.setCellValueFactory(new PropertyValueFactory<>("contact"));

                //Search bar
                txtSearch.textProperty()
                        .addListener((observable, oldValue, newValue) ->{
                            loadAllCustomers(newValue);
                        });
                loadAllCustomers("");
            }else {
                new Alert(Alert.AlertType.WARNING, "Customer not Updated!").show();
            }
        }catch (SQLException | ClassNotFoundException e){
            throw new RuntimeException(e);
        }


        ObservableList<Customer> currentTableData = tblCustomer.getItems();
        String currentCustomerId = txtCustomerId.getText();

        for(Customer customer : currentTableData){
            if(customer.getId() == currentCustomerId){
                customer.setName(txtName.getText());
                customer.setAddress(txtAddress.getText());
                customer.setContact(Integer.parseInt(txtContact.getText()));

                tblCustomer.setItems(currentTableData);
                tblCustomer.refresh();
                break;
            }
        }

    }

    public void btnClearOnAction(ActionEvent actionEvent) {
        txtCustomerId.setText("");
        txtName.setText("");
        txtAddress.setText("");
        txtContact.setText("");
    }

    public void btnDeleteOnAction(ActionEvent actionEvent) throws SQLException, ClassNotFoundException {
        String id = txtCustomerId.getText();
        String name = txtName.getText();
        String address = txtAddress.getText();
        int contact = Integer.parseInt(txtContact.getText());

        try{
            Customer customer = new Customer(id,name,address,contact);
            boolean isDeleted = CustomerModel.delete(customer, id);
            if (isDeleted){
                new Alert(Alert.AlertType.CONFIRMATION, "Customer Deleted Successfully!").show();
            }else {
                new Alert(Alert.AlertType.WARNING, "Customer not Deleted!").show();
            }
        }catch (SQLException | ClassNotFoundException e){
            throw new RuntimeException(e);
        }

        int selectedID = tblCustomer.getSelectionModel().getSelectedIndex();
        tblCustomer.getItems().remove(selectedID);
        
    }

    public void txtCustomerIdOnAction(ActionEvent actionEvent) {
        String id = txtCustomerId.getText();
        try {
            Customer customer = CustomerModel.search(id);
            if (customer != null){
                fillData(customer);
            }
        }catch (SQLException | ClassNotFoundException e){
            throw new RuntimeException(e);
        }
    }

    public void txtSearchOnAction(ActionEvent actionEvent) {
        String id = txtSearch.getText();
        try{
            Customer customer = CustomerModel.search(id);
            if (customer != null){
                fillData(customer);
            }
        }catch (SQLException | ClassNotFoundException e){
            throw new RuntimeException(e);
        }
    }

    private void fillData(Customer customer){
        txtCustomerId.setText(customer.getId());
        txtName.setText(customer.getName());
        txtAddress.setText(customer.getAddress());
        txtContact.setText(String.valueOf(customer.getContact()));
    }

    public void rowClicked(MouseEvent mouseEvent) {
        Customer clickedCustomer = (Customer) tblCustomer.getSelectionModel().getSelectedItem();
        txtCustomerId.setText(String.valueOf(clickedCustomer.getId()));
        txtName.setText(String.valueOf(clickedCustomer.getName()));
        txtAddress.setText(String.valueOf(clickedCustomer.getAddress()));
        txtContact.setText(String.valueOf(clickedCustomer.getContact()));

    }

}

