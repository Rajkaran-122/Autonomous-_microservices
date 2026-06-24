"use client";

import { usePolling, postAction } from '@/shared/api/baseApi';
import { useState } from 'react';

interface HealingAction {
  id: string;
  actionType: string;
  targetService: string;
  status: string;
  confidenceScore: number;
  createdAt: string;
}

interface PageResponse {
  content: HealingAction[];
}

export default function HealingPage() {
  const { data: pending, isLoading: loadingPending, error: err1 } = usePolling<PageResponse>('/api/v1/healing/pending', 3000);
  const { data: history, isLoading: loadingHistory, error: err2 } = usePolling<HealingAction[]>('/api/v1/healing/history', 5000);
  const [processingId, setProcessingId] = useState<string | null>(null);

  const handleApprove = async (id: string) => {
    setProcessingId(id);
    try {
      await postAction(`/api/v1/healing/${id}/approve`);
      // Optimistically we could remove it from the list, but our 3-second poller will catch the update instantly.
    } catch (error) {
      alert('Failed to approve action: ' + error);
    } finally {
      setProcessingId(null);
    }
  };

  return (
    <div className="animate-entrance">
      <div className="header-section" style={{ marginBottom: '32px' }}>
        <div>
          <h1 className="page-title">Self-Healing & Guardrails<span className="text-gradient"></span></h1>
          <p className="page-subtitle">AI-driven remediation with policy enforcement</p>
        </div>
      </div>

      <div className="glass-panel" style={{ marginBottom: '24px' }}>
        <div className="panel-header">
          <h3>Pending Approvals</h3>
          {pending?.content && pending.content.length > 0 && (
            <span className="badge high">{pending.content.length} Action(s) Required</span>
          )}
        </div>

        {loadingPending && !pending ? (
          <div style={{ color: 'var(--text-muted)' }}>Loading pending actions...</div>
        ) : err1 ? (
          <div style={{ color: 'var(--status-critical)' }}>Connection Error: Docker Backend is not running.</div>
        ) : pending?.content.length === 0 ? (
          <div style={{ padding: '20px', color: 'var(--text-muted)' }}>No actions currently require manual approval.</div>
        ) : (
          pending?.content.map(action => (
            <div key={action.id} style={{ padding: '16px', background: 'rgba(255, 153, 51, 0.05)', border: '1px solid var(--border-subtle)', borderRadius: '8px', marginBottom: '12px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <div>
                  <div style={{ display: 'flex', gap: '8px', alignItems: 'center', marginBottom: '8px' }}>
                    <h4 style={{ margin: 0 }}>{action.actionType}</h4>
                    <span className="badge high" style={{ fontSize: '0.6rem' }}>HIGH RISK</span>
                    <span className="badge info" style={{ fontSize: '0.6rem' }}>CONFIDENCE {action.confidenceScore}%</span>
                  </div>
                  <p style={{ margin: 0, color: 'var(--text-muted)', fontSize: '0.9rem' }}>Target: {action.targetService}</p>
                </div>
                <div style={{ display: 'flex', gap: '8px' }}>
                  <button className="btn btn-secondary">Reject</button>
                  <button
                    className="btn btn-primary"
                    style={{ background: 'var(--status-healthy)' }}
                    onClick={() => handleApprove(action.id)}
                    disabled={processingId === action.id}
                  >
                    {processingId === action.id ? 'Approving...' : 'Approve Action'}
                  </button>
                </div>
              </div>
            </div>
          ))
        )}
      </div>

      <div className="glass-panel">
        <div className="panel-header">
          <h3>Recent Healing Actions</h3>
        </div>

        {loadingHistory && !history ? (
          <div style={{ color: 'var(--text-muted)' }}>Loading history...</div>
        ) : (
          <table style={{ width: '100%', textAlign: 'left', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid var(--border-subtle)', color: 'var(--text-muted)' }}>
                <th style={{ padding: '12px 0' }}>Action</th>
                <th style={{ padding: '12px 0' }}>Target</th>
                <th style={{ padding: '12px 0' }}>Confidence</th>
                <th style={{ padding: '12px 0' }}>Status</th>
                <th style={{ padding: '12px 0' }}>Time</th>
              </tr>
            </thead>
            <tbody>
              {history?.length === 0 && (
                <tr><td colSpan={5} style={{ padding: '20px 0', textAlign: 'center', color: 'var(--text-muted)' }}>No historical actions found.</td></tr>
              )}
              {history?.map((action, idx) => (
                <tr key={action.id} style={{ borderTop: idx > 0 ? '1px solid var(--border-subtle)' : 'none' }}>
                  <td style={{ padding: '16px 0', fontWeight: '500' }}>{action.actionType}</td>
                  <td style={{ padding: '16px 0' }}>{action.targetService}</td>
                  <td style={{ padding: '16px 0' }}>{action.confidenceScore}%</td>
                  <td style={{ padding: '16px 0' }}>
                    <span style={{ color: action.status === 'COMPLETED' ? 'var(--status-healthy)' : action.status === 'FAILED' ? 'var(--status-critical)' : 'var(--text-muted)' }}>
                      {action.status}
                    </span>
                  </td>
                  <td style={{ padding: '16px 0', color: 'var(--text-muted)' }}>{new Date(action.createdAt).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
