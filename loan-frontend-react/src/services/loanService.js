import axios from 'axios';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

/**
 * Loan service for API interactions
 */
class LoanService {
  /**
   * Gets all loans with optional filters
   */
  async getLoans(filters = {}) {
    const params = new URLSearchParams(filters).toString();
    const response = await axios.get(`${API_URL}/loans?${params}`);
    return response.data;
  }

  /**
   * Gets a single loan by ID
   */
  async getLoanById(id) {
    const response = await axios.get(`${API_URL}/loans/${id}`);
    return response.data;
  }

  /**
   * Creates a new loan application
   */
  async submitApplication(applicationData) {
    const response = await axios.post(`${API_URL}/loans/applications`, applicationData);
    return response.data;
  }

  /**
   * Updates loan application
   */
  async updateApplication(id, applicationData) {
    const response = await axios.put(`${API_URL}/loans/applications/${id}`, applicationData);
    return response.data;
  }

  /**
   * Gets loan statistics for dashboard
   */
  async getStats() {
    const response = await axios.get(`${API_URL}/loans/statistics`);
    return response.data;
  }

  /**
   * Gets loan trend data for charts
   */
  async getLoanTrend(period = '12months') {
    const response = await axios.get(`${API_URL}/loans/trend?period=${period}`);
    return response.data;
  }

  /**
   * Gets loans by status
   */
  async getLoansByStatus() {
    const response = await axios.get(`${API_URL}/loans/by-status`);
    return response.data;
  }

  /**
   * Calculates loan payment schedule
   */
  async calculatePaymentSchedule(loanData) {
    const response = await axios.post(`${API_URL}/loans/calculate`, loanData);
    return response.data;
  }

  /**
   * Approves a loan application
   */
  async approveLoan(loanId, approvalData) {
    const response = await axios.post(`${API_URL}/loans/${loanId}/approve`, approvalData);
    return response.data;
  }

  /**
   * Rejects a loan application
   */
  async rejectLoan(loanId, rejectionReason) {
    const response = await axios.post(`${API_URL}/loans/${loanId}/reject`, { reason: rejectionReason });
    return response.data;
  }

  /**
   * Gets credit score for applicant
   */
  async getCreditScore(applicantId) {
    const response = await axios.get(`${API_URL}/credit-bureau/score/${applicantId}`);
    return response.data;
  }

  /**
   * Uploads document for loan application
   */
  async uploadDocument(loanId, documentType, file) {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('documentType', documentType);

    const response = await axios.post(
      `${API_URL}/loans/${loanId}/documents`,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      }
    );
    return response.data;
  }

  /**
   * Gets documents for a loan
   */
  async getDocuments(loanId) {
    const response = await axios.get(`${API_URL}/loans/${loanId}/documents`);
    return response.data;
  }

  /**
   * Exports loans to Excel
   */
  async exportToExcel(filters = {}) {
    const params = new URLSearchParams(filters).toString();
    const response = await axios.get(`${API_URL}/loans/export/excel?${params}`, {
      responseType: 'blob'
    });
    return response.data;
  }
}

export const loanService = new LoanService();
