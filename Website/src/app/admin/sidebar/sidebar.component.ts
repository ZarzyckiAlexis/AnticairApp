import { Component } from '@angular/core';
import { ThemeService } from '../../../service/theme.service';
import { Router } from '@angular/router';
import { AuthService } from '../../../service/auth.service';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent {

  currentTheme: 'dark' | 'light' = 'light'; // Actual theme, by default light
  isMobileMenuOpen: boolean | undefined;


  constructor(private authService : AuthService, private themeService : ThemeService, private router: Router) { }

  ngOnInit(){
    // Subscribe to Theme event
    this.themeService.theme$.subscribe(theme => {
      this.currentTheme = theme;
    });
  }

  // Toggle between themes trough the service
  toggleTheme(): void {
    this.themeService.toggleTheme();
  }


  logout(){
    // Redirect to home page
    this.router.navigate(['/home']);
    // We wait 300ms before logging out
    setTimeout(() => {
      this.authService.logout();
    }, 300);
  }

  toggleMobileMenu() {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
    
    // Prevent scrolling on body when mobile menu is open
    if (this.isMobileMenuOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = 'auto';
    }
  }

  closeMobileMenu() {
    this.isMobileMenuOpen = false;
    document.body.style.overflow = 'auto';
  }


}
