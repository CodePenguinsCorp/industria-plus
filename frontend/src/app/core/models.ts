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

/** Técnico é um usuário com perfil TECHNICIAN, vinculado a um gestor. */
export interface Technician extends CreatedResource {
  id: number;
  name: string;
  email: string;
  specialty: string | null;
  managerId: number | null;
  managerName: string | null;
  highUrgencyOpenRequests: number;
}

export interface ManagerOption {
  id: number;
  name: string;
  email: string;
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

export type UserRole = 'ADMIN' | 'MANAGER' | 'TECHNICIAN';
export interface AppUser {
  id: number;
  name: string;
  email: string;
  role: UserRole;
  specialty: string | null;
  managerId: number | null;
  managerName: string | null;
  mustChangePassword: boolean;
  createdAt?: string;
}
export interface UserPayload {
  name: string;
  email: string;
  password: string;
  role: UserRole;
  managerId: number | null;
  specialty: string | null;
}
export interface ReplacedPart {
  id?: number;
  name: string;
  quantity: number;
}
export interface HistoryEntry {
  id: number;
  requestId: number;
  eventType: 'CREATED' | 'STATUS' | 'ASSIGNMENT' | 'INTERVENTION';
  actor: string;
  description: string;
  previousStatus: string | null;
  currentStatus: string;
  previousTechnicianId: number | null;
  previousTechnicianName: string | null;
  currentTechnicianId: number | null;
  currentTechnicianName: string | null;
  createdAt: string;
  parts: ReplacedPart[];
}
export interface EquipmentDetail {
  equipment: Equipment;
  requests: MaintenanceRequest[];
  history: HistoryEntry[];
}
