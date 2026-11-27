import { DatePipe } from '@angular/common';
import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { HistoryEntry } from '../../core/models';
@Component({
  selector: 'app-history-list',
  imports: [DatePipe, RouterLink],
  template: ` @if (entries().length === 0) {
      <div class="empty-state empty-state--compact">
        <strong>Nenhum registro de atendimento</strong>
        <p>As novas alterações e intervenções aparecerão aqui.</p>
      </div>
    }
    <ol class="history-list">
      @for (entry of entries(); track entry.id) {
        <li class="history-item">
          <div class="section-head">
            <strong>{{ eventLabel(entry.eventType) }}</strong
            ><a class="history-link" [routerLink]="['/chamados', entry.requestId]"
              >Chamado #{{ entry.requestId }}</a
            >
          </div>
          <span class="history-meta"
            >{{ entry.createdAt | date: 'dd/MM/yyyy HH:mm:ss' }} · {{ entry.actor }}</span
          >
          <p>{{ entry.description }}</p>
          @if (entry.eventType === 'STATUS') {
            <p class="history-meta">
              {{ statusLabel(entry.previousStatus) }} → {{ statusLabel(entry.currentStatus) }}
            </p>
          }
          @if (entry.eventType === 'ASSIGNMENT') {
            <p class="history-meta">
              {{ entry.previousTechnicianName || 'Não atribuído' }} →
              {{ entry.currentTechnicianName || 'Não atribuído' }}
            </p>
          }
          @if (entry.eventType === 'INTERVENTION') {
            <p class="history-meta">
              Responsável: {{ entry.currentTechnicianName || 'Não atribuído' }}
            </p>
          }
          @if (entry.parts.length > 0) {
            <strong>Peças trocadas</strong>
            <ul>
              @for (part of entry.parts; track part.id) {
                <li>{{ part.quantity }} × {{ part.name }}</li>
              }
            </ul>
          }
        </li>
      }
    </ol>`,
})
export class HistoryListComponent {
  readonly entries = input.required<HistoryEntry[]>();
  eventLabel(type: string): string {
    return (
      (
        {
          CREATED: 'Abertura',
          STATUS: 'Mudança de status',
          ASSIGNMENT: 'Mudança de responsável',
          INTERVENTION: 'Serviço realizado',
        } as Record<string, string>
      )[type] ?? type
    );
  }
  statusLabel(status: string | null): string {
    return (
      (
        { OPEN: 'Aberto', IN_PROGRESS: 'Em andamento', CLOSED: 'Encerrado' } as Record<
          string,
          string
        >
      )[status ?? ''] ?? '—'
    );
  }
}
