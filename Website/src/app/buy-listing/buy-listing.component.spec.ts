import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BuyListingComponent } from './buy-listing.component';

describe('BuyListingComponent', () => {
  let component: BuyListingComponent;
  let fixture: ComponentFixture<BuyListingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [BuyListingComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BuyListingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
