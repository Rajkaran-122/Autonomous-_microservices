import { usePolling, postAction } from '@/shared/api/baseApi';

export interface Incident {
  id: string;
  title: string;
  description: string;
  severity: string;
  status: string;
  serviceId: string;
  detectedAt: string;
}

export function useIncidents() {
  return usePolling<Incident[]>('http://localhost:8080/api/v1/incidents', 5000);
}

export async function resolveIncident(id: string) {
  return postAction(`http://localhost:8080/api/v1/incidents/${id}/resolve`);
}
