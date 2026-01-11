package uz.muso.debtbook.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.muso.debtbook.model.Debt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface DebtRepository extends JpaRepository<Debt, Long> {

    List<Debt> findByShopId(Long shopId);

    @Query(value = """
                SELECT * FROM (
                    SELECT d.id AS id, 'DEBT' AS type, d.total_amount AS amount, d.debt_date AS transactionDate, d.description AS description
                    FROM debts d WHERE d.customer_id = :customerId
                    UNION ALL
                    SELECT p.id AS id, 'PAYMENT' AS type, p.amount AS amount, p.payment_date AS transactionDate, 'To''lov' AS description
                    FROM payments p WHERE p.customer_id = :customerId
                ) AS t
                ORDER BY t.transactionDate DESC NULLS LAST
            """, nativeQuery = true)
    java.util.List<uz.muso.debtbook.dto.TransactionProjection> findAllTransactions(
            @Param("customerId") Long customerId);

    // Statistics Query
    @Query(value = """
                SELECT * FROM (
                    SELECT d.id AS id, 'DEBT' AS type, d.total_amount AS amount, d.debt_date AS transactionDate, c.full_name AS description
                    FROM debts d
                    JOIN customers c ON d.customer_id = c.id
                    WHERE d.shop_id = :shopId
                    UNION ALL
                    SELECT p.id AS id, 'PAYMENT' AS type, p.amount AS amount, p.payment_date AS transactionDate, c.full_name AS description
                    FROM payments p
                    JOIN customers c ON p.customer_id = c.id
                    join shops s on c.shop_id = s.id
                    WHERE s.id = :shopId
                ) AS t
                ORDER BY t.transactionDate DESC NULLS LAST
            """, nativeQuery = true)
    java.util.List<uz.muso.debtbook.dto.TransactionProjection> findAllShopTransactions(
            @Param("shopId") Long shopId);

    List<Debt> findByCustomerId(Long customerId);

    @Query("""
            select sum(d.totalAmount) - (
                select coalesce(sum(p.amount), 0)
                from Payment p
                where p.customer.id = :customerId
            )
            from Debt d
            where d.customer.id = :customerId
            """)
    BigDecimal totalDebtByCustomer(@Param("customerId") Long customerId, @Param("shopId") Long shopId);

    @Query("SELECT SUM(d.totalAmount) FROM Debt d WHERE d.shop.id = :shopId")
    BigDecimal sumTotalAmountByShopId(@Param("shopId") Long shopId);

    List<Debt> findByCustomerIdOrderByDebtDateAsc(Long customerId);

    @Query("""
                select d
                from Debt d
                where d.isClosed = false
                  and d.isReminderSent = false
                  and d.dueDate <= :targetDate
            """)
    List<Debt> findDueSoon(@Param("targetDate") LocalDate targetDate);

}
