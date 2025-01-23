import { Component } from '@angular/core';
import { ThemeService } from '../../service/theme.service';
import { ListingService } from '../../service/listing.service';
import { Antiquity } from '../../modele/DtoListing';
import { ImageServiceService } from '../../service/image-service.service';
import { forkJoin, mergeMap } from 'rxjs';
import { Router } from '@angular/router';

@Component({
  selector: 'app-sell',
  templateUrl: './sell.component.html',
  styleUrl: './sell.component.css'
})
export class SellComponent {
  currentTheme: 'dark' | 'light' = 'light';
  antiquities: Antiquity[] = [];
  filteredAntiquities: Antiquity[] = [];
  pictures: String[] = [];
  searchText: string = '';
  sortCriteria: string = 'id';
  currentPage: number = 1;
  itemsPerPage: number = 12;

  get paginatedAntiquities(): Antiquity[] {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    return this.filteredAntiquities.slice(startIndex, startIndex + this.itemsPerPage);
  }


  get totalPages(): number {
    return Math.ceil(this.filteredAntiquities.length / this.itemsPerPage);
  }

  constructor(
    private themeService: ThemeService,
    private listingService: ListingService,
    private imageService: ImageServiceService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.themeService.theme$.subscribe(theme => {
      this.currentTheme = theme;
    });

    this.listingService.getAllAntiquitiesChecked().subscribe(antiquities => {
      this.antiquities = antiquities;
      this.filteredAntiquities = antiquities;

      this.antiquities.forEach(antiquity => {
        this.imageService.getImageFromAntiquity(antiquity.idAntiquity).pipe(
          mergeMap(photoPaths => {
            const urlRequests = photoPaths.map(photoPath =>
              this.imageService.getImageUrl(photoPath)
            );
            return forkJoin(urlRequests);
          })
        ).subscribe(pictures => {
          antiquity.photos = pictures;

        });
      });
    });
  }

  onSearchChange(): void {
    this.currentPage = 1;
    if (this.searchText.trim() === '') {
      this.filteredAntiquities = this.antiquities;
    } else {
      this.filteredAntiquities = this.antiquities.filter(antiquity =>
        antiquity.titleAntiquity.toLowerCase().includes(this.searchText.toLowerCase()) ||
        antiquity.descriptionAntiquity.toLowerCase().includes(this.searchText.toLowerCase())
      );
    }
    this.onSortChange();
  }

  onSortChange(): void {
    if (this.sortCriteria === 'id') {
      this.filteredAntiquities.sort((a, b) => a.idAntiquity - b.idAntiquity);
    } else if (this.sortCriteria === 'price') {
      this.filteredAntiquities.sort((a, b) => a.priceAntiquity - b.priceAntiquity);
    }
  }

  changePage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
    }

  }

  getPages(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

  viewDetails(id: number): void {
    // Redirect to the details page
    this.router.navigate(['/sell', id]);
  }

}
