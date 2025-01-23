import { Component } from '@angular/core';
import { ThemeService } from '../../service/theme.service';

@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrl: './footer.component.css'
})
export class FooterComponent {

  currentTheme: 'dark' | 'light' = 'light'; // Actual theme, by default light
  constructor(private themeService: ThemeService) {}

  ngOnInit(): void {
    // Subscribe to Theme event
    this.themeService.theme$.subscribe(theme => {
      this.currentTheme = theme;
    });

  }

}
