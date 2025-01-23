import { Component, Input } from '@angular/core';
import { Antiquity } from '../../modele/DtoListing';
import { ThemeService } from '../../service/theme.service';
import { ActivatedRoute, Router } from '@angular/router';
import { ListingService } from '../../service/listing.service';
import { AuthService } from '../../service/auth.service';

@Component({
  selector: 'app-buy-listing',
  templateUrl: './buy-listing.component.html',
  styleUrl: './buy-listing.component.css'
})
export class BuyListingComponent {

    currentTheme: 'dark' | 'light' = 'light'; // Actual theme, by default light
    @Input() id!: number; // Input property to accept the id
    antiquity!: Antiquity; // Antiquity object
    errorMessage: string = ''; // Error message
  
    constructor(private themeService: ThemeService, private route : ActivatedRoute, private router : Router, private listingService : ListingService, private authService: AuthService) {}
  
    ngOnInit(): void {
      // Subscribe to Theme event
      this.themeService.theme$.subscribe(theme => {
        this.currentTheme = theme;
      });
  
       // Get the ID from the route and fetch the antiquity details
      this.route.paramMap.subscribe(params => {
        const id = +params.get('id')!;
        if (id) {
          this.listingService.getAntiquityById(id.toString()).subscribe(antiquity => {
            if (antiquity.state !== 1) {
              this.router.navigate(['/sell']);
            } else {
              this.antiquity = antiquity;
            }
          });
        } else {
          this.router.navigate(['/sell']);
        }
      });
    }

  buyAntiquity(): void {
    // Check if the user is connected
    if (!this.authService.isLoggedIn().value) {
      // Show a message to the user
      this.errorMessage = 'You must be logged in to buy an antiquity';
      return;
    }
    // Call the service to buy the antiquity
    this.listingService.buyAntiquity(this.antiquity.idAntiquity).subscribe(response => {
      // Open the paypal link in the current tab
      window.open(response, '_self');
    });
  }

  goBack(): void {
    this.router.navigate(['/sell']);
  }

}
