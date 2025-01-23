import { ChangeDetectorRef, Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ThemeService } from '../../../../service/theme.service';
import { GroupService } from '../../../../service/group.service';
import { AuthService } from '../../../../service/auth.service';

@Component({
  selector: 'app-edit-groups',
  templateUrl: './edit-groups.component.html',
  styleUrl: './edit-groups.component.css'
})
export class EditGroupsComponent {
  userEmail: string = ''; // Email of the user
  groups: { name: string; selected: boolean }[] = [];
  currentTheme: 'dark' | 'light' = 'light'; // Actual theme, by default light

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private groupsService: GroupService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
    private themeService : ThemeService
  ) {}

  ngOnInit() {
    this.route.paramMap.subscribe((params) => {
      this.userEmail = params.get('email') || ''; // We fetch the email from the URL
    });
    this.getGroups(); // Fetch the groups from the Keycloak realm
    this.themeService.theme$.subscribe(theme => {
      this.currentTheme = theme;
    });
  }

  saveGroups() {
    const selectedGroups = this.groups
      .filter((group) => group.selected)
      .map((group) => group.name);
    const unselectedGroups = this.groups
      .filter((group) => !group.selected)
      .map((group) => group.name);
  
    const addRequests = selectedGroups.map((groupName) => {
      return this.groupsService.addGroupToUser(this.userEmail, groupName).toPromise();
    });
  
    const removeRequests = unselectedGroups.map((groupName) => {
      return this.groupsService.removeGroupFromUser(this.userEmail, groupName).toPromise();
    });
  
    const requests = [...addRequests, ...removeRequests];
  
    // Wait for all requests to complete
    Promise.all(requests)
      .then(() => {
        alert('Groups updated successfully!');
        // Manually update the local groups state
        this.groups = this.groups.map((group) => {
          if (selectedGroups.includes(group.name)) {
            group.selected = true;
          } else if (unselectedGroups.includes(group.name)) {
            group.selected = false;
          }
          return group;
        });
  
        this.router.navigate(['/admin/users']); // Redirect to the users page
      })
      .catch((error) => {
        console.error('Error updating groups:', error);
        alert('An error occurred while updating groups.');
      });
  }

  getGroups() {
    // First, fetch all available groups from Keycloak
    this.groupsService.getGroups().subscribe(
      (groupsResponse: any) => {
        // Fetch the groups of the specific user
        this.groupsService.getGroupsFromUser(this.userEmail).subscribe(
          (userGroupsResponse: any) => {
            // If the user has no groups, userGroupsResponse will be empty
            const userGroupNames = userGroupsResponse.length > 0 ? userGroupsResponse.map((group: any) => group.name) : [];
  
            // Map over all available groups and check if the user is part of each
            this.groups = groupsResponse.map((group: any) => ({
              ...group,
              selected: userGroupNames.includes(group.name)  // Set 'selected' to true if the user is part of the group
            }));
            this.cdr.markForCheck(); // Trigger change detection
          },
          (error) => {
            console.error('Error fetching user groups', error);
          }
        );
      },
      (error) => {
        console.error('Error fetching all groups', error);
      }
    );
  }
  
  
}
