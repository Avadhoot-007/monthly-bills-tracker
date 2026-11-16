package com.example.billreminder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// This class represents one row of the bills table
@Entity
@Table(name = "bills")
public class Bill {

    // Primary key. MySQL creates the number automatically
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;

    @Column(name = "bill_type")
    private String billType;

    // Amount in rupees
    private double amount;

    @Column(name = "due_day")
    private int dueDay;

    // Example: 2026-11. Empty means never paid
    @Column(name = "paid_month")
    private String paidMonth;

    private String notes;

    // Getters and setters. Spring uses these to read and write values

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBillType() {
        return billType;
    }

    public void setBillType(String billType) {
        this.billType = billType;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getDueDay() {
        return dueDay;
    }

    public void setDueDay(int dueDay) {
        this.dueDay = dueDay;
    }

    public String getPaidMonth() {
        return paidMonth;
    }

    public void setPaidMonth(String paidMonth) {
        this.paidMonth = paidMonth;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}