import { Component, OnInit, ViewChild } from '@angular/core';
import { ThemeService } from '../../service/theme.service';
import { Router } from '@angular/router';
import { ListingService } from '../../service/listing.service';
import { AuthService } from '../../service/auth.service';

@Component({
  selector: 'app-create-listing',
  templateUrl: './create-listing.component.html',
  styleUrl: './create-listing.component.css'
})
export class CreateListingComponent {
  @ViewChild('fileInput') fileInput: any;

  currentTheme: 'dark' | 'light' = 'light';

  email: string = '';
  title: string = '';
  description: string = '';
  price: number = 0;
  photos: File[] = [];
  imagePreviews: String[] = []; 

  errorMessage: string = '';
  successMessage: string = '';

  stayOnPage: boolean = false;

  constructor(
    private themeService: ThemeService,
    private router: Router, 
    private authService: AuthService, 
    private listingService: ListingService
  ) {}

  ngOnInit() {
    this.themeService.theme$.subscribe(theme => {
      this.currentTheme = theme;
    });

    this.email = this.authService.getUserDetails().email;
  }

  redirectToHome() {
    this.router.navigate(['/home']);
  }

  // Method who manages the file selection
  onFileSelected(event: any) {
    this.photos = Array.from(event.target.files);
    this.imagePreviews = [];
    for (let file of this.photos) {
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.imagePreviews.push(e.target.result);
      };
      reader.readAsDataURL(file);
    }
  }

  // Method who submits the form
  onSubmit() {
    // Reset the error and success messages
    this.errorMessage = '';
    this.successMessage = '';
    // Verify the field are not empty
    if (!this.title) {
      this.errorMessage = 'Title is required';
      return;
    }

    if (!this.description) {
      this.errorMessage = 'Description is required';
      return;
    }

    if (this.price <= 0) {
      this.errorMessage = 'Price must be greater than 0';
      return;
    }

    // Validation for Files
    if (this.photos && this.photos.length > 0) {
      if(this.photos.length <= 10) {
        for (let photo of this.photos) {
          if (!photo.name.toLowerCase().endsWith('.jpg')) {
            this.errorMessage = 'All files must be in .jpg format';
            return;
          }
        }

      } else {
        this.errorMessage = 'You can only upload 10 photos maximum';
        return;
      }

    } else {
      this.errorMessage = 'At least one photo is required';
      return;
    }

    this.listingService.createListing(
      this.email,
      this.title, 
      this.description, 
      this.price, 
      this.photos,
    ).subscribe({
      next: (response) => {
        this.resetForm();

        if(this.stayOnPage) {
          this.successMessage = 'Listing created successfully!';
        } else {
          this.successMessage = 'Listing created successfully! You are being redirected to the home page';
          setTimeout(() => {
            this.redirectToHome();
          }, 3000);
        }
        
        
      },
      error: (error) => {
        // Gestion des erreurs
        if (error.status === 400) {
          this.errorMessage = 'Invalid input. Please check your data.';
        } else if (error.status === 401) {
          this.errorMessage = 'Unauthorized. Please log in again.';
        } else if (error.status === 500) {
          this.errorMessage = 'Server error. Please try again later.';
        } else if(error.status === 413) {
          this.errorMessage = 'File too large. Please upload files smaller';
        } else {
          this.errorMessage = error.message || 'An unexpected error occurred';
        }
      }
    });
  }

  // Method who resets the form
  resetForm() {
    this.title = '';
    this.description = '';
    this.price = 0;
    this.photos = [];
    this.imagePreviews = [];
    this.fileInput.nativeElement.value = '';
  }

  // Method to remove a selected image
  removeImage(index: number) {
    this.photos.splice(index, 1);
    this.imagePreviews.splice(index, 1);

    // Create a new DataTransfer object and assign the remaining files to it
    const dataTransfer = new DataTransfer();
    this.photos.forEach(file => dataTransfer.items.add(file));
    this.fileInput.nativeElement.files = dataTransfer.files;

    // Reset the file input if all images are removed
    if (this.photos.length === 0) {
      this.fileInput.nativeElement.value = '';
    }
  }
}

