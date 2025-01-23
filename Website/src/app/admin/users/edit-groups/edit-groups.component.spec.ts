import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditGroupsComponent } from './edit-groups.component';

describe('EditGroupsComponent', () => {
  let component: EditGroupsComponent;
  let fixture: ComponentFixture<EditGroupsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [EditGroupsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EditGroupsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
