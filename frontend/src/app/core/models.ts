export type Urgency = 'LOW' | 'MEDIUM' | 'HIGH';
export type MaintenanceType = 'PREVENTIVE' | 'CORRECTIVE';
export type MaintenanceRequestStatus = 'OPEN' | 'IN_PROGRESS' | 'CLOSED';

export interface CreatedResource {
  createdAt: string;
}

export interface HealthStatus {
  applicationName: string;
  status: string;
  timestamp: string;
  stack: string[];
}

export interface Sector extends CreatedResource {
  id: number;
  name: string;
  description: string | null;
}

export interface SectorPayload {
  name: string;
  description?: string;
}

export interface Equipment extends CreatedResource {
  id: number;
  assetTag: string;
  name: string;
  description: string | null;
  sectorId: number;
  sectorName: string;
}

export interface EquipmentPayload {
  assetTag: string;
  name: string;
  description?: string;
  sectorId: number;
}

export interface Technician extends CreatedResource {
  id: number;
  name: string;
  email: string;
  specialty: string | null;
  highUrgencyOpenRequests: number;
}

export interface TechnicianPayload {
  name: string;
  email: string;
  specialty?: string;
}

export interface MaintenanceRequest extends CreatedResource {
  id: number;
  title: string;
  description: string;
  equipmentId: number;
  equipmentName: string;
  sectorId: number;
  sectorName: string;
  type: MaintenanceType;
  urgency: Urgency;
  status: MaintenanceRequestStatus;
  technicianId: number | null;
  technicianName: string | null;
  updatedAt: string;
}

export interface MaintenanceRequestPayload {
  title: string;
  description: string;
  equipmentId: number;
  sectorId: number;
  type: MaintenanceType;
  urgency: Urgency;
}

export interface MaintenanceRequestFilters {
  status?: MaintenanceRequestStatus;
  urgency?: Urgency;
}

export interface AssignmentPayload {
  technicianId: number;
}

export interface MaintenanceStatusPayload {
  status: MaintenanceRequestStatus;
}

export interface ApiErrorResponse {
  timestamp?: string;
  status?: number;
  error?: string;
  message?: string;
  code?: string;
  path?: string;
  fieldErrors?: Record<string, string>;
}
