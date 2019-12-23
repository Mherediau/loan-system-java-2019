import { useState, useCallback } from 'react';
import { loanService } from '../services/loanService';

/**
 * Custom hook for loan operations
 * Provides loan data fetching and manipulation with loading/error states
 */
export const useLoan = () => {
  const [loans, setLoans] = useState([]);
  const [currentLoan, setCurrentLoan] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  /**
   * Fetches all loans with optional filters
   */
  const fetchLoans = useCallback(async (filters = {}) => {
    setLoading(true);
    setError(null);
    
    try {
      const data = await loanService.getLoans(filters);
      setLoans(data);
      return data;
    } catch (err) {
      setError(err.message || 'Error fetching loans');
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * Fetches a single loan by ID
   */
  const fetchLoanById = useCallback(async (id) => {
    setLoading(true);
    setError(null);
    
    try {
      const data = await loanService.getLoanById(id);
      setCurrentLoan(data);
      return data;
    } catch (err) {
      setError(err.message || 'Error fetching loan details');
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * Submits a new loan application
   */
  const submitApplication = useCallback(async (applicationData) => {
    setLoading(true);
    setError(null);
    
    try {
      const data = await loanService.submitApplication(applicationData);
      return data;
    } catch (err) {
      setError(err.message || 'Error submitting application');
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * Calculates loan payment schedule
   */
  const calculatePaymentSchedule = useCallback(async (loanData) => {
    setLoading(true);
    setError(null);
    
    try {
      const data = await loanService.calculatePaymentSchedule(loanData);
      return data;
    } catch (err) {
      setError(err.message || 'Error calculating payment schedule');
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * Approves a loan
   */
  const approveLoan = useCallback(async (loanId, approvalData) => {
    setLoading(true);
    setError(null);
    
    try {
      const data = await loanService.approveLoan(loanId, approvalData);
      
      // Update local state
      setLoans(prevLoans =>
        prevLoans.map(loan =>
          loan.id === loanId ? { ...loan, status: 'APPROVED' } : loan
        )
      );
      
      if (currentLoan && currentLoan.id === loanId) {
        setCurrentLoan(prev => ({ ...prev, status: 'APPROVED' }));
      }
      
      return data;
    } catch (err) {
      setError(err.message || 'Error approving loan');
      throw err;
    } finally {
      setLoading(false);
    }
  }, [currentLoan]);

  /**
   * Rejects a loan
   */
  const rejectLoan = useCallback(async (loanId, rejectionReason) => {
    setLoading(true);
    setError(null);
    
    try {
      const data = await loanService.rejectLoan(loanId, rejectionReason);
      
      // Update local state
      setLoans(prevLoans =>
        prevLoans.map(loan =>
          loan.id === loanId ? { ...loan, status: 'REJECTED' } : loan
        )
      );
      
      if (currentLoan && currentLoan.id === loanId) {
        setCurrentLoan(prev => ({ ...prev, status: 'REJECTED' }));
      }
      
      return data;
    } catch (err) {
      setError(err.message || 'Error rejecting loan');
      throw err;
    } finally {
      setLoading(false);
    }
  }, [currentLoan]);

  /**
   * Uploads document for loan
   */
  const uploadDocument = useCallback(async (loanId, documentType, file) => {
    setLoading(true);
    setError(null);
    
    try {
      const data = await loanService.uploadDocument(loanId, documentType, file);
      return data;
    } catch (err) {
      setError(err.message || 'Error uploading document');
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  return {
    loans,
    currentLoan,
    loading,
    error,
    fetchLoans,
    fetchLoanById,
    submitApplication,
    calculatePaymentSchedule,
    approveLoan,
    rejectLoan,
    uploadDocument
  };
};
