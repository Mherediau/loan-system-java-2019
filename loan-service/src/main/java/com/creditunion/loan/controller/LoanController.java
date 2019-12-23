package com.creditunion.loan.controller;

import com.creditunion.loan.dto.LoanDTO;
import com.creditunion.loan.dto.PaymentDTO;
import com.creditunion.loan.entity.Loan;
import com.creditunion.loan.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller for loan management operations.
 * Provides endpoints for loan creation, approval, disbursement,
 * payment processing, and status tracking.
 *
 * @author Loan System Team
 * @version 1.0
 * @since 2019-05-15
 */
@RestController
@RequestMapping("/api/loans")
@Tag(name = "Loan Management", description = "APIs for loan lifecycle management")
@Slf4j
@RequiredArgsConstructor
@Validated
public class LoanController {

    private final LoanService loanService;

    /**
     * Creates a new loan from approved application.
     *
     * @param loanDTO loan creation request
     * @return created loan with HTTP 201
     */
    @PostMapping
    @Operation(summary = "Create a new loan", description = "Creates a new loan record from an approved loan application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Loan created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid loan data"),
            @ApiResponse(responseCode = "409", description = "Customer exceeds maximum active loans")
    })
    public ResponseEntity<Loan> createLoan(@Valid @RequestBody LoanDTO loanDTO) {
        log.info("REST request to create loan for customer: {}", loanDTO.getCustomerId());

        Loan createdLoan = loanService.createLoan(loanDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdLoan);
    }

    /**
     * Retrieves loan by ID.
     *
     * @param loanId loan ID
     * @return loan details with HTTP 200
     */
    @GetMapping("/{loanId}")
    @Operation(summary = "Get loan by ID", description = "Retrieves loan details by loan ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loan found"),
            @ApiResponse(responseCode = "404", description = "Loan not found")
    })
    public ResponseEntity<Loan> getLoanById(
            @Parameter(description = "Loan ID", required = true) @PathVariable @NotNull Long loanId) {
        log.info("REST request to get loan: {}", loanId);

        Loan loan = loanService.getLoanById(loanId);

        return ResponseEntity.ok(loan);
    }

    /**
     * Retrieves loan by loan number.
     *
     * @param loanNumber unique loan number
     * @return loan details with HTTP 200
     */
    @GetMapping("/number/{loanNumber}")
    @Operation(summary = "Get loan by loan number", description = "Retrieves loan details by unique loan number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loan found"),
            @ApiResponse(responseCode = "404", description = "Loan not found")
    })
    public ResponseEntity<Loan> getLoanByNumber(
            @Parameter(description = "Loan number (e.g., LOAN-2019-000123)", required = true) @PathVariable String loanNumber) {
        log.info("REST request to get loan by number: {}", loanNumber);

        Loan loan = loanService.getLoanByNumber(loanNumber);

        return ResponseEntity.ok(loan);
    }

    /**
     * Retrieves all loans for a customer with pagination.
     *
     * @param customerId customer ID
     * @param pageable   pagination parameters
     * @return page of loans with HTTP 200
     */
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get customer loans", description = "Retrieves all loans for a specific customer with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loans retrieved successfully")
    })
    public ResponseEntity<Page<Loan>> getCustomerLoans(
            @Parameter(description = "Customer ID", required = true) @PathVariable @NotNull Long customerId,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        log.info("REST request to get loans for customer: {}", customerId);

        Page<Loan> loans = loanService.getCustomerLoans(customerId, pageable);

        return ResponseEntity.ok(loans);
    }

    /**
     * Retrieves active loans for a customer.
     *
     * @param customerId customer ID
     * @return list of active loans with HTTP 200
     */
    @GetMapping("/customer/{customerId}/active")
    @Operation(summary = "Get active customer loans", description = "Retrieves all active loans for a specific customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Active loans retrieved successfully")
    })
    public ResponseEntity<List<Loan>> getActiveLoans(
            @Parameter(description = "Customer ID", required = true) @PathVariable @NotNull Long customerId) {
        log.info("REST request to get active loans for customer: {}", customerId);

        List<Loan> activeLoans = loanService.getActiveLoans(customerId);

        return ResponseEntity.ok(activeLoans);
    }

    /**
     * Disburses an approved loan.
     *
     * @param loanId loan ID
     * @return disbursed loan with HTTP 200
     */
    @PutMapping("/{loanId}/disburse")
    @Operation(summary = "Disburse loan", description = "Disburses funds for an approved loan and activates it")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loan disbursed successfully"),
            @ApiResponse(responseCode = "400", description = "Loan not in approved status"),
            @ApiResponse(responseCode = "404", description = "Loan not found")
    })
    public ResponseEntity<Loan> disburseLoan(
            @Parameter(description = "Loan ID", required = true) @PathVariable @NotNull Long loanId) {
        log.info("REST request to disburse loan: {}", loanId);

        Loan disbursedLoan = loanService.disburseLoan(loanId);

        return ResponseEntity.ok(disbursedLoan);
    }

    /**
     * Processes a payment on a loan.
     *
     * @param loanId     loan ID
     * @param paymentDTO payment details
     * @return updated loan with HTTP 200
     */
    @PostMapping("/{loanId}/payments")
    @Operation(summary = "Process loan payment", description = "Records and applies a payment to a loan")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid payment or loan status"),
            @ApiResponse(responseCode = "404", description = "Loan not found")
    })
    public ResponseEntity<Loan> processPayment(
            @Parameter(description = "Loan ID", required = true) @PathVariable @NotNull Long loanId,
            @Valid @RequestBody PaymentDTO paymentDTO) {
        log.info("REST request to process payment of {} for loan: {}",
                paymentDTO.getPaymentAmount(), loanId);

        Loan updatedLoan = loanService.processPayment(
                loanId,
                paymentDTO.getPaymentAmount(),
                paymentDTO.getPrincipalAmount(),
                paymentDTO.getInterestAmount());

        return ResponseEntity.ok(updatedLoan);
    }

    /**
     * Marks a loan as delinquent.
     *
     * @param loanId      loan ID
     * @param daysOverdue days past due
     * @return updated loan with HTTP 200
     */
    @PutMapping("/{loanId}/delinquent")
    @Operation(summary = "Mark loan as delinquent", description = "Updates loan status to delinquent with days overdue")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loan marked as delinquent"),
            @ApiResponse(responseCode = "404", description = "Loan not found")
    })
    public ResponseEntity<Loan> markDelinquent(
            @Parameter(description = "Loan ID", required = true) @PathVariable @NotNull Long loanId,
            @Parameter(description = "Days past due", required = true) @RequestParam @NotNull Integer daysOverdue) {
        log.info("REST request to mark loan {} as delinquent with {} days overdue",
                loanId, daysOverdue);

        Loan updatedLoan = loanService.markDelinquent(loanId, daysOverdue);

        return ResponseEntity.ok(updatedLoan);
    }

    /**
     * Closes a fully paid loan.
     *
     * @param loanId loan ID
     * @return closed loan with HTTP 200
     */
    @PutMapping("/{loanId}/close")
    @Operation(summary = "Close loan", description = "Closes a fully paid off loan")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loan closed successfully"),
            @ApiResponse(responseCode = "400", description = "Loan has outstanding balance"),
            @ApiResponse(responseCode = "404", description = "Loan not found")
    })
    public ResponseEntity<Loan> closeLoan(
            @Parameter(description = "Loan ID", required = true) @PathVariable @NotNull Long loanId) {
        log.info("REST request to close loan: {}", loanId);

        Loan closedLoan = loanService.closeLoan(loanId);

        return ResponseEntity.ok(closedLoan);
    }

    /**
     * Retrieves all delinquent loans.
     *
     * @return list of delinquent loans with HTTP 200
     */
    @GetMapping("/delinquent")
    @Operation(summary = "Get delinquent loans", description = "Retrieves all loans with overdue payments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delinquent loans retrieved successfully")
    })
    public ResponseEntity<List<Loan>> getDelinquentLoans() {
        log.info("REST request to get delinquent loans");

        List<Loan> delinquentLoans = loanService.getDelinquentLoans();

        return ResponseEntity.ok(delinquentLoans);
    }

    /**
     * Retrieves loans with upcoming payments.
     *
     * @param withinDays number of days to look ahead
     * @return list of loans with HTTP 200
     */
    @GetMapping("/upcoming-payments")
    @Operation(summary = "Get loans with upcoming payments", description = "Retrieves loans with payments due within specified days")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loans retrieved successfully")
    })
    public ResponseEntity<List<Loan>> getLoansWithUpcomingPayments(
            @Parameter(description = "Number of days to look ahead", required = true) @RequestParam(defaultValue = "7") Integer withinDays) {
        log.info("REST request to get loans with payments due in {} days", withinDays);

        List<Loan> loans = loanService.getLoansWithUpcomingPayments(withinDays);

        return ResponseEntity.ok(loans);
    }

    /**
     * Calculates total outstanding balance for a customer.
     *
     * @param customerId customer ID
     * @return total balance with HTTP 200
     */
    @GetMapping("/customer/{customerId}/total-balance")
    @Operation(summary = "Get customer total outstanding balance", description = "Calculates total outstanding balance across all active loans")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Total balance calculated successfully")
    })
    public ResponseEntity<BigDecimal> getTotalOutstandingBalance(
            @Parameter(description = "Customer ID", required = true) @PathVariable @NotNull Long customerId) {
        log.info("REST request to get total outstanding balance for customer: {}", customerId);

        BigDecimal totalBalance = loanService.getTotalOutstandingBalance(customerId);

        return ResponseEntity.ok(totalBalance);
    }

    /**
     * Health check endpoint.
     *
     * @return status message with HTTP 200
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Service health check endpoint")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Loan Service is running");
    }
}
