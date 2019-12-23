package com.creditunion.loan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Data Transfer Object for payment processing.
 *
 * @author Loan System Team
 * @version 1.0
 * @since 2019-05-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDTO {

    /**
     * Total payment amount.
     */
    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be positive")
    private BigDecimal paymentAmount;

    /**
     * Principal portion of payment.
     */
    @NotNull(message = "Principal amount is required")
    @DecimalMin(value = "0.00", message = "Principal amount cannot be negative")
    private BigDecimal principalAmount;

    /**
     * Interest portion of payment.
     */
    @NotNull(message = "Interest amount is required")
    @DecimalMin(value = "0.00", message = "Interest amount cannot be negative")
    private BigDecimal interestAmount;

    /**
     * Payment method (ACH, Check, Card, Cash).
     */
    private String paymentMethod;

    /**
     * External transaction ID from payment gateway.
     */
    private String transactionId;
}
