import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CheckListingComponent } from './check-listing.component';

describe('CheckListingComponent', () => {
  let component: CheckListingComponent;
  let fixture: ComponentFixture<CheckListingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CheckListingComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CheckListingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
