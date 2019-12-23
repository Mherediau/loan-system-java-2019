import React, { useState, useEffect } from 'react';
import {
  Card,
  CardContent,
  Typography,
  Grid,
  CircularProgress,
  Box
} from '@material-ui/core';
import { Line, Doughnut } from 'react-chartjs-2';
import { makeStyles } from '@material-ui/core/styles';
import {
  TrendingUp,
  AttachMoney,
  Assessment,
  People
} from '@material-ui/icons';
import { loanService } from '../services/loanService';

const useStyles = makeStyles((theme) => ({
  root: {
    flexGrow: 1,
    padding: theme.spacing(3)
  },
  card: {
    height: '100%',
    display: 'flex',
    flexDirection: 'column'
  },
  cardContent: {
    flexGrow: 1
  },
  statCard: {
    position: 'relative',
    overflow: 'visible'
  },
  icon: {
    position: 'absolute',
    top: -20,
    right: 20,
    backgroundColor: theme.palette.primary.main,
    color: 'white',
    padding: theme.spacing(2),
    borderRadius: '50%'
  }
}));

/**
 * Dashboard component showing loan statistics and analytics
 * Uses React Hooks for state management and side effects
 */
export const Dashboard = () => {
  const classes = useStyles();
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    totalLoans: 0,
    activeLoans: 0,
    totalDisbursed: 0,
    pendingApplications: 0,
    averageInterestRate: 0,
    defaultRate: 0
  });
  const [trendData, setTrendData] = useState(null);
  const [statusData, setStatusData] = useState(null);

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        const [statsData, trend, status] = await Promise.all([
          loanService.getStats(),
          loanService.getLoanTrend(),
          loanService.getLoansByStatus()
        ]);

        setStats(statsData);

        // Prepare trend chart data
        setTrendData({
          labels: trend.map(item => item.month),
          datasets: [
            {
              label: 'Loans Disbursed',
              data: trend.map(item => item.amount),
              borderColor: 'rgb(75, 192, 192)',
              backgroundColor: 'rgba(75, 192, 192, 0.2)',
              tension: 0.1
            }
          ]
        });

        // Prepare status doughnut chart
        setStatusData({
          labels: ['Active', 'Paid Off', 'Default', 'Pending'],
          datasets: [
            {
              data: [
                status.active,
                status.paidOff,
                status.default,
                status.pending
              ],
              backgroundColor: [
                'rgba(75, 192, 192, 0.8)',
                'rgba(54, 162, 235, 0.8)',
                'rgba(255, 99, 132, 0.8)',
                'rgba(255, 206, 86, 0.8)'
              ]
            }
          ]
        });
      } catch (error) {
        console.error('Error fetching dashboard data:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <div className={classes.root}>
      <Typography variant="h4" gutterBottom>
        Loan Management Dashboard
      </Typography>

      <Grid container spacing={3}>
        {/* Statistics Cards */}
        <Grid item xs={12} sm={6} md={3}>
          <Card className={`${classes.card} ${classes.statCard}`}>
            <TrendingUp className={classes.icon} fontSize="large" />
            <CardContent className={classes.cardContent}>
              <Typography color="textSecondary" gutterBottom>
                Total Loans
              </Typography>
              <Typography variant="h4" component="h2">
                {stats.totalLoans.toLocaleString()}
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card className={`${classes.card} ${classes.statCard}`}>
            <Assessment className={classes.icon} fontSize="large" />
            <CardContent className={classes.cardContent}>
              <Typography color="textSecondary" gutterBottom>
                Active Loans
              </Typography>
              <Typography variant="h4" component="h2">
                {stats.activeLoans.toLocaleString()}
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card className={`${classes.card} ${classes.statCard}`}>
            <AttachMoney className={classes.icon} fontSize="large" />
            <CardContent className={classes.cardContent}>
              <Typography color="textSecondary" gutterBottom>
                Total Disbursed
              </Typography>
              <Typography variant="h4" component="h2">
                ${(stats.totalDisbursed / 1000000).toFixed(2)}M
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card className={`${classes.card} ${classes.statCard}`}>
            <People className={classes.icon} fontSize="large" />
            <CardContent className={classes.cardContent}>
              <Typography color="textSecondary" gutterBottom>
                Pending Applications
              </Typography>
              <Typography variant="h4" component="h2">
                {stats.pendingApplications}
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        {/* Loan Trend Chart */}
        <Grid item xs={12} md={8}>
          <Card className={classes.card}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Loan Disbursement Trend
              </Typography>
              {trendData && <Line data={trendData} options={{ maintainAspectRatio: true }} />}
            </CardContent>
          </Card>
        </Grid>

        {/* Loan Status Distribution */}
        <Grid item xs={12} md={4}>
          <Card className={classes.card}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Loan Status Distribution
              </Typography>
              {statusData && (
                <Box display="flex" justifyContent="center" mt={2}>
                  <Doughnut data={statusData} options={{ maintainAspectRatio: true }} />
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Key Metrics */}
        <Grid item xs={12} md={6}>
          <Card className={classes.card}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Key Metrics
              </Typography>
              <Box mt={2}>
                <Typography variant="body1">
                  Average Interest Rate: <strong>{stats.averageInterestRate}%</strong>
                </Typography>
                <Typography variant="body1" style={{ marginTop: 8 }}>
                  Default Rate: <strong>{stats.defaultRate}%</strong>
                </Typography>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </div>
  );
};
