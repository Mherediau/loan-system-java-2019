package com.creditunion.loan.repository;

import com.creditunion.loan.entity.Loan;
import com.creditunion.loan.entity.LoanStatus;
import com.creditunion.loan.entity.LoanType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Loan entity.
 * Provides CRUD operations and custom queries for loan management.
 *
 * @author Loan System Team
 * @version 1.0
 * @since 2019-05-15
 */
@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    /**
     * Finds loan by unique loan number.
     *
     * @param loanNumber unique loan account number
     * @return Optional containing loan if found
     */
    Optional<Loan> findByLoanNumber(String loanNumber);

    /**
     * Finds all loans for a specific customer.
     *
     * @param customerId customer ID
     * @param pageable   pagination parameters
     * @return page of loans
     */
    Page<Loan> findByCustomerId(Long customerId, Pageable pageable);

    /**
     * Finds all active loans for a customer.
     *
     * @param customerId customer ID
     * @return list of active loans
     */
    @Query("SELECT l FROM Loan l WHERE l.customerId = :customerId " +
            "AND l.status IN ('ACTIVE', 'DELINQUENT') " +
            "AND l.isDeleted = false")
    List<Loan> findActiveLoansForCustomer(@Param("customerId") Long customerId);

    /**
     * Finds loans by status.
     *
     * @param status   loan status
     * @param pageable pagination parameters
     * @return page of loans
     */
    Page<Loan> findByStatus(LoanStatus status, Pageable pageable);

    /**
     * Finds loans by type and status.
     *
     * @param loanType type of loan
     * @param status   loan status
     * @param pageable pagination parameters
     * @return page of loans
     */
    Page<Loan> findByLoanTypeAndStatus(LoanType loanType, LoanStatus status, Pageable pageable);

    /**
     * Finds all delinquent loans (past due).
     *
     * @return list of delinquent loans
     */
    @Query("SELECT l FROM Loan l WHERE l.daysPastDue > 0 " +
            "AND l.status IN ('ACTIVE', 'DELINQUENT') " +
            "AND l.isDeleted = false")
    List<Loan> findDelinquentLoans();

    /**
     * Finds loans with next payment due within specified date range.
     *
     * @param startDate start date
     * @param endDate   end date
     * @return list of loans with upcoming payments
     */
    @Query("SELECT l FROM Loan l WHERE l.nextPaymentDate BETWEEN :startDate AND :endDate " +
            "AND l.status = 'ACTIVE' AND l.isDeleted = false")
    List<Loan> findLoansWithUpcomingPayments(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Finds loans maturing within specified date range.
     *
     * @param startDate start date
     * @param endDate   end date
     * @return list of maturing loans
     */
    @Query("SELECT l FROM Loan l WHERE l.maturityDate BETWEEN :startDate AND :endDate " +
            "AND l.status = 'ACTIVE' AND l.isDeleted = false")
    List<Loan> findMaturingLoans(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Calculates total outstanding balance for a customer.
     *
     * @param customerId customer ID
     * @return total outstanding balance
     */
    @Query("SELECT COALESCE(SUM(l.outstandingBalance), 0) FROM Loan l " +
            "WHERE l.customerId = :customerId " +
            "AND l.status IN ('ACTIVE', 'DELINQUENT') " +
            "AND l.isDeleted = false")
    BigDecimal calculateTotalOutstandingBalance(@Param("customerId") Long customerId);

    /**
     * Counts active loans for a customer.
     *
     * @param customerId customer ID
     * @return number of active loans
     */
    @Query("SELECT COUNT(l) FROM Loan l WHERE l.customerId = :customerId " +
            "AND l.status IN ('ACTIVE', 'DELINQUENT') " +
            "AND l.isDeleted = false")
    Long countActiveLoansForCustomer(@Param("customerId") Long customerId);

    /**
     * Finds loans approved by a specific user.
     *
     * @param userId   approver user ID
     * @param pageable pagination parameters
     * @return page of loans
     */
    Page<Loan> findByApprovedBy(Long userId, Pageable pageable);

    /**
     * Searches loans by customer ID and loan type.
     *
     * @param customerId customer ID
     * @param loanType   loan type
     * @return list of matching loans
     */
    List<Loan> findByCustomerIdAndLoanType(Long customerId, LoanType loanType);

    /**
     * Finds loans with outstanding balance greater than specified amount.
     *
     * @param minBalance minimum balance
     * @return list of loans
     */
    @Query("SELECT l FROM Loan l WHERE l.outstandingBalance > :minBalance " +
            "AND l.status = 'ACTIVE' AND l.isDeleted = false")
    List<Loan> findLoansWithBalanceGreaterThan(@Param("minBalance") BigDecimal minBalance);

    /**
     * Finds loans in default status (90+ days overdue).
     *
     * @return list of defaulted loans
     */
    @Query("SELECT l FROM Loan l WHERE l.status = 'DEFAULT' " +
            "OR l.isDefaulted = true " +
            "AND l.isDeleted = false")
    List<Loan> findDefaultedLoans();

    /**
     * Calculates portfolio statistics by loan type.
     *
     * @return list of objects containing loanType, count, totalAmount,
     *         totalOutstanding
     */
    @Query("SELECT l.loanType as loanType, " +
            "COUNT(l) as count, " +
            "SUM(l.loanAmount) as totalAmount, " +
            "SUM(l.outstandingBalance) as totalOutstanding " +
            "FROM Loan l " +
            "WHERE l.status IN ('ACTIVE', 'DELINQUENT') " +
            "AND l.isDeleted = false " +
            "GROUP BY l.loanType")
    List<Object[]> calculatePortfolioStatistics();

    /**
     * Finds loans with missed payments exceeding threshold.
     *
     * @param minMissedPayments minimum number of missed payments
     * @return list of loans
     */
    @Query("SELECT l FROM Loan l WHERE l.missedPayments >= :minMissedPayments " +
            "AND l.status IN ('ACTIVE', 'DELINQUENT', 'DEFAULT') " +
            "AND l.isDeleted = false")
    List<Loan> findLoansWithMissedPayments(@Param("minMissedPayments") Integer minMissedPayments);

    /**
     * Finds loans disbursed within date range.
     *
     * @param startDate start date
     * @param endDate   end date
     * @return list of loans
     */
    @Query("SELECT l FROM Loan l WHERE l.disbursementDate BETWEEN :startDate AND :endDate " +
            "AND l.isDeleted = false")
    List<Loan> findLoansDisbursedBetween(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Checks if customer has any active loans.
     *
     * @param customerId customer ID
     * @return true if customer has active loans
     */
    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM Loan l " +
            "WHERE l.customerId = :customerId " +
            "AND l.status IN ('ACTIVE', 'DELINQUENT') " +
            "AND l.isDeleted = false")
    boolean hasActiveLoans(@Param("customerId") Long customerId);

    /**
     * Finds loans requiring payment update (next payment date passed).
     *
     * @param currentDate current date
     * @return list of loans needing update
     */
    @Query("SELECT l FROM Loan l WHERE l.nextPaymentDate < :currentDate " +
            "AND l.status = 'ACTIVE' AND l.isDeleted = false")
    List<Loan> findLoansRequiringPaymentUpdate(@Param("currentDate") LocalDate currentDate);
}
