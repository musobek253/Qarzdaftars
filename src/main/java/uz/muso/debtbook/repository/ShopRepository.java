package uz.muso.debtbook.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.muso.debtbook.model.Shop;

public interface ShopRepository extends JpaRepository<Shop, Long> {
}
