import { Component, OnInit } from '@angular/core';
import { ListingService } from '../../service/listing.service';
import { ActivatedRoute, Router } from '@angular/router';
import { Antiquity } from '../../modele/DtoListing';
import { ImageServiceService } from '../../service/image-service.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-edit-listing',
  templateUrl: './edit-listing.component.html',
  styleUrl: './edit-listing.component.css'
})
export class EditListingComponent implements OnInit {
  antiquity!: Antiquity;
  imagePreviews: String[] = []; // Previews of selected images
  selectedFiles: File[] = []; // Selected files for upload
  BlobFiles: Blob[] = []; // Loaded Blob files

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private antiquityService: ListingService,
    private imageService: ImageServiceService
  ) {}

  ngOnInit(): void {
    // Retrieve the ID from the URL
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadAntiquityById(id);
    }
  }

  // Load the antiquity from the API by its ID
  loadAntiquityById(id: string) {
    this.antiquityService.getAntiquityById(id).subscribe({
      next: (data) => {
        this.antiquity = data;

        // Log photos for debugging
        data.photos.forEach((photoPath, index) => {
          console.log(`Photo ${index}: ${photoPath}`);
        });

        // Prepare observables to load images
        const imageObservables = data.photos.map(photoPath =>
          this.imageService.getImageFile(photoPath)
        );

        // Load images using forkJoin
        forkJoin(imageObservables).subscribe({
          next: (urls) => {
            this.BlobFiles = urls;
            console.log('BlobFiles loaded:', this.BlobFiles);

            // Process images after they are loaded
            this.BlobFiles.forEach((photoPath) => {
              const preview = URL.createObjectURL(photoPath);
              this.imagePreviews.push(preview);
              this.selectedFiles.push(new File([photoPath], this.extractBlobFileName(preview),{ type: 'image/jpeg' }));
            });

            console.log('Image previews:', this.imagePreviews);
          },
          error: (err) => console.error('Error loading images:', err),
        });
      },
      error: (err) => console.error('Error loading antiquity:', err),
    });
  }

  // Extract the file name from a Blob URL
  extractBlobFileName(url: string): string {
    try {
      const parts = url.split('/');
      return parts[parts.length - 1];
    } catch (error) {
      console.error('Error extracting Blob file name:', error);
      return '';
    }
  }

  // Handle file selection from the file input
  onFileSelect(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      Array.from(input.files).forEach((file) => {
        if (this.imagePreviews.length < 10) {
          // Convert the selected file to JPEG format
          const reader = new FileReader();

          reader.onload = (e: ProgressEvent<FileReader>) => {
            if (e.target?.result) {
              const imgDataUrl = e.target.result as string;

              // Convert the Data URL to a Blob
              fetch(imgDataUrl)
                .then(res => res.blob())
                .then(blob => {
                  // Create a new File with type JPEG
                  const newFile = new File([blob], `${file.name.replace(/\.[^/.]+$/, "")}.jpg`, { type: 'image/jpeg' });

                  this.selectedFiles.push(newFile);

                  // Update the preview
                  this.imagePreviews.push(imgDataUrl);
                });
            }
          };

          reader.readAsDataURL(file);
        }
      });
    }
  }

  // Remove an image from the previews
  removeImage(index: number): void {
    this.imagePreviews.splice(index, 1); // Remove the image from the preview list
    this.selectedFiles.splice(index, 1); // Remove the corresponding file
  }

  // Submit the form
  onSubmit(): void {
    if (!this.antiquity.idAntiquity) {
      console.error('Antiquity ID is missing');
      return;
    }
    if (this.selectedFiles.length <= 0) {
      alert("The antiquity must have at least one image");
      return;
    }
    if(this.selectedFiles.length > 10){
      alert("The antiquity must have max 10 images");
      return;
    }

    // Send the updated antiquity with selected images
    this.antiquityService.updateAntiquityWithPhotos(
      this.antiquity.idAntiquity,
      this.antiquity,
      this.selectedFiles
    ).subscribe({
      next: () => {
        alert('Antiquity updated successfully');
        this.router.navigate(['/antiquities']);
      },
      error: (err) => {
        console.error('Error updating antiquity:', err);
        alert('Error updating the antiquity');
      }
    });
  }

  deleteById(index: number) {
    const bool = confirm("Are you sure you want to delete this antiquity?");
    if(bool) this.antiquityService.deleteById(index).subscribe(res => {
      this.router.navigate(['/profile']);
    })
  }
}
