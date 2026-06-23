"use client";

import { usePolling } from '@/lib/api';

interface Incident {
  id: string;
  title: string;
  description: string;
  severity: string;
  status: string;
  service: { name: string };
  detectedAt: string;
}

interface PageResponse {
  content: Incident[];
  totalPages: number;
  totalElements: number;
}

export default function IncidentsPage() {
  const { data, isLoading, error } = usePolling<PageResponse>('/api/v1/incidents', 5000);

  return (
    <div className="animate-entrance">
      <div className="header-section" style={{marginBottom: '32px'}}>
        <div>
          <h1 className="page-title">Incident <span className="text-gradient">Response</span></h1>
          <p className="page-subtitle">AI-assisted root cause analysis and management</p>
        </div>
      </div>
      
      <div className="glass-panel">
        <div className="panel-header">
          <h3>Active Incidents</h3>
        </div>
        
        {isLoading && !data ? (
          <div style={{padding: '20px', color: 'var(--text-muted)'}}>Loading live database records...</div>
        ) : error ? (
          <div style={{padding: '20px', color: 'var(--status-critical)'}}>Connection Error: Docker Backend is not running.</div>
        ) : (
          <table style={{width: '100%', textAlign: 'left', borderCollapse: 'collapse'}}>
            <thead>
              <tr style={{borderBottom: '1px solid var(--border-subtle)', color: 'var(--text-muted)'}}>
                <th style={{padding: '12px 0'}}>ID</th>
                <th style={{padding: '12px 0'}}>Severity</th>
                <th style={{padding: '12px 0'}}>Service</th>
                <th style={{padding: '12px 0'}}>Title</th>
                <th style={{padding: '12px 0'}}>Detected At</th>
                <th style={{padding: '12px 0'}}>Status</th>
              </tr>
            </thead>
            <tbody>
              {data?.content.length === 0 && (
                <tr><td colSpan={6} style={{padding: '20px 0', textAlign: 'center', color: 'var(--text-muted)'}}>No incidents found in the database.</td></tr>
              )}
              {data?.content.map((incident, idx) => (
                <tr key={incident.id} style={{borderTop: idx > 0 ? '1px solid var(--border-subtle)' : 'none'}}>
                  <td style={{padding: '16px 0', fontFamily: 'monospace'}}>{incident.id.split('-')[0]}...</td>
                  <td style={{padding: '16px 0'}}>
                    <span className={`badge ${incident.severity === 'P1' ? 'critical' : 'high'}`}>{incident.severity}</span>
                  </td>
                  <td style={{padding: '16px 0', fontWeight: '500'}}>{incident.service?.name || 'Unknown'}</td>
                  <td style={{padding: '16px 0'}}>{incident.title}</td>
                  <td style={{padding: '16px 0', color: 'var(--text-muted)'}}>{new Date(incident.detectedAt).toLocaleString()}</td>
                  <td style={{padding: '16px 0'}}>
                    <span style={{color: incident.status === 'ACTIVE' ? 'var(--status-critical)' : 'var(--status-high)'}}>{incident.status}</span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
