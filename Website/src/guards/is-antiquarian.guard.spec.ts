import { TestBed } from '@angular/core/testing';
import { CanActivateFn } from '@angular/router';

import { isAntiquarianGuard } from './is-antiquarian.guard';

describe('isAntiquarianGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) => 
      TestBed.runInInjectionContext(() => isAntiquarianGuard(...guardParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });
});
