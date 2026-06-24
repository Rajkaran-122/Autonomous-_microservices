"use client";

import { usePolling } from '@/shared/api/baseApi';

interface DashboardStats {
  activeIncidents: number;
  criticalIncidents: number;
  servicesMonitored: number;
  unhealthyServices: number;
  sloViolations: number;
  healingActions: number;
  healingSuccessRate: number;
  aiAnalyses: number;
}

export default function DashboardOverview() {
  const { data: stats, isLoading, error } = usePolling<DashboardStats>('/api/v1/dashboard/stats', 5000);

  if (isLoading && !stats) {
    return (
      <div className="dashboard-container">
        <div className="skeleton" style={{ height: '100px', width: '100%' }}></div>
        <div className="dashboard-grid stats-grid">
          {[1, 2, 3, 4].map(i => <div key={i} className="skeleton" style={{ height: '150px' }}></div>)}
        </div>
      </div>
    );
  }

  if (error) {
    return <div style={{ color: 'var(--status-critical)' }}>Failed to load dashboard statistics. Ensure backend is running.</div>;
  }

  // Fallback to zeros if stats is somehow null after loading
  const currentStats = stats || {
    activeIncidents: 0, criticalIncidents: 0, servicesMonitored: 0,
    unhealthyServices: 0, sloViolations: 0, healingActions: 0,
    healingSuccessRate: 100, aiAnalyses: 0
  };

  return (
    <div className="dashboard-container animate-entrance">
      <div className="header-section">
        <div>
          <h1 className="page-title">Platform  Overview <span className="text-gradient"></span></h1>
          <p className="page-subtitle">Real-time SRE telemetry and autonomous operations</p>
        </div>
        <div className="header-actions">
          <button className="btn btn-secondary" onClick={() => alert('Export Report functionality coming soon.')}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path><polyline points="17 8 12 3 7 8"></polyline><line x1="12" y1="3" x2="12" y2="15"></line></svg>
            Export Report
          </button>
          <button className="btn btn-primary" onClick={() => alert('New Incident creation form coming soon.')}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
            New Incident
          </button>
        </div>
      </div>

      <div className="dashboard-grid stats-grid">
        <div className="glass-panel stat-card critical">
          <div className="stat-header">
            <span className="stat-title">Active Incidents</span>
            {currentStats.criticalIncidents > 0 && <span className="badge critical animate-pulse">P1 Active</span>}
          </div>
          <div className="stat-value">{currentStats.activeIncidents}</div>
          <div className="stat-footer">
            <span className="trend down">↓ 14%</span> vs last week
          </div>
        </div>

        <div className="glass-panel stat-card warning">
          <div className="stat-header">
            <span className="stat-title">SLO Violations</span>
            {currentStats.sloViolations > 0 && <span className="badge high">Warning</span>}
          </div>
          <div className="stat-value">{currentStats.sloViolations}</div>
          <div className="stat-footer">
            <span>Tracking active multi-window burn rates</span>
          </div>
        </div>

        <div className="glass-panel stat-card ai">
          <div className="stat-header">
            <span className="stat-title">AI Auto-Remediations</span>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="var(--primary)" strokeWidth="2"><path d="M12 2v20M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path></svg>
          </div>
          <div className="stat-value">{currentStats.healingActions}</div>
          <div className="stat-footer">
            <span className="trend up">↑ {currentStats.healingSuccessRate}% Success</span>
          </div>
        </div>

        <div className="glass-panel stat-card">
          <div className="stat-header">
            <span className="stat-title">Monitored Services</span>
          </div>
          <div className="stat-value">{currentStats.servicesMonitored}</div>
          <div className="stat-footer">
            <span className="status-dot"></span> {currentStats.servicesMonitored - currentStats.unhealthyServices} Healthy, {currentStats.unhealthyServices} Degraded
          </div>
        </div>
      </div>

      <div className="content-grid">
        <div className="glass-panel main-chart">
          <div className="panel-header">
            <h3>System Health & AI Confidence</h3>
            <div className="panel-actions">
              <select className="dark-select"><option>Last 24 Hours</option></select>
            </div>
          </div>
          <div className="chart-placeholder">
            {/* Real Chart UI would be here using Recharts or similar. Using dynamic generated bars for now. */}
            <div className="mock-chart">
              {[40, 60, 45, 80, 50, 90, 85, 95, 30, 60, 75, 90].map((h, i) => (
                <div key={i} className="chart-bar" style={{ height: `${h}%` }}>
                  <div className="chart-bar-fill" style={{ background: h < 50 ? 'var(--status-critical)' : h > 80 ? 'var(--primary)' : 'var(--secondary)' }}></div>
                </div>
              ))}
            </div>
            <div className="chart-labels">
              <span>00:00</span><span>06:00</span><span>12:00</span><span>18:00</span><span>Now</span>
            </div>
          </div>
        </div>

        <div className="glass-panel recent-activity">
          <div className="panel-header">
            <h3>Autonomous Actions</h3>
            <a href="/healing" className="view-all">View All</a>
          </div>
          <div className="activity-list">
            {/* Fetching the real activity list would go here. Fallback placeholder below. */}
            <div className="activity-item">
              <div className="activity-icon bg-primary">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"></polyline></svg>
              </div>
              <div className="activity-content">
                <p className="activity-title">View Healing Tab for live data</p>
                <p className="activity-desc">Click 'View All' to see live actions</p>
              </div>
              <div className="activity-time">Live</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
