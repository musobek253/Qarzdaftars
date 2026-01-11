package uz.muso.debtbook.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.muso.debtbook.model.Payment;

import java.math.BigDecimal;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("""
                select coalesce(sum(p.amount), 0)
                from Payment p
                where p.customer.id = :customerId
            """)
    BigDecimal totalPaidByCustomer(@Param("customerId") Long customerId);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.customer.shop.id = :shopId")
    BigDecimal sumAmountByShopId(@Param("shopId") Long shopId);

    @Query("SELECT p FROM Payment p WHERE p.customer.shop.id = :shopId")
    java.util.List<Payment> findByShopId(@Param("shopId") Long shopId);

    java.util.List<Payment> findByCustomerId(Long customerId);
}
