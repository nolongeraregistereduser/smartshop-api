package com.smartshop.api.repository;

import com.smartshop.api.entity.Payment;
import com.smartshop.api.enums.PaymentMethod;
import com.smartshop.api.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByOrderId(Long orderId);

    List<Payment> findByStatut(PaymentStatus statut);

    List<Payment> findByOrderIdAndStatut(Long orderId, PaymentStatus statut);

    List<Payment> findByPaymentMethod(PaymentMethod paymentMethod);

    // calculate totale paid for an order


    @Query("SELECT COALESCE(SUM(p.montant), 0.0) FROM Payment p WHERE p.order.id = :orderId AND p.statut = 'ENCAISSE'")
    Double sumTotalPaidByOrderId(Long orderId);

    List<Payment> findByOrderIdAndPaymentMethod(Long orderId, PaymentMethod paymentMethod);

    List<Payment> findByStatutAndDateEcheance(PaymentStatus statut, LocalDate dateEcheance);




}
