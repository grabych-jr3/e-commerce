package com.ogidazepam.e_commerce.repository;

import com.ogidazepam.e_commerce.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Long> {
    Optional<Orders> findByIdAndCustomerId(long id, long customerId);
    List<Orders> findAllByCustomerId(long customerId);
}
