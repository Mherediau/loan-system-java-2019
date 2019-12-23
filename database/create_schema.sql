-- =====================================================
-- Loan Management System - PostgreSQL Database Schema
-- Credit Union Loan System 2019
-- Database: loan_db
-- Version: 1.0.0
-- =====================================================

-- Drop existing tables if exist (for clean setup)
DROP TABLE IF EXISTS loan_documents CASCADE;
DROP TABLE IF EXISTS loan_applications CASCADE;
DROP TABLE IF EXISTS loans CASCADE;

-- =====================================================
-- LOANS TABLE
-- =====================================================
CREATE TABLE loans (
    loan_id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    loan_number VARCHAR(20) UNIQUE,
    loan_type VARCHAR(20) NOT NULL CHECK (loan_type IN ('PERSONAL', 'AUTO', 'MORTGAGE', 'HOME_EQUITY', 'BUSINESS')),
    loan_amount DECIMAL(12,2) NOT NULL CHECK (loan_amount >= 1000 AND loan_amount <= 1000000),
    interest_rate DECIMAL(5,2) NOT NULL CHECK (interest_rate >= 0 AND interest_rate <= 36),
    term_months INTEGER NOT NULL CHECK (term_months >= 6 AND term_months <= 360),
    monthly_payment DECIMAL(10,2) NOT NULL,
    outstanding_balance DECIMAL(12,2) NOT NULL,
    total_interest_paid DECIMAL(12,2) DEFAULT 0.00,
    total_principal_paid DECIMAL(12,2) DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' 
        CHECK (status IN ('PENDING', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'ACTIVE', 
                         'DELINQUENT', 'DEFAULT', 'CLOSED', 'WRITTEN_OFF', 'CANCELLED')),
    application_date DATE NOT NULL,
    approval_date DATE,
    disbursement_date DATE,
    maturity_date DATE,
    closed_date DATE,
    first_payment_date DATE,
    next_payment_date DATE,
    days_past_due INTEGER DEFAULT 0,
    missed_payments INTEGER DEFAULT 0,
    loan_purpose VARCHAR(100),
    approved_by BIGINT,
    approval_notes VARCHAR(500),
    collateral_description VARCHAR(500),
    collateral_value DECIMAL(12,2),
    loan_to_value_ratio DECIMAL(5,2),
    application_id BIGINT,
    is_defaulted BOOLEAN DEFAULT FALSE,
    is_written_off BOOLEAN DEFAULT FALSE,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for loans table
CREATE INDEX idx_loan_customer_id ON loans(customer_id);
CREATE INDEX idx_loan_status ON loans(status);
CREATE INDEX idx_loan_type ON loans(loan_type);
CREATE INDEX idx_loan_application_date ON loans(application_date);
CREATE INDEX idx_loan_next_payment_date ON loans(next_payment_date);
CREATE INDEX idx_loan_delinquent ON loans(days_past_due) WHERE days_past_due > 0;

-- Comments for loans table
COMMENT ON TABLE loans IS 'Main table storing all loan records';
COMMENT ON COLUMN loans.loan_number IS 'Unique loan account number (format: LOAN-YYYY-NNNNNN)';
COMMENT ON COLUMN loans.outstanding_balance IS 'Current principal balance remaining';
COMMENT ON COLUMN loans.days_past_due IS 'Number of days payment is overdue';

-- =====================================================
-- LOAN_APPLICATIONS TABLE
-- =====================================================
CREATE TABLE loan_applications (
    application_id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    loan_type VARCHAR(20) NOT NULL CHECK (loan_type IN ('PERSONAL', 'AUTO', 'MORTGAGE', 'HOME_EQUITY', 'BUSINESS')),
    requested_amount DECIMAL(12,2) NOT NULL CHECK (requested_amount >= 1000),
    preferred_term_months INTEGER NOT NULL CHECK (preferred_term_months >= 6 AND preferred_term_months <= 360),
    loan_purpose VARCHAR(200) NOT NULL,
    employment_info JSONB,
    annual_income DECIMAL(12,2) NOT NULL CHECK (annual_income >= 12000),
    monthly_housing_payment DECIMAL(10,2),
    other_monthly_debts DECIMAL(10,2),
    debt_to_income_ratio DECIMAL(5,2),
    credit_score INTEGER,
    credit_bureau VARCHAR(50),
    income_verification JSONB,
    collateral_info JSONB,
    co_borrower_info JSONB,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT'
        CHECK (status IN ('DRAFT', 'SUBMITTED', 'UNDER_REVIEW', 'INFO_REQUESTED', 
                         'APPROVED', 'REJECTED', 'WITHDRAWN', 'EXPIRED')),
    underwriting_decision VARCHAR(20),
    decision_notes VARCHAR(1000),
    approved_amount DECIMAL(12,2),
    approved_rate DECIMAL(5,2),
    approved_term_months INTEGER,
    reviewed_by BIGINT,
    application_date TIMESTAMP,
    reviewed_date TIMESTAMP,
    loan_id BIGINT,
    submission_ip VARCHAR(45),
    user_agent VARCHAR(500),
    fraud_check_completed BOOLEAN DEFAULT FALSE,
    fraud_check_score INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for loan_applications table
CREATE INDEX idx_app_customer_id ON loan_applications(customer_id);
CREATE INDEX idx_app_status ON loan_applications(status);
CREATE INDEX idx_app_date ON loan_applications(application_date);
CREATE INDEX idx_app_credit_score ON loan_applications(credit_score);

-- Comments for loan_applications table
COMMENT ON TABLE loan_applications IS 'Stores all loan application submissions';
COMMENT ON COLUMN loan_applications.employment_info IS 'JSON: employerName, jobTitle, yearsEmployed, monthlyIncome';
COMMENT ON COLUMN loan_applications.income_verification IS 'JSON: verificationType, documentIds, verificationStatus';

-- =====================================================
-- LOAN_DOCUMENTS TABLE
-- =====================================================
CREATE TABLE loan_documents (
    document_id BIGSERIAL PRIMARY KEY,
    loan_id BIGINT NOT NULL,
    document_type VARCHAR(50) NOT NULL 
        CHECK (document_type IN ('IDENTIFICATION', 'PAY_STUB', 'W2_FORM', 'TAX_RETURN', 
                                'BANK_STATEMENT', 'EMPLOYMENT_VERIFICATION', 'CREDIT_REPORT',
                                'APPRAISAL', 'VEHICLE_TITLE', 'INSURANCE', 'LOAN_AGREEMENT',
                                'PROMISSORY_NOTE', 'OTHER')),
    file_name VARCHAR(255) NOT NULL,
    document_url VARCHAR(500) NOT NULL,
    file_size BIGINT,
    mime_type VARCHAR(100),
    verified BOOLEAN DEFAULT FALSE,
    verified_by BIGINT,
    verified_date TIMESTAMP,
    verification_notes VARCHAR(500),
    uploaded_by BIGINT,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expiration_date TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (loan_id) REFERENCES loans(loan_id) ON DELETE CASCADE
);

-- Indexes for loan_documents table
CREATE INDEX idx_doc_loan_id ON loan_documents(loan_id);
CREATE INDEX idx_doc_type ON loan_documents(document_type);
CREATE INDEX idx_doc_verified ON loan_documents(verified);

-- Comments for loan_documents table
COMMENT ON TABLE loan_documents IS 'Stores metadata for loan-related documents';
COMMENT ON COLUMN loan_documents.document_url IS 'S3 URL or file storage path';

-- =====================================================
-- TRIGGER: Update updated_at timestamp
-- =====================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_loans_updated_at BEFORE UPDATE ON loans
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_loan_applications_updated_at BEFORE UPDATE ON loan_applications
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- SAMPLE DATA
-- =====================================================

-- Sample Loans
INSERT INTO loans (customer_id, loan_number, loan_type, loan_amount, interest_rate, 
                   term_months, monthly_payment, outstanding_balance, status, 
                   application_date, approval_date, disbursement_date, first_payment_date,
                   next_payment_date, maturity_date, loan_purpose) VALUES
(1001, 'LOAN-2019-000001', 'PERSONAL', 25000.00, 8.99, 60, 518.45, 18750.00, 'ACTIVE',
 '2019-01-15', '2019-01-20', '2019-01-25', '2019-02-25', '2024-11-25', '2024-01-25', 'Debt Consolidation'),

(1002, 'LOAN-2019-000002', 'AUTO', 35000.00, 5.49, 72, 567.23, 28000.00, 'ACTIVE',
 '2019-03-10', '2019-03-12', '2019-03-15', '2019-04-15', '2024-11-15', '2025-03-15', 'Vehicle Purchase'),

(1003, 'LOAN-2019-000003', 'MORTGAGE', 350000.00, 3.75, 360, 1620.91, 330000.00, 'ACTIVE',
 '2019-05-20', '2019-06-01', '2019-06-15', '2019-07-15', '2024-11-15', '2049-06-15', 'Home Purchase'),

(1004, 'LOAN-2019-000004', 'PERSONAL', 15000.00, 12.99, 48, 402.50, 13500.00, 'DELINQUENT',
 '2019-08-05', '2019-08-10', '2019-08-15', '2019-09-15', '2024-10-15', '2023-08-15', 'Medical Expenses'),

(1005, 'LOAN-2019-000005', 'BUSINESS', 100000.00, 7.25, 120, 1174.15, 85000.00, 'ACTIVE',
 '2019-10-01', '2019-10-10', '2019-10-15', '2019-11-15', '2024-11-15', '2029-10-15', 'Business Expansion');

-- Update delinquent loan
UPDATE loans SET days_past_due = 45, missed_payments = 2 WHERE loan_number = 'LOAN-2019-000004';

-- Sample Loan Applications
INSERT INTO loan_applications (customer_id, loan_type, requested_amount, preferred_term_months,
                               loan_purpose, annual_income, credit_score, status,
                               application_date) VALUES
(1006, 'PERSONAL', 20000.00, 60, 'Home Improvement', 75000.00, 720, 'UNDER_REVIEW',
 CURRENT_TIMESTAMP - INTERVAL '2 days'),

(1007, 'AUTO', 30000.00, 60, 'Vehicle Purchase', 85000.00, 780, 'SUBMITTED',
 CURRENT_TIMESTAMP - INTERVAL '1 day'),

(1008, 'PERSONAL', 50000.00, 84, 'Debt Consolidation', 120000.00, 650, 'DRAFT',
 CURRENT_TIMESTAMP);

-- Sample Loan Documents
INSERT INTO loan_documents (loan_id, document_type, file_name, document_url, 
                           file_size, mime_type, verified, verified_by, verified_date) VALUES
(1, 'IDENTIFICATION', 'drivers_license_1001.pdf', 
 's3://creditunion-docs/loans/1/drivers_license_1001.pdf', 245678, 'application/pdf', 
 TRUE, 5001, CURRENT_TIMESTAMP - INTERVAL '30 days'),

(1, 'PAY_STUB', 'paystub_jan2019_1001.pdf',
 's3://creditunion-docs/loans/1/paystub_jan2019_1001.pdf', 156789, 'application/pdf',
 TRUE, 5001, CURRENT_TIMESTAMP - INTERVAL '30 days'),

(2, 'VEHICLE_TITLE', 'vehicle_title_1002.pdf',
 's3://creditunion-docs/loans/2/vehicle_title_1002.pdf', 189456, 'application/pdf',
 TRUE, 5002, CURRENT_TIMESTAMP - INTERVAL '60 days'),

(3, 'APPRAISAL', 'home_appraisal_1003.pdf',
 's3://creditunion-docs/loans/3/home_appraisal_1003.pdf', 3456789, 'application/pdf',
 TRUE, 5003, CURRENT_TIMESTAMP - INTERVAL '90 days');

-- =====================================================
-- VIEWS
-- =====================================================

-- View: Active Loans Summary
CREATE OR REPLACE VIEW v_active_loans_summary AS
SELECT 
    loan_type,
    COUNT(*) as loan_count,
    SUM(loan_amount) as total_originated,
    SUM(outstanding_balance) as total_outstanding,
    AVG(interest_rate) as avg_interest_rate,
    AVG(days_past_due) as avg_days_past_due
FROM loans
WHERE status IN ('ACTIVE', 'DELINQUENT')
  AND is_deleted = FALSE
GROUP BY loan_type;

-- View: Delinquent Loans
CREATE OR REPLACE VIEW v_delinquent_loans AS
SELECT 
    l.loan_id,
    l.loan_number,
    l.customer_id,
    l.loan_type,
    l.outstanding_balance,
    l.days_past_due,
    l.missed_payments,
    l.next_payment_date,
    l.monthly_payment
FROM loans l
WHERE l.days_past_due > 0
  AND l.status IN ('ACTIVE', 'DELINQUENT', 'DEFAULT')
  AND l.is_deleted = FALSE
ORDER BY l.days_past_due DESC;

-- View: Loan Portfolio Metrics
CREATE OR REPLACE VIEW v_portfolio_metrics AS
SELECT 
    COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) as active_loans,
    COUNT(CASE WHEN status = 'DELINQUENT' THEN 1 END) as delinquent_loans,
    COUNT(CASE WHEN status = 'DEFAULT' THEN 1 END) as defaulted_loans,
    SUM(CASE WHEN status IN ('ACTIVE', 'DELINQUENT') THEN outstanding_balance ELSE 0 END) as total_portfolio_balance,
    SUM(CASE WHEN days_past_due > 0 THEN outstanding_balance ELSE 0 END) as delinquent_balance,
    ROUND(
        (SUM(CASE WHEN days_past_due > 0 THEN outstanding_balance ELSE 0 END) * 100.0) /
        NULLIF(SUM(CASE WHEN status IN ('ACTIVE', 'DELINQUENT') THEN outstanding_balance ELSE 0 END), 0),
        2
    ) as delinquency_rate_pct
FROM loans
WHERE is_deleted = FALSE;

-- =====================================================
-- STORED PROCEDURES
-- =====================================================

-- Procedure: Calculate Monthly Payment
CREATE OR REPLACE FUNCTION calculate_monthly_payment(
    p_principal DECIMAL,
    p_annual_rate DECIMAL,
    p_term_months INTEGER
)
RETURNS DECIMAL AS $$
DECLARE
    v_monthly_rate DECIMAL;
    v_factor DECIMAL;
    v_payment DECIMAL;
BEGIN
    -- Convert annual rate to monthly decimal
    v_monthly_rate := p_annual_rate / 1200.0;
    
    -- Calculate payment using amortization formula
    IF v_monthly_rate = 0 THEN
        v_payment := p_principal / p_term_months;
    ELSE
        v_factor := POWER(1 + v_monthly_rate, p_term_months);
        v_payment := p_principal * (v_monthly_rate * v_factor) / (v_factor - 1);
    END IF;
    
    RETURN ROUND(v_payment, 2);
END;
$$ LANGUAGE plpgsql;

-- Procedure: Get Customer Loan Summary
CREATE OR REPLACE FUNCTION get_customer_loan_summary(p_customer_id BIGINT)
RETURNS TABLE (
    total_loans BIGINT,
    active_loans BIGINT,
    total_borrowed DECIMAL,
    total_outstanding DECIMAL,
    total_paid DECIMAL,
    delinquent_loans BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COUNT(*)::BIGINT as total_loans,
        COUNT(CASE WHEN status IN ('ACTIVE', 'DELINQUENT') THEN 1 END)::BIGINT as active_loans,
        COALESCE(SUM(loan_amount), 0) as total_borrowed,
        COALESCE(SUM(CASE WHEN status IN ('ACTIVE', 'DELINQUENT') THEN outstanding_balance ELSE 0 END), 0) as total_outstanding,
        COALESCE(SUM(total_principal_paid + total_interest_paid), 0) as total_paid,
        COUNT(CASE WHEN days_past_due > 0 THEN 1 END)::BIGINT as delinquent_loans
    FROM loans
    WHERE customer_id = p_customer_id
      AND is_deleted = FALSE;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- GRANTS (adjust as needed for your environment)
-- =====================================================
-- GRANT SELECT, INSERT, UPDATE ON ALL TABLES IN SCHEMA public TO loan_user;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO loan_user;

-- =====================================================
-- END OF SCHEMA
-- =====================================================

-- Display summary
SELECT 'Loan database schema created successfully' as status;
SELECT 'Total loans: ' || COUNT(*) FROM loans;
SELECT 'Total applications: ' || COUNT(*) FROM loan_applications;
SELECT 'Total documents: ' || COUNT(*) FROM loan_documents;
SELECT * FROM v_portfolio_metrics;
