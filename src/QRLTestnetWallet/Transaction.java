/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package QRLTestnetWallet;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author Aidan
 */
public class Transaction extends RecursiveTreeObject<Transaction>{

    SimpleStringProperty timeProperty;
    SimpleStringProperty hashProperty;
    SimpleStringProperty toProperty;
    SimpleStringProperty amountProperty;
    SimpleStringProperty blockProperty;
    
    public Transaction(String time, String hash, String to, String amount, String block) {
        this.timeProperty = new SimpleStringProperty(time);
        this.hashProperty = new SimpleStringProperty(hash);
        this.toProperty = new SimpleStringProperty(to);
        this.amountProperty = new SimpleStringProperty(amount);
        this.blockProperty = new SimpleStringProperty(block);
    }
    
    public SimpleStringProperty getTimeProperty() {
        return timeProperty;
    }
    
    public SimpleStringProperty getHashProperty() {
        return hashProperty;
    }
    
    public SimpleStringProperty getToProperty() {
        return toProperty;
    }
    
    public SimpleStringProperty getAmountProperty() {
        return amountProperty;
    }
    
    public SimpleStringProperty getBlockProperty() {
        return blockProperty;
    }
}