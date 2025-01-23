import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OneAntiquityComponent } from './one-antiquity.component';

describe('OneAntiquityComponent', () => {
  let component: OneAntiquityComponent;
  let fixture: ComponentFixture<OneAntiquityComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [OneAntiquityComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OneAntiquityComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
