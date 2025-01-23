import { TestBed } from '@angular/core/testing';
import { CanActivateFn } from '@angular/router';

import { editAntiquityGuard } from './edit-antiquity.guard';

describe('editAntiquityGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) => 
      TestBed.runInInjectionContext(() => editAntiquityGuard(...guardParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });
});
