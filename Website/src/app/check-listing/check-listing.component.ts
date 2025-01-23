import { Component, OnInit } from '@angular/core';
import { ListingService } from '../../service/listing.service';
import { Antiquity } from '../../modele/DtoListing';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../service/auth.service';
import { ThemeService } from '../../service/theme.service';
import { Review } from '../../modele/Review';

@Component({
  selector: 'app-check-listing',
  templateUrl: './check-listing.component.html',
  styleUrl: './check-listing.component.css'
})
export class CheckListingComponent implements OnInit{

  currentTheme: 'dark' | 'light' = 'light'; // Actual theme, by default light
  antiquity!: Antiquity;
  isPopupVisible: boolean = false;
  review: Review =
  {
    note_title: "",
    note_description: "",
    note_price: "",
    note_photo: ""
  };
  messageErreur : string = "";

  constructor( private route: ActivatedRoute, private listingService : ListingService, private router : Router, private authService : AuthService, private themeService : ThemeService){}

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
          //If the state of the antiquity isn't 0 (need to be check or 2 already checked but modified), return to the list-antiquity-verify
          if (antiquity.state !== 0 && antiquity.state !== 2) {
            this.router.navigate(['/list-antiquity-verify']);
          } else {
            this.antiquity = antiquity;
            //Get the user details
            const userDetails = this.authService.getUserDetails();
            //Your mail and the mail antiquarian of the antiquity isn't the same, return to home
            if(userDetails.email!==this.antiquity.mailAntiquarian) this.router.navigate(['/home']);
          }
        });
      } else { // If not id, return to the list-antiquity-verify
        this.router.navigate(['/list-antiquity-verify']);
      }
    });
  }

  //Function to accepte the antiquity
    acceptAntiquity(){
      //Popup to be sure if you want to accept the antiquity
     const isConfirm = confirm("Are you sure to accept this listing ?");
     if(isConfirm){
      this.listingService.acceptAntiquity(this.antiquity).subscribe({
       next : (data: any) => { // Succes 
        // Convert plain object to Map
          const responseMap = new Map(Object.entries(data));
          alert(responseMap.get('message')); 
          this.router.navigate(['/list-antiquity-verify']);
        },
        error: (err : Map<String, String>)=>{ //Fail
          console.error('Error accepting antiquity:', err.get("message"));
        }
      });
     }
    }

    //Function to open the popup
    openOrClosePopup(){
      this.isPopupVisible = !this.isPopupVisible;
    }

    //Function to reject the antiquity
    rejectAntiquity(){
      //Check if every fields is empty, so display a error message.
      if(this.review.note_title=="" && this.review.note_description=="" && this.review.note_price=="" && this.review.note_photo==""){
        this.messageErreur="At leats one field have to be fill !";
        return;
      }
      //If at least one of the field is fill, ask if the user is sure to recject the antiquity
      const isConfirm = confirm("Are you sure to reject this listing ?");
     if(isConfirm){
      this.listingService.rejectAntiquity(this.antiquity,this.review).subscribe({
        next : (data: any) => { // Succes
          // Convert plain object to Map
          const responseMap = new Map(Object.entries(data));
          alert(responseMap.get('message')); 
          this.router.navigate(['/list-antiquity-verify']);
        },
        error: (err : Map<String, String>)=>{ //Fail
          console.error('Error rejecting antiquity:', err.get("message"));
        }
      });
     }
    }


}
