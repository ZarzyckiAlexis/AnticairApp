import { Component, Input, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin, mergeMap } from 'rxjs';
import { Antiquity } from '../../modele/DtoListing';
import { ThemeService } from '../../service/theme.service';
import { ListingService } from '../../service/listing.service';
import { ImageServiceService } from '../../service/image-service.service';

@Component({
  selector: 'app-one-antiquity',
  templateUrl: './one-antiquity.component.html',
  styleUrls: ['./one-antiquity.component.css']
})
export class OneAntiquityComponent implements OnInit {

  currentTheme: 'dark' | 'light' = 'light'; // Actual theme, by default light
  @Input() id!: number; // Input property to accept the id
  antiquity!: Antiquity; // Antiquity object
  imageUrls: String[] = []; // Array to hold image URLs
  currentImageIndex: number = 0; // Index of the current image displayed

  constructor(
    private themeService: ThemeService,
    private route: ActivatedRoute,
    private router: Router,
    private listingService: ListingService,
    private imageService: ImageServiceService
  ) {}

  ngOnInit(): void {
    // Subscribe to Theme event
    this.themeService.theme$.subscribe(theme => {
      this.currentTheme = theme;
    });

    // Get the ID from the route and fetch the antiquity details
    this.listingService.getAntiquityById(this.id.toString()).subscribe(antiquity => {
      if (!antiquity) {
        this.router.navigate(['/home']);
      } else {
        this.antiquity = antiquity;
        this.imageService.getImageFromAntiquity(antiquity.idAntiquity).pipe(
          mergeMap(photoPaths => {
            const urlRequests = photoPaths.map(photoPath =>
              this.imageService.getImageUrl(photoPath)
            );
            return forkJoin(urlRequests);  // Wait for all requests to finish
          })
        ).subscribe(urls => {
          this.imageUrls = urls;
        });
      }
    });
  }

  // Method to go to the next image
  nextImage(): void {
    this.currentImageIndex = (this.currentImageIndex + 1) % this.imageUrls.length;
  }

  // Method to go to the previous image
  prevImage(): void {
    this.currentImageIndex = (this.currentImageIndex - 1 + this.imageUrls.length) % this.imageUrls.length;
  }
}