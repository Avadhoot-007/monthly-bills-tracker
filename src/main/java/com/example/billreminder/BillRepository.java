package com.example.billreminder;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BillRepository extends JpaRepository<Bill, Integer> {

    // All bills that belong to one user
    List<Bill> findByUserId(Integer userId);

    // One bill, but only if it belongs to that user
    Optional<Bill> findByIdAndUserId(Integer id, Integer userId);
}