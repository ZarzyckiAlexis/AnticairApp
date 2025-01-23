import { APP_INITIALIZER, NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { KeycloakAngularModule, KeycloakService } from 'keycloak-angular';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NavbarComponent } from './navbar/navbar.component';
import { HomeComponent } from './home/home.component';
import { FooterComponent } from './footer/footer.component';
import { SellComponent } from './sell/sell.component';
import { DashboardComponent } from './admin/dashboard/dashboard.component';
import { SidebarComponent } from './admin/sidebar/sidebar.component';
import { UsersComponent } from './admin/users/users.component';
import { MatDialogModule } from '@angular/material/dialog';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { EditGroupsComponent } from './admin/users/edit-groups/edit-groups.component';
import { PhonenumberPipe } from './pipe/phonenumber.pipe';
import { CreateListingComponent } from './create-listing/create-listing.component';
import { EditListingComponent } from './edit-listing/edit-listing.component';
import { CheckListingComponent } from './check-listing/check-listing.component';
import { OneAntiquityComponent } from './one-antiquity/one-antiquity.component';
import { AuthService } from '../service/auth.service';
import { PaymentConfComponent } from './payment-conf/payment-conf.component';
import { BuyListingComponent } from './buy-listing/buy-listing.component';
import { ListListingVerifyComponent } from './list-listing-verify/list-listing-verify.component';

// Function to initialize Keycloak
export function initializeKeycloak(authService: AuthService) {
  return (): Promise<any> => {
    return authService.initKeycloak(); // Call the init method from AuthService
  };
}

@NgModule({
  declarations: [
    AppComponent,
    NavbarComponent,
    HomeComponent,
    FooterComponent,
    SellComponent,
    DashboardComponent,
    SidebarComponent,
    UsersComponent,
    EditGroupsComponent,
    PhonenumberPipe,
    CreateListingComponent,
    EditListingComponent,
    CheckListingComponent,
    OneAntiquityComponent,
    PaymentConfComponent,
    BuyListingComponent,
    ListListingVerifyComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    KeycloakAngularModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatCheckboxModule,
  ],
  providers: [
    {
      provide: APP_INITIALIZER,
      useFactory: initializeKeycloak,
      deps: [AuthService], // Ensure AuthService is injected
      multi: true,
    },
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
