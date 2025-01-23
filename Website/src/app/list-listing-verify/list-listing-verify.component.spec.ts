import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListListingVerifyComponent } from './list-listing-verify.component';

describe('ListListingVerifyComponent', () => {
  let component: ListListingVerifyComponent;
  let fixture: ComponentFixture<ListListingVerifyComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ListListingVerifyComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ListListingVerifyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
