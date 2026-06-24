import { postAction } from '@/shared/api/baseApi';

export interface HealingActionRequest {
  incidentId: string;
  actionType: string; // e.g., 'POD_RESTART', 'SCALE_UP'
  targetService: string;
}

export async function executeHealingAction(request: HealingActionRequest) {
  return postAction('http://localhost:8080/api/v1/healing/execute', request);
}
