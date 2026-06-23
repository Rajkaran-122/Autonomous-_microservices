"use client";

import { usePolling } from '@/lib/api';

interface SloViolation {
  id: string;
  service: { name: string };
  violationType: string;
  burnRate: number;
  errorBudgetRemaining: number;
  severity: string;
  startedAt: string;
}

interface PageResponse {
  content: SloViolation[];
}

export default function SLOPage() {
  const { data, isLoading, error } = usePolling<PageResponse>('/api/v1/slo', 5000);

  return (
    <div className="animate-entrance">
      <div className="header-section" style={{marginBottom: '32px'}}>
        <div>
          <h1 className="page-title">SLOs & <span className="text-gradient">Error Budgets</span></h1>
          <p className="page-subtitle">Google SRE multi-window burn rate tracking</p>
        </div>
      </div>
      
      {isLoading && !data ? (
        <div style={{color: 'var(--text-muted)'}}>Loading live SLO tracking data...</div>
      ) : error ? (
        <div style={{color: 'var(--status-critical)'}}>Connection Error: Docker Backend is not running.</div>
      ) : (
        <div className="dashboard-grid">
          {data?.content.length === 0 && (
            <div className="glass-panel" style={{gridColumn: '1 / -1', textAlign: 'center', padding: '40px'}}>
               <p style={{color: 'var(--text-muted)'}}>No active SLO violations found.</p>
            </div>
          )}
          {data?.content.map(slo => (
            <div key={slo.id} className="glass-panel">
              <div style={{display: 'flex', justifyContent: 'space-between', marginBottom: '16px'}}>
                <h4>{slo.service?.name || 'Unknown'}</h4>
                <span className={`badge ${slo.severity === 'P1' ? 'critical' : 'high'}`}>{slo.violationType}</span>
              </div>
              <div style={{fontSize: '2rem', fontWeight: 'bold', marginBottom: '8px', color: slo.burnRate > 5 ? 'var(--status-critical)' : 'var(--status-high)'}}>
                {slo.burnRate}x <span style={{fontSize: '1rem', fontWeight: 'normal', color: 'var(--text-muted)'}}>burn rate</span>
              </div>
              <p style={{color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '16px'}}>Error Budget Remaining: {slo.errorBudgetRemaining}%</p>
              <div className="progress-track">
                <div className="progress-fill" style={{width: `${Math.max(0, slo.errorBudgetRemaining)}%`, background: slo.errorBudgetRemaining < 20 ? 'var(--status-critical)' : 'var(--status-healthy)'}}></div>
              </div>
              <p style={{fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '8px'}}>Started: {new Date(slo.startedAt).toLocaleString()}</p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
