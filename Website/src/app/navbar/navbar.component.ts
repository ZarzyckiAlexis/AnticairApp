import { Component, OnInit } from '@angular/core';
import { ThemeService } from '../../service/theme.service';
import { Subscription } from 'rxjs';
import { AuthService } from '../../service/auth.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent implements OnInit {

  currentTheme: 'dark' | 'light' = 'light'; // Actual theme, by default light
  isMenuOpen = false; // Flag used for the navbar on the mobile phone
  isUserLoggedIn: boolean = false; // Track the login status
  private loginStatusSubscription: Subscription | undefined;

  constructor(private themeService: ThemeService, private authService: AuthService) {}

  ngOnInit(): void {
    // Subscribe to Theme event
    this.themeService.theme$.subscribe(theme => {
      this.currentTheme = theme;
    });

     // Subscribe to login status
    this.loginStatusSubscription = this.authService.isLoggedIn().subscribe(loggedIn => {
      this.isUserLoggedIn = loggedIn;
    });
  }
  isUserAntiquarian(): boolean {
    if(this.isUserLoggedIn) {
      const userInfo = this.authService.getUserDetails();
      if(userInfo['groups'].includes('Antiquarian')) {
        return true;
      }else {
        return false;
      }
    }
    return false;
  }
  isUserAdmin(): boolean {
    if(this.isUserLoggedIn) {
      return this.authService.isAdmin();
    } else{
      return false;
    }
  }

  ngOnDestroy(): void {
    // Unsubscribe to avoid memory leaks
    if (this.loginStatusSubscription) {
      this.loginStatusSubscription.unsubscribe();
    }
  }

  // Toggle between themes trough the service
  toggleTheme(): void {
    this.themeService.toggleTheme();
  }

  // Method to login from the navbar
  login() {
    // Calling keycloak method
    this.authService.login();
  }

  // Method to logout from the navbar
  logout() {
    // Calling keycloak method
    this.authService.logout();
  }

}
