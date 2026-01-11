package uz.muso.debtbook.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.muso.debtbook.model.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByShopId(Long shopId);

    @org.springframework.data.jpa.repository.Query("""
                SELECT new uz.muso.debtbook.dto.CustomerDto(
                    c.id,
                    c.fullName,
                    c.phone,
                    (SELECT SUM(d.totalAmount) FROM Debt d WHERE d.customer.id = c.id),
                    (SELECT SUM(p.amount) FROM Payment p WHERE p.customer.id = c.id),
                    (SELECT MIN(d.dueDate) FROM Debt d WHERE d.customer.id = c.id AND d.isClosed = false AND d.dueDate >= CURRENT_DATE)
                )
                FROM Customer c
                WHERE c.shop.id = :shopId
                ORDER BY (SELECT MIN(d.dueDate) FROM Debt d WHERE d.customer.id = c.id AND d.isClosed = false AND d.dueDate >= CURRENT_DATE) ASC NULLS LAST, c.id DESC
            """)
    List<uz.muso.debtbook.dto.CustomerDto> findAllWithBalance(Long shopId);

    Optional<Customer> findByIdAndShopId(Long id, Long shopId);

}
