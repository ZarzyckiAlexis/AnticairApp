import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PaymentConfComponent } from './payment-conf.component';

describe('PaymentConfComponent', () => {
  let component: PaymentConfComponent;
  let fixture: ComponentFixture<PaymentConfComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PaymentConfComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PaymentConfComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
