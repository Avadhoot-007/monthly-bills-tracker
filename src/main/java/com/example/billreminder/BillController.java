package com.example.billreminder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpSession;

// This class answers web requests that start with /api/bills
@RestController
@RequestMapping("/api/bills")
public class BillController {

    // Spring gives us the repository automatically
    @Autowired
    private BillRepository billRepository;

    // Finds who is logged in. Stops with error 401 if nobody is
    private int getLoggedInUserId(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in");
        }
        return userId;
    }

    // GET /api/bills : return the bills of the logged-in user
    @GetMapping
    public List<Bill> getAllBills(HttpSession session) {
        int userId = getLoggedInUserId(session);
        return billRepository.findByUserId(userId);
    }

    // GET /api/bills/3 : return one bill of this user
    @GetMapping("/{id}")
    public Bill getOneBill(@PathVariable Integer id, HttpSession session) {
        int userId = getLoggedInUserId(session);
        Optional<Bill> result = billRepository.findByIdAndUserId(id, userId);
        if (!result.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill not found");
        }
        return result.get();
    }

    // POST /api/bills : add a new bill for this user
    @PostMapping
    public Bill addBill(@RequestBody Bill newBill, HttpSession session) {
        int userId = getLoggedInUserId(session);
        // Empty id tells the database to create a new row
        newBill.setId(null);
        // Always use the logged-in user, never a user id sent by the browser
        newBill.setUserId(userId);
        return billRepository.save(newBill);
    }

    // PUT /api/bills/3 : change an existing bill of this user
    @PutMapping("/{id}")
    public Bill updateBill(@PathVariable Integer id, @RequestBody Bill changedBill, HttpSession session) {
        int userId = getLoggedInUserId(session);
        Optional<Bill> existing = billRepository.findByIdAndUserId(id, userId);
        if (!existing.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill not found");
        }
        // Same id means save() updates the old row
        changedBill.setId(id);
        changedBill.setUserId(userId);
        return billRepository.save(changedBill);
    }

    // DELETE /api/bills/3 : remove a bill of this user
    @DeleteMapping("/{id}")
    public void deleteBill(@PathVariable Integer id, HttpSession session) {
        int userId = getLoggedInUserId(session);
        Optional<Bill> existing = billRepository.findByIdAndUserId(id, userId);
        if (!existing.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill not found");
        }
        billRepository.delete(existing.get());
    }

    // POST /api/bills/3/toggle-paid : switch Paid and Unpaid
    @PostMapping("/{id}/toggle-paid")
    public Bill togglePaid(@PathVariable Integer id, HttpSession session) {
        int userId = getLoggedInUserId(session);
        Optional<Bill> result = billRepository.findByIdAndUserId(id, userId);
        if (!result.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill not found");
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