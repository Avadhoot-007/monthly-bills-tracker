package com.example.billreminder;

import org.springframework.data.jpa.repository.JpaRepository;

// Spring builds the save, find, and delete methods for us.
// Bill is the table class. Integer is the type of its id.
public interface BillRepository extends JpaRepository<Bill, Integer> {
}