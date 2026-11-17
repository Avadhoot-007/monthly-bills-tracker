package com.example.billreminder;

import java.time.YearMonth;
import java.time.ZoneId;
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

// This class answers web requests that start with /api/bills
@RestController
@RequestMapping("/api/bills")
public class BillController {

    // "This month" is always counted in India time.
    // Without this, a server set to UTC flips the month 5.5 hours late
    private static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");

    // Spring gives us the repository automatically
    @Autowired
    private BillRepository billRepository;

    // Finds who is logged in. Stops with error 401 if nobody is
    // getSession(false): a request with no login must not create a session
    private int getLoggedInUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in");
        }
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in");
        }
        return userId;
    }

    // Cleans the text and number fields, then stops with error 400 if bad
    private void checkBill(Bill bill) {
        if (bill.getTitle() == null || bill.getTitle().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is required");
        }
        // Save the trimmed title, not the raw one
        bill.setTitle(bill.getTitle().trim());
        if (bill.getTitle().length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is too long");
        }

        // Round to 2 decimals first. The database keeps exactly 2
        bill.setAmount(Math.round(bill.getAmount() * 100.0) / 100.0);
        if (bill.getAmount() <= 0 || bill.getAmount() > 99999999) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad amount");
        }
        if (bill.getDueDay() < 1 || bill.getDueDay() > 31) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Due day must be 1 to 31");
        }

        // Empty notes are stored as null
        if (bill.getNotes() != null) {
            String notes = bill.getNotes().trim();
            if (notes.length() > 255) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Notes are too long");
            }
            if (notes.isEmpty()) {
                bill.setNotes(null);
            } else {
                bill.setNotes(notes);
            }
        }

        // Only these four bill types are allowed
        String type = bill.getBillType();
        boolean typeOk = false;
        if ("Utilities".equals(type)) {
            typeOk = true;
        }
        if ("Investment".equals(type)) {
            typeOk = true;
        }
        if ("Loan".equals(type)) {
            typeOk = true;
        }
        if ("Subscription".equals(type)) {
            typeOk = true;
        }
        if (!typeOk) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad bill type");
        }
    }

    // GET /api/bills : return the bills of the logged-in user
    @GetMapping
    public List<Bill> getAllBills(HttpServletRequest request) {
        int userId = getLoggedInUserId(request);
        return billRepository.findByUserId(userId);
    }

    // GET /api/bills/3 : return one bill of this user
    @GetMapping("/{id}")
    public Bill getOneBill(@PathVariable Integer id, HttpServletRequest request) {
        int userId = getLoggedInUserId(request);
        Optional<Bill> result = billRepository.findByIdAndUserId(id, userId);
        if (!result.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill not found");
        }
        return result.get();
    }

    // POST /api/bills : add a new bill for this user
    @PostMapping
    public Bill addBill(@RequestBody Bill newBill, HttpServletRequest request) {
        int userId = getLoggedInUserId(request);
        // Stop here if the values are bad
        checkBill(newBill);
        // Empty id tells the database to create a new row
        newBill.setId(null);
        // Always use the logged-in user, never a user id sent by the browser
        newBill.setUserId(userId);
        // The server decides these two, never the browser
        newBill.setPaidMonth(null);
        newBill.setCreatedMonth(YearMonth.now(ZONE).toString());
        return billRepository.save(newBill);
    }

    // PUT /api/bills/3 : change an existing bill of this user
    @PutMapping("/{id}")
    public Bill updateBill(@PathVariable Integer id, @RequestBody Bill changedBill, HttpServletRequest request) {
        int userId = getLoggedInUserId(request);
        // Stop here if the values are bad
        checkBill(changedBill);
        Optional<Bill> existing = billRepository.findByIdAndUserId(id, userId);
        if (!existing.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill not found");
        }
        // Same id means save() updates the old row
        changedBill.setId(id);
        changedBill.setUserId(userId);
        // Keep the stored values. The browser cannot change them
        changedBill.setPaidMonth(existing.get().getPaidMonth());
        changedBill.setCreatedMonth(existing.get().getCreatedMonth());
        return billRepository.save(changedBill);
    }

    // DELETE /api/bills/3 : remove a bill of this user
    @DeleteMapping("/{id}")
    public void deleteBill(@PathVariable Integer id, HttpServletRequest request) {
        int userId = getLoggedInUserId(request);
        Optional<Bill> existing = billRepository.findByIdAndUserId(id, userId);
        if (!existing.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill not found");
        }
        billRepository.delete(existing.get());
    }

    // POST /api/bills/3/toggle-paid
    // Paid this month -> unpay this month. Otherwise pay the oldest unpaid month
    @PostMapping("/{id}/toggle-paid")
    public Bill togglePaid(@PathVariable Integer id, HttpServletRequest request) {
        int userId = getLoggedInUserId(request);
        Optional<Bill> result = billRepository.findByIdAndUserId(id, userId);
        if (!result.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill not found");
        }
        Bill bill = result.get();

        YearMonth thisMonth = YearMonth.now(ZONE);
        YearMonth created = thisMonth;
        if (bill.getCreatedMonth() != null) {
            created = YearMonth.parse(bill.getCreatedMonth());
        }
        // Nothing paid yet means paid through the month before creation.
        // A paid month older than that is ignored (months before creation are never owed)
        YearMonth beforeCreated = created.minusMonths(1);
        YearMonth paidThrough = beforeCreated;
        if (bill.getPaidMonth() != null) {
            paidThrough = YearMonth.parse(bill.getPaidMonth());
            if (paidThrough.isBefore(beforeCreated)) {
                paidThrough = beforeCreated;
            }
        }

        if (!paidThrough.isBefore(thisMonth)) {
            paidThrough = thisMonth.minusMonths(1); // undo this month
        } else {
            paidThrough = paidThrough.plusMonths(1); // pay the oldest unpaid month
        }

        if (paidThrough.isBefore(created)) {
            bill.setPaidMonth(null);
        } else {
            bill.setPaidMonth(paidThrough.toString());
        }
        bill.setCreatedMonth(created.toString()); // saves the baseline for old rows
        return billRepository.save(bill);
    }
}
