import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {

  // BehaviorSubject is used to manage the theme, initialized to 'light'
  private themeSubject = new BehaviorSubject<'dark' | 'light'>(localStorage.getItem('theme') as 'dark' | 'light' || 'light');

  // Observable to allow components to subscribe to theme changes
  theme$ = this.themeSubject.asObservable();

  constructor() {}

  // Method to get the current theme
  get currentTheme(): 'dark' | 'light' {
    return this.themeSubject.value;
  }

  // Method to set the theme
  setTheme(theme: 'dark' | 'light'): void {
    this.themeSubject.next(theme);  // Update the BehaviorSubject value
    localStorage.setItem('theme', theme);  // Store the selected theme in localStorage
    document.documentElement.setAttribute('data-theme', theme);  // Apply the theme by setting a custom attribute on the <html> element
  }

  // Toggle between dark and light themes
  toggleTheme(): void {
    const newTheme = this.currentTheme === 'dark' ? 'light' : 'dark';  // Switch the theme
    this.setTheme(newTheme);  // Apply the new theme
  }
  
}
