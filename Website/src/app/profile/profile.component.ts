import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {CurrencyPipe, NgClass, NgForOf, NgIf} from "@angular/common";
import { ThemeService } from '../../service/theme.service';
import { AuthService } from '../../service/auth.service';
import { UserService } from '../../service/user.service';
import {RgpdService} from '../../service/rgpd.service';
import {Router} from '@angular/router';
import {Antiquity} from '../../modele/DtoListing';
import {ListingService} from '../../service/listing.service';
import {ImageServiceService} from '../../service/image-service.service';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  standalone: true,

  imports: [FormsModule, NgIf, CurrencyPipe, NgForOf],

})
export class ProfileComponent implements OnInit {
  showSuccessMessage: boolean = false;
  showErrorMessage: boolean = false;
  userDetails: any = {};
  isLoading: boolean = true;
  currentTheme: 'dark' | 'light' = 'light'; // Actual theme, by default light
  antiquities: Antiquity[] = []

  constructor(
      private authService: AuthService,
      private userService: UserService,
      private themeService: ThemeService,
      private rgpdService: RgpdService,
      private router : Router,
      private listingService: ListingService,
      private imageService: ImageServiceService
  ) {}


  async ngOnInit() {
    this.themeService.theme$.subscribe(theme => {
      this.currentTheme = theme;
    });
    try {
      this.userDetails = this.authService.getUserDetails();

      this.listingService.getListingSeller(this.userDetails['email']).subscribe(res => {
        this.antiquities = res;
        this.antiquities.forEach(antiquity => {
          let photos: string[] = []
          antiquity.photos.forEach(photo => {
            this.imageService.getImageUrl(photo).subscribe(image => {
              photos.push(image.toString());
            })
          })
          antiquity.photos = photos;
        });
      })


    } catch (error) {
      console.error('Error loading user details:', error);
    } finally {
      this.isLoading = false;
    }
  }

  viewDetails(id: number): void {
    this.router.navigate(['/edit/'+id]);
    console.log(`View details for antiquity with ID: ${id}`);
  }

  async saveChanges(): Promise<void> {
    try {
      const token = await this.authService.getToken();
      const response = await this.userService
          .updateUserProfile(this.userDetails, token)
          .toPromise();

      this.showSuccessMessage = true;

      setTimeout(() => {
        this.showSuccessMessage = false;
      }, 3000);
    } catch (error) {
      // @ts-ignore
      this.showErrorMessage = true;
      setTimeout(() => {
        this.showErrorMessage = false;
      }, 3000);
    }
  }


  logout(): void {
    this.authService.logout();

  }

  async deleteData() {
    const bool = confirm("Deleting user data = account disabled");
    if (bool) {
      const token = await this.authService.getToken().then(res => {
        this.rgpdService.updateUserProfile(this.userDetails, res).subscribe(res => {
          console.log(res);

          this.logout();


        });
      });



    }
  }
}
