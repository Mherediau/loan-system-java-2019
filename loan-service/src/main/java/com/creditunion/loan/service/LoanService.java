package com.creditunion.loan.service;

import com.creditunion.loan.dto.LoanDTO;
import com.creditunion.loan.entity.Loan;
import com.creditunion.loan.entity.LoanStatus;
import com.creditunion.loan.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service class for loan business logic.
 * Handles loan lifecycle operations including approval, disbursement,
 * payment processing, and delinquency management.
 *
 * @author Loan System Team
 * @version 1.0
 * @since 2019-05-15
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoanService {

    private final LoanRepository loanRepository;
    private final PaymentScheduleService paymentScheduleService;

    /**
     * Retrieves loan by ID with caching.
     *
     * @param loanId loan ID
     * @return loan details
     */
    @Cacheable(value = "loans", key = "#loanId")
    public Loan getLoanById(Long loanId) {
        log.info("Fetching loan with ID: {}", loanId);
        return loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + loanId));
    }

    /**
     * Retrieves loan by loan number.
     *
     * @param loanNumber unique loan number
     * @return loan details
     */
    public Loan getLoanByNumber(String loanNumber) {
        log.info("Fetching loan with number: {}", loanNumber);
        return loanRepository.findByLoanNumber(loanNumber)
                .orElseThrow(() -> new RuntimeException("Loan not found with number: " + loanNumber));
    }

    /**
     * Retrieves all loans for a customer with pagination.
     *
     * @param customerId customer ID
     * @param pageable   pagination parameters
     * @return page of loans
     */
    public Page<Loan> getCustomerLoans(Long customerId, Pageable pageable) {
        log.info("Fetching loans for customer: {}", customerId);
        return loanRepository.findByCustomerId(customerId, pageable);
    }

    /**
     * Retrieves active loans for a customer.
     *
     * @param customerId customer ID
     * @return list of active loans
     */
    public List<Loan> getActiveLoans(Long customerId) {
        log.info("Fetching active loans for customer: {}", customerId);
        return loanRepository.findActiveLoansForCustomer(customerId);
    }

    /**
     * Creates a new loan from approved application.
     *
     * @param loanDTO loan data transfer object
     * @return created loan
     */
    @Transactional
    @CacheEvict(value = "loans", allEntries = true)
    public Loan createLoan(LoanDTO loanDTO) {
        log.info("Creating new loan for customer: {}", loanDTO.getCustomerId());

        // Validate customer doesn't exceed maximum active loans
        Long activeLoanCount = loanRepository.countActiveLoansForCustomer(loanDTO.getCustomerId());
        if (activeLoanCount >= 5) {
            throw new RuntimeException("Customer has reached maximum active loans limit");
        }

        // Generate unique loan number
        String loanNumber = generateLoanNumber();

        // Calculate monthly payment using amortization formula
        BigDecimal monthlyPayment = calculateMonthlyPayment(
                loanDTO.getLoanAmount(),
                loanDTO.getInterestRate(),
                loanDTO.getTermMonths());

        // Create loan entity
        Loan loan = Loan.builder()
                .customerId(loanDTO.getCustomerId())
                .loanNumber(loanNumber)
                .loanType(loanDTO.getLoanType())
                .loanAmount(loanDTO.getLoanAmount())
                .interestRate(loanDTO.getInterestRate())
                .termMonths(loanDTO.getTermMonths())
                .monthlyPayment(monthlyPayment)
                .outstandingBalance(loanDTO.getLoanAmount())
                .totalInterestPaid(BigDecimal.ZERO)
                .totalPrincipalPaid(BigDecimal.ZERO)
                .status(LoanStatus.APPROVED)
                .applicationDate(LocalDate.now())
                .approvalDate(LocalDate.now())
                .loanPurpose(loanDTO.getLoanPurpose())
                .approvedBy(loanDTO.getApprovedBy())
                .applicationId(loanDTO.getApplicationId())
                .daysPastDue(0)
                .missedPayments(0)
                .isDefaulted(false)
                .isWrittenOff(false)
                .isDeleted(false)
                .build();

        loan = loanRepository.save(loan);
        log.info("Loan created successfully with ID: {} and number: {}", loan.getLoanId(), loanNumber);

        return loan;
    }

    /**
     * Disburses an approved loan.
     *
     * @param loanId loan ID
     * @return disbursed loan
     */
    @Transactional
    @CacheEvict(value = "loans", key = "#loanId")
    public Loan disburseLoan(Long loanId) {
        log.info("Disbursing loan: {}", loanId);

        Loan loan = getLoanById(loanId);

        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new RuntimeException("Only approved loans can be disbursed");
        }

        // Update loan status and dates
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setDisbursementDate(LocalDate.now());

        // Calculate first payment date (typically 30 days after disbursement)
        LocalDate firstPaymentDate = LocalDate.now().plusDays(30);
        loan.setFirstPaymentDate(firstPaymentDate);
        loan.setNextPaymentDate(firstPaymentDate);

        // Calculate maturity date
        LocalDate maturityDate = firstPaymentDate.plusMonths(loan.getTermMonths() - 1);
        loan.setMaturityDate(maturityDate);

        loan = loanRepository.save(loan);

        // Generate payment schedule
        paymentScheduleService.generateSchedule(loan);

        log.info("Loan {} disbursed successfully. First payment due: {}", loanId, firstPaymentDate);

        return loan;
    }

    /**
     * Processes a payment on a loan.
     *
     * @param loanId          loan ID
     * @param paymentAmount   payment amount
     * @param principalAmount principal portion
     * @param interestAmount  interest portion
     * @return updated loan
     */
    @Transactional
    @CacheEvict(value = "loans", key = "#loanId")
    public Loan processPayment(Long loanId, BigDecimal paymentAmount,
            BigDecimal principalAmount, BigDecimal interestAmount) {
        log.info("Processing payment of {} for loan: {}", paymentAmount, loanId);

        Loan loan = getLoanById(loanId);

        if (loan.getStatus() != LoanStatus.ACTIVE && loan.getStatus() != LoanStatus.DELINQUENT) {
            throw new RuntimeException("Cannot process payment for loan in status: " + loan.getStatus());
        }

        // Apply payment to loan
        loan.applyPayment(principalAmount, interestAmount);

        // Update next payment date
        if (loan.getStatus() == LoanStatus.ACTIVE || loan.getStatus() == LoanStatus.DELINQUENT) {
            LocalDate nextPaymentDate = loan.getNextPaymentDate().plusMonths(1);
            loan.setNextPaymentDate(nextPaymentDate);
        }

        loan = loanRepository.save(loan);

        log.info("Payment processed successfully for loan: {}. New balance: {}",
                loanId, loan.getOutstandingBalance());

        return loan;
    }

    /**
     * Marks a loan as delinquent.
     *
     * @param loanId      loan ID
     * @param daysOverdue days past due
     * @return updated loan
     */
    @Transactional
    @CacheEvict(value = "loans", key = "#loanId")
    public Loan markDelinquent(Long loanId, int daysOverdue) {
        log.warn("Marking loan {} as delinquent with {} days overdue", loanId, daysOverdue);

        Loan loan = getLoanById(loanId);
        loan.markDelinquent(daysOverdue);

        loan = loanRepository.save(loan);

        log.info("Loan {} status updated to: {}", loanId, loan.getStatus());

        return loan;
    }

    /**
     * Closes a fully paid loan.
     *
     * @param loanId loan ID
     * @return closed loan
     */
    @Transactional
    @CacheEvict(value = "loans", key = "#loanId")
    public Loan closeLoan(Long loanId) {
        log.info("Closing loan: {}", loanId);

        Loan loan = getLoanById(loanId);

        if (loan.getOutstandingBalance().compareTo(BigDecimal.ONE) > 0) {
            throw new RuntimeException("Cannot close loan with outstanding balance");
        }

        loan.setStatus(LoanStatus.CLOSED);
        loan.setClosedDate(LocalDate.now());
        loan.setOutstandingBalance(BigDecimal.ZERO);

        loan = loanRepository.save(loan);

        log.info("Loan {} closed successfully", loanId);

        return loan;
    }

    /**
     * Retrieves delinquent loans requiring action.
     *
     * @return list of delinquent loans
     */
    public List<Loan> getDelinquentLoans() {
        log.info("Fetching delinquent loans");
        return loanRepository.findDelinquentLoans();
    }

    /**
     * Retrieves loans with upcoming payments within days.
     *
     * @param withinDays number of days to look ahead
     * @return list of loans with upcoming payments
     */
    public List<Loan> getLoansWithUpcomingPayments(int withinDays) {
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(withinDays);

        log.info("Fetching loans with payments due between {} and {}", today, futureDate);
        return loanRepository.findLoansWithUpcomingPayments(today, futureDate);
    }

    /**
     * Calculates total outstanding balance for a customer.
     *
     * @param customerId customer ID
     * @return total balance
     */
    public BigDecimal getTotalOutstandingBalance(Long customerId) {
        return loanRepository.calculateTotalOutstandingBalance(customerId);
    }

    /**
     * Generates unique loan number.
     * Format: LOAN-YYYY-NNNNNN
     *
     * @return loan number
     */
    private String generateLoanNumber() {
        int year = LocalDate.now().getYear();
        long count = loanRepository.count() + 1;
        return String.format("LOAN-%d-%06d", year, count);
    }

    /**
     * Calculates monthly payment using amortization formula.
     * Formula: M = P * [r(1+r)^n] / [(1+r)^n - 1]
     * Where: M = monthly payment, P = principal, r = monthly rate, n = number of
     * payments
     *
     * @param principal  loan amount
     * @param annualRate annual interest rate (percentage)
     * @param termMonths loan term in months
     * @return monthly payment amount
     */
    private BigDecimal calculateMonthlyPayment(BigDecimal principal, BigDecimal annualRate, Integer termMonths) {
        // Convert annual rate to monthly decimal rate
        BigDecimal monthlyRate = annualRate.divide(new BigDecimal("1200"), 10, BigDecimal.ROUND_HALF_UP);

        // Calculate (1 + r)^n
        double onePlusR = 1 + monthlyRate.doubleValue();
        double power = Math.pow(onePlusR, termMonths);

        // Calculate monthly payment
        BigDecimal numerator = monthlyRate.multiply(new BigDecimal(power));
        BigDecimal denominator = new BigDecimal(power - 1);

        BigDecimal monthlyPayment = principal.multiply(numerator.divide(denominator, 2, BigDecimal.ROUND_HALF_UP));

        return monthlyPayment.setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}

/**
 * Placeholder for PaymentScheduleService (would be implemented separately).
 */
@Service
@RequiredArgsConstructor
class PaymentScheduleService {
    public void generateSchedule(Loan loan) {
        // Implementation for generating amortization schedule
    }
}
