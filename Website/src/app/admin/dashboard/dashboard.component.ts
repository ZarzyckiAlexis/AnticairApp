import { Component } from '@angular/core';
import { ThemeService } from '../../../service/theme.service';
import {UserService} from '../../../service/user.service';
import { AuthService } from '../../../service/auth.service';
import { ListingService } from '../../../service/listing.service';
import { Antiquity } from '../../../modele/DtoListing';
import { ImageServiceService } from '../../../service/image-service.service';

interface Activity {
  message: string;
  inferredTime?: string;
  type?: 'sale' | 'listing' | 'other';
}

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent {

  currentTheme: 'dark' | 'light' = 'light'; // Actual theme, by default light
  isSidebarOpen: boolean = false;
  isUserMenuOpen: boolean = false;
  isLoading: boolean = true;
  numberOfUsers: number = 0;
  error: string | null = null;
  notification: { message: string, type: 'success' | 'error' } | null = null;
  recentActivities: { inferredTime: string; message: string; type: string }[] = [];
  isLoadingActivities: boolean = true;
  activitiesError: string | null = null;
  constructor(private themeService: ThemeService, private userService: UserService,private authService: AuthService,private listingService: ListingService
  ) {}

  ngOnInit(): void {
    // Subscribe to Theme event
    this.themeService.theme$.subscribe(theme => {
      this.currentTheme = theme;
    });
    this.fetchUserCount();
    this.fetchRecentActivities();


  }

  toggleUserMenu() {
    this.isUserMenuOpen = !this.isUserMenuOpen;
  }

  dismissNotification() {
    this.notification = null;
  }

  async fetchUserCount() {
    try {
      const token = await this.authService.getToken();
      this.numberOfUsers = await this.userService.numberOfUsers(token);
      this.isLoading = false;
    } catch (err) {
      this.error = 'Failed to load user count';
      this.isLoading = false;
      console.error('Error fetching user count:', err);
    }
  }

  fetchRecentActivities(): void {
    this.isLoadingActivities = true;
    this.activitiesError = null;

    this.listingService.getAllAntiquitiesChecked().subscribe({
      next: (antiquities: Antiquity[]) => {
        // Sort antiquities by id descending (assuming higher IDs are more recent)
        const sortedAntiquities = antiquities.sort((a, b) => b.idAntiquity - a.idAntiquity);

        // Take the latest 5 activities (adjust as needed)
        const latestAntiquities = sortedAntiquities.slice(0, 5);

        // Map antiquities to activities
        this.recentActivities = latestAntiquities.map(antiquity => ({
          message: `User ${antiquity.mailSeller} listed "${antiquity.titleAntiquity}".`,
          inferredTime: 'Recently', // Placeholder as exact timestamp is unavailable
          type: 'listing' // Assuming this is a listing activity
        }));

        this.isLoadingActivities = false;
      },
      error: (err) => {
        console.error('Error fetching antiquities:', err);
        this.activitiesError = 'Failed to load recent activities.';
        this.isLoadingActivities = false;
      }
    });
  }

}
