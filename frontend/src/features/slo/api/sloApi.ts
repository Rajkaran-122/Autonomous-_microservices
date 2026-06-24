import { usePolling } from '@/shared/api/baseApi';

export interface SLOData {
  serviceName: string;
  sloTarget: number;
  currentSli: number;
  burnRate: number;
  alertLevel: string;
}

export function useSLOs() {
  return usePolling<SLOData[]>('http://localhost:8080/api/v1/slo', 10000);
}
