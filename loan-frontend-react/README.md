# Loan Management System - React 16.8 Frontend

## 📖 Overview

Modern React application built with React 16.8 featuring Hooks for the Loan Management System. This frontend provides an interface for loan origination, underwriting, disbursement tracking, and payment management with real-time updates.

## Technology Stack

| Technology                | Version | Purpose                    |
| ------------------------- | ------- | -------------------------- |
| **React**                 | 16.8.6  | UI library with Hooks      |
| **JavaScript ES6+**       | -       | Programming language       |
| **React Router**          | 5.0.1   | Client-side routing        |
| **Redux**                 | 4.0.4   | State management           |
| **React Redux**           | 7.1.0   | React bindings for Redux   |
| **Redux Thunk**           | 2.3.0   | Async action handling      |
| **Axios**                 | 0.19.0  | HTTP client                |
| **Material-UI**           | 4.3.0   | UI component library       |
| **Formik**                | 1.5.8   | Form management            |
| **Yup**                   | 0.27.0  | Form validation            |
| **Chart.js**              | 2.8.0   | Data visualization         |
| **React-Chartjs-2**       | 2.7.6   | React wrapper for Chart.js |
| **date-fns**              | 1.30.1  | Date utilities             |
| **Create React App**      | 3.0.1   | Build tooling              |
| **Jest**                  | 24.8.0  | Testing framework          |
| **React Testing Library** | 8.0.5   | Component testing          |

## Features

### 1. Loan Application

- Multi-step application wizard with progress indicator
- Personal information collection
- Employment and income verification
- Asset and liability declaration
- Document upload (ID, proof of income, bank statements)
- Credit score display and soft inquiry
- Co-applicant information

### 2. Loan Dashboard

- Application status tracking
- Loan portfolio overview
- Payment schedule calendar
- Upcoming payment reminders
- Loan summary cards (active, paid off, in default)
- Quick action buttons

### 3. Underwriting Workflow

- Application review interface
- Credit report integration
- Risk assessment scoring
- Document verification checklist
- Approval/rejection workflow
- Conditional approval with requirements
- Audit trail of decisions

### 4. Payment Management

- Payment history table with filters
- Payment scheduling
- Auto-payment setup
- Payment method management
- Receipt generation and download
- Payment reminders and notifications
- Early payment calculator

### 5. Loan Calculator

- Loan amount and term sliders
- Interest rate calculation
- Monthly payment estimation
- Amortization schedule table
- Total interest calculation
- Comparison tool for different loan options

## Project Structure

```
loan-frontend-react/
├── public/
│   ├── index.html
│   ├── favicon.ico
│   └── manifest.json
├── src/
│   ├── components/
│   │   ├── common/
│   │   │   ├── Button/
│   │   │   ├── Input/
│   │   │   ├── Card/
│   │   │   ├── Modal/
│   │   │   ├── Loading/
│   │   │   └── ErrorBoundary/
│   │   ├── layout/
│   │   │   ├── Header/
│   │   │   ├── Sidebar/
│   │   │   ├── Footer/
│   │   │   └── MainLayout/
│   │   ├── loan/
│   │   │   ├── LoanApplicationForm/
│   │   │   ├── LoanCard/
│   │   │   ├── LoanList/
│   │   │   ├── LoanDetails/
│   │   │   └── LoanCalculator/
│   │   ├── payment/
│   │   │   ├── PaymentForm/
│   │   │   ├── PaymentHistory/
│   │   │   └── PaymentSchedule/
│   │   └── underwriting/
│   │       ├── ApplicationReview/
│   │       ├── CreditReport/
│   │       └── DecisionPanel/
│   ├── pages/
│   │   ├── Dashboard.jsx
│   │   ├── LoanApplication.jsx
│   │   ├── LoanDetails.jsx
│   │   ├── PaymentPage.jsx
│   │   ├── Underwriting.jsx
│   │   ├── Login.jsx
│   │   └── NotFound.jsx
│   ├── redux/
│   │   ├── store.js
│   │   ├── actions/
│   │   │   ├── authActions.js
│   │   │   ├── loanActions.js
│   │   │   └── paymentActions.js
│   │   ├── reducers/
│   │   │   ├── authReducer.js
│   │   │   ├── loanReducer.js
│   │   │   ├── paymentReducer.js
│   │   │   └── index.js
│   │   └── types/
│   │       └── actionTypes.js
│   ├── services/
│   │   ├── api.js
│   │   ├── authService.js
│   │   ├── loanService.js
│   │   └── paymentService.js
│   ├── utils/
│   │   ├── formatters.js
│   │   ├── validators.js
│   │   └── constants.js
│   ├── hooks/
│   │   ├── useAuth.js
│   │   ├── useLoan.js
│   │   └── usePayment.js
│   ├── styles/
│   │   ├── theme.js
│   │   ├── global.css
│   │   └── variables.css
│   ├── App.jsx
│   ├── App.css
│   ├── index.js
│   └── index.css
├── .env.development
├── .env.production
├── package.json
├── README.md
└── .gitignore
```

## Key Components with React Hooks

### Custom Hook: useAuth

```javascript
import { useState, useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { login, logout } from "../redux/actions/authActions";

/**
 * Custom hook for authentication
 */
export const useAuth = () => {
  const dispatch = useDispatch();
  const { user, isAuthenticated, loading, error } = useSelector(
    (state) => state.auth
  );

  const handleLogin = (credentials) => {
    dispatch(login(credentials));
  };

  const handleLogout = () => {
    dispatch(logout());
  };

  return {
    user,
    isAuthenticated,
    loading,
    error,
    login: handleLogin,
    logout: handleLogout,
  };
};
```

### Loan Application Form with Hooks

```javascript
import React, { useState, useEffect } from "react";
import { useFormik } from "formik";
import * as Yup from "yup";
import {
  TextField,
  Button,
  Stepper,
  Step,
  StepLabel,
  Grid,
  Paper,
} from "@material-ui/core";
import { loanService } from "../../services/loanService";

const validationSchema = Yup.object({
  firstName: Yup.string().required("Required"),
  lastName: Yup.string().required("Required"),
  email: Yup.string().email("Invalid email").required("Required"),
  loanAmount: Yup.number().min(1000).max(500000).required("Required"),
  loanPurpose: Yup.string().required("Required"),
});

export const LoanApplicationForm = () => {
  const [activeStep, setActiveStep] = useState(0);
  const [loading, setLoading] = useState(false);

  const steps = [
    "Personal Information",
    "Employment Details",
    "Loan Details",
    "Document Upload",
    "Review & Submit",
  ];

  const formik = useFormik({
    initialValues: {
      firstName: "",
      lastName: "",
      email: "",
      phone: "",
      employerName: "",
      monthlyIncome: "",
      loanAmount: "",
      loanPurpose: "",
      loanTerm: 12,
    },
    validationSchema,
    onSubmit: async (values) => {
      setLoading(true);
      try {
        await loanService.submitApplication(values);
        setActiveStep(steps.length);
      } catch (error) {
        console.error("Error submitting application:", error);
      } finally {
        setLoading(false);
      }
    },
  });

  const handleNext = () => {
    setActiveStep((prev) => prev + 1);
  };

  const handleBack = () => {
    setActiveStep((prev) => prev - 1);
  };

  return (
    <Paper style={{ padding: "24px" }}>
      <Stepper activeStep={activeStep}>
        {steps.map((label) => (
          <Step key={label}>
            <StepLabel>{label}</StepLabel>
          </Step>
        ))}
      </Stepper>

      <form onSubmit={formik.handleSubmit}>
        {activeStep === 0 && (
          <Grid container spacing={3}>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                id="firstName"
                name="firstName"
                label="First Name"
                value={formik.values.firstName}
                onChange={formik.handleChange}
                error={
                  formik.touched.firstName && Boolean(formik.errors.firstName)
                }
                helperText={formik.touched.firstName && formik.errors.firstName}
              />
            </Grid>
            {/* More fields... */}
          </Grid>
        )}

        <div style={{ marginTop: "24px" }}>
          <Button disabled={activeStep === 0} onClick={handleBack}>
            Back
          </Button>
          {activeStep === steps.length - 1 ? (
            <Button
              variant="contained"
              color="primary"
              type="submit"
              disabled={loading}
            >
              Submit Application
            </Button>
          ) : (
            <Button variant="contained" color="primary" onClick={handleNext}>
              Next
            </Button>
          )}
        </div>
      </form>
    </Paper>
  );
};
```

### Dashboard Component with useEffect

```javascript
import React, { useState, useEffect } from "react";
import { Grid, Card, CardContent, Typography } from "@material-ui/core";
import { Line } from "react-chartjs-2";
import { loanService } from "../services/loanService";

export const Dashboard = () => {
  const [stats, setStats] = useState({
    totalLoans: 0,
    activeLoans: 0,
    totalDisbursed: 0,
    pendingApplications: 0,
  });
  const [chartData, setChartData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        const [statsData, chartDataRes] = await Promise.all([
          loanService.getStats(),
          loanService.getChartData(),
        ]);

        setStats(statsData);
        setChartData(chartDataRes);
      } catch (error) {
        console.error("Error fetching dashboard data:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  if (loading) return <div>Loading...</div>;

  return (
    <div>
      <Typography variant="h4" gutterBottom>
        Loan Dashboard
      </Typography>

      <Grid container spacing={3}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Total Loans
              </Typography>
              <Typography variant="h5">{stats.totalLoans}</Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Active Loans
              </Typography>
              <Typography variant="h5">{stats.activeLoans}</Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Loan Trend
              </Typography>
              {chartData && <Line data={chartData} />}
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </div>
  );
};
```

## State Management (Redux)

### Loan Actions

```javascript
import { loanService } from "../../services/loanService";
import {
  FETCH_LOANS_REQUEST,
  FETCH_LOANS_SUCCESS,
  FETCH_LOANS_FAILURE,
  CREATE_LOAN_REQUEST,
  CREATE_LOAN_SUCCESS,
  CREATE_LOAN_FAILURE,
} from "../types/actionTypes";

export const fetchLoans = () => async (dispatch) => {
  dispatch({ type: FETCH_LOANS_REQUEST });

  try {
    const loans = await loanService.getLoans();
    dispatch({ type: FETCH_LOANS_SUCCESS, payload: loans });
  } catch (error) {
    dispatch({ type: FETCH_LOANS_FAILURE, payload: error.message });
  }
};

export const createLoan = (loanData) => async (dispatch) => {
  dispatch({ type: CREATE_LOAN_REQUEST });

  try {
    const loan = await loanService.createLoan(loanData);
    dispatch({ type: CREATE_LOAN_SUCCESS, payload: loan });
    return loan;
  } catch (error) {
    dispatch({ type: CREATE_LOAN_FAILURE, payload: error.message });
    throw error;
  }
};
```

## Build & Deployment

### Development

```bash
# Install dependencies
npm install

# Start development server
npm start

# Runs on http://localhost:3000
```

### Production Build

```bash
# Create optimized production build
npm run build

# Output directory: build/
```

### Docker Deployment

```dockerfile
# Multi-stage build
FROM node:10-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

## Testing

```bash
# Run tests
npm test

# Run tests with coverage
npm test -- --coverage

# Run tests in watch mode
npm test -- --watch
```

## Environment Configuration

### .env.development

```
REACT_APP_API_URL=http://localhost:8080/api
REACT_APP_WS_URL=ws://localhost:8080/ws
REACT_APP_CREDIT_BUREAU_URL=http://localhost:8081/api
```

### .env.production

```
REACT_APP_API_URL=https://api.loanmgmt.company.com/api
REACT_APP_WS_URL=wss://api.loanmgmt.company.com/ws
REACT_APP_CREDIT_BUREAU_URL=https://creditbureau.provider.com/api
```

## Performance Optimizations

- **Code Splitting**: React.lazy() for route-based code splitting
- **Memoization**: React.memo() for expensive components
- **useMemo & useCallback**: Prevent unnecessary re-renders
- **Virtual Scrolling**: For large loan lists
- **Image Optimization**: Lazy loading for images
- **Service Workers**: Offline support with Create React App

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## License

© 2019 Company Name. All rights reserved.
