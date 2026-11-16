package com.example.billreminder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// This class answers web requests that start with /api/bills
@RestController
@RequestMapping("/api/bills")
public class BillController {

    // Spring gives us the repository automatically
    @Autowired
    private BillRepository billRepository;

    // GET /api/bills : return all bills
    @GetMapping
    public List<Bill> getAllBills() {
        return billRepository.findAll();
    }

    // GET /api/bills/3 : return one bill, or null if not found
    @GetMapping("/{id}")
    public Bill getOneBill(@PathVariable Integer id) {
        Optional<Bill> result = billRepository.findById(id);
        if (result.isPresent()) {
            return result.get();
        }
        return null;
    }

    // POST /api/bills : add a new bill
    @PostMapping
    public Bill addBill(@RequestBody Bill newBill) {
        // Empty id tells the database to create a new row
        newBill.setId(null);
        return billRepository.save(newBill);
    }

    // PUT /api/bills/3 : change an existing bill
    @PutMapping("/{id}")
    public Bill updateBill(@PathVariable Integer id, @RequestBody Bill changedBill) {
        // Stop if this id does not exist
        if (!billRepository.existsById(id)) {
            return null;
        }
        // Same id means save() updates the old row
        changedBill.setId(id);
        return billRepository.save(changedBill);
    }

    // DELETE /api/bills/3 : remove a bill
    @DeleteMapping("/{id}")
    public void deleteBill(@PathVariable Integer id) {
        billRepository.deleteById(id);
    }

    // POST /api/bills/3/toggle-paid : switch Paid and Unpaid
    @PostMapping("/{id}/toggle-paid")
    public Bill togglePaid(@PathVariable Integer id) {
        Optional<Bill> result = billRepository.findById(id);
        if (!result.isPresent()) {
            return null;
        }
        Bill bill = result.get();

        // Build the current month text, like 2026-11
        String thisMonth = LocalDate.now().toString().substring(0, 7);

        if (thisMonth.equals(bill.getPaidMonth())) {
            // Already paid this month, so mark unpaid
            bill.setPaidMonth(null);
        } else {
            // Not paid yet, so mark paid for this month
            bill.setPaidMonth(thisMonth);
        }
        return billRepository.save(bill);
    }
}