package com.creditunion.loan.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entity representing documents associated with a loan.
 * Stores document metadata with actual files stored in object storage (S3).
 *
 * @author Loan System Team
 * @version 1.0
 * @since 2019-05-15
 */
@Entity
@Table(name = "loan_documents", indexes = {
        @Index(name = "idx_doc_loan_id", columnList = "loan_id"),
        @Index(name = "idx_doc_type", columnList = "document_type"),
        @Index(name = "idx_doc_verified", columnList = "verified")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id")
    private Long documentId;

    /**
     * Associated loan.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    @NotNull(message = "Loan is required")
    private Loan loan;

    /**
     * Type of document.
     */
    @NotNull(message = "Document type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 50)
    private DocumentType documentType;

    /**
     * Original filename as uploaded.
     */
    @NotBlank(message = "Filename is required")
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    /**
     * S3 bucket URL or file storage path.
     */
    @NotBlank(message = "Document URL is required")
    @Column(name = "document_url", nullable = false, length = 500)
    private String documentUrl;

    /**
     * File size in bytes.
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * MIME type of the file.
     */
    @Column(name = "mime_type", length = 100)
    private String mimeType;

    /**
     * Indicates if document has been verified by staff.
     */
    @Builder.Default
    @Column(name = "verified")
    private Boolean verified = false;

    /**
     * User ID who verified the document.
     */
    @Column(name = "verified_by")
    private Long verifiedBy;

    /**
     * Date document was verified.
     */
    @Column(name = "verified_date")
    private LocalDateTime verifiedDate;

    /**
     * Verification notes or issues.
     */
    @Column(name = "verification_notes", length = 500)
    private String verificationNotes;

    /**
     * User ID who uploaded the document.
     */
    @Column(name = "uploaded_by")
    private Long uploadedBy;

    /**
     * Date document was uploaded.
     */
    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    /**
     * Document expiration date (for time-sensitive docs like pay stubs).
     */
    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

    /**
     * Indicates if document is marked for deletion.
     */
    @Builder.Default
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    /**
     * Marks document as verified.
     */
    public void verify(Long verifierId, String notes) {
        this.verified = true;
        this.verifiedBy = verifierId;
        this.verifiedDate = LocalDateTime.now();
        this.verificationNotes = notes;
    }

    /**
     * Checks if document is expired.
     */
    public boolean isExpired() {
        return expirationDate != null && LocalDateTime.now().isAfter(expirationDate);
    }
}

/**
 * Enum representing types of loan documents.
 */
enum DocumentType {
    /**
     * Government-issued ID (Driver's License, Passport).
     */
    IDENTIFICATION,

    /**
     * Pay stub from employer.
     */
    PAY_STUB,

    /**
     * W-2 tax form.
     */
    W2_FORM,

    /**
     * Tax return (1040).
     */
    TAX_RETURN,

    /**
     * Bank statement.
     */
    BANK_STATEMENT,

    /**
     * Employment verification letter.
     */
    EMPLOYMENT_VERIFICATION,

    /**
     * Credit report.
     */
    CREDIT_REPORT,

    /**
     * Property appraisal (for mortgage/auto loans).
     */
    APPRAISAL,

    /**
     * Vehicle title (for auto loans).
     */
    VEHICLE_TITLE,

    /**
     * Insurance document.
     */
    INSURANCE,

    /**
     * Loan agreement signed by customer.
     */
    LOAN_AGREEMENT,

    /**
     * Promissory note.
     */
    PROMISSORY_NOTE,

    /**
     * Other supporting documents.
     */
    OTHER
}
