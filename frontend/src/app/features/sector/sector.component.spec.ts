import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { Sector } from '../../core/models';
import { SectorService } from '../../core/services/sector.service';
import { SectorComponent } from './sector.component';

describe('SectorComponent', () => {
  const createdSector: Sector = {
    id: 1,
    name: 'Usinagem',
    description: 'Linha principal',
    createdAt: '2026-08-28T10:00:00',
  };

  const sectorService = {
    list: vi.fn(() => of([])),
    create: vi.fn(() => of(createdSector)),
  };

  beforeEach(async () => {
    vi.clearAllMocks();
    sectorService.list.mockReturnValue(of([]));
    sectorService.create.mockReturnValue(of(createdSector));

    await TestBed.configureTestingModule({
      imports: [SectorComponent],
      providers: [{ provide: SectorService, useValue: sectorService }],
    }).compileComponents();
  });

  it('does not submit an invalid form', () => {
    const fixture = TestBed.createComponent(SectorComponent);
    const component = fixture.componentInstance as any;
    fixture.detectChanges();

    component.submit();

    expect(sectorService.create).not.toHaveBeenCalled();
    expect(component.form.controls.name.touched).toBe(true);
  });

  it('normalizes optional text and adds the created sector to the list', () => {
    const fixture = TestBed.createComponent(SectorComponent);
    const component = fixture.componentInstance as any;
    fixture.detectChanges();
    component.form.setValue({ name: '  Usinagem  ', description: '  Linha principal  ' });

    component.submit();

    expect(sectorService.create).toHaveBeenCalledWith({
      name: 'Usinagem',
      description: 'Linha principal',
    });
    expect(component.sectors()).toEqual([createdSector]);
  });
});
