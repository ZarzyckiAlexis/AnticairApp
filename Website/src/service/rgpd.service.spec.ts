import { TestBed } from '@angular/core/testing';

import { RgpdService } from './rgpd.service';

describe('RgpdService', () => {
  let service: RgpdService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(RgpdService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
