import { Injectable, Injector } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { UserService } from '../service/user.service';
import { GroupService } from '../service/group.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private keycloakInitialized = false; // Is Keycloak Initialized ?
  public userDetails: any = null; // To store the user's info
  private loggedInSubject = new BehaviorSubject<boolean>(false); // BehaviorSubject for login status
  private _userService!: UserService;
  private _groupService!: GroupService;

  constructor(private keycloakService: KeycloakService, private http: HttpClient, private router: Router, private injector: Injector) {} // Inject Router

  private get userService(): UserService {
    if (!this._userService) {
      this._userService = this.injector.get(UserService);
    }
    return this._userService;
  }

  private get groupService(): GroupService {
    if (!this._groupService) {
      this._groupService = this.injector.get(GroupService);
    }
    return this._groupService;
  }

  // Method to initialize Keycloak
  async initKeycloak(): Promise<boolean> {
    if (!this.keycloakInitialized) {
      try {
        const keycloakInitPromise = this.keycloakService.init({
          config: {
            url: 'http://localhost:8081/', // Keycloak server URL
            realm: 'anticairapp',               // Keycloak realm name
            clientId: 'anticairapp',        // Keycloak client ID
          },
          initOptions: {
            onLoad: 'check-sso',
            silentCheckSsoRedirectUri: window.location.origin + '/assets/silent-check-sso.html',
          },

        });

        const timeoutPromise = new Promise<boolean>((_, reject) =>
          setTimeout(() => reject(new Error('Keycloak initialization timed out')), 5000)
        );

        await Promise.race([keycloakInitPromise, timeoutPromise]);

        this.keycloakInitialized = true;
        await this.loadUserProfile();
        this.loggedInSubject.next(await this.keycloakService.isLoggedIn()); // Update login status
        return true;
      } catch (error) {
        console.error('Keycloak initialization failed or timed out', error);
        this.keycloakInitialized = false; // Mark as not initialized
        return false; // Indicate failure
      }
    }
    return true; // Already initialized
  }

  // Method to login through Keycloak
  async login(): Promise<void> {
    const initialized = await this.initKeycloak();
    if (initialized) {
      await this.keycloakService.login();
      await this.loadUserProfile(); // Load user info after login
    } else {
      this.keycloakInitialized = false;
      this.loggedInSubject.next(false); // Update login status on failure
      console.error('Cannot login because Keycloak failed to initialize.');
    }
  }

  // Method to logout through Keycloak
  async logout(): Promise<void> {
    if (this.userDetails) {
      this.userDetails = null; // Clear user details on logout
      await this.keycloakService.logout();
      this.loggedInSubject.next(false); // Update login status
    } else {
      console.error('Cannot logout because Keycloak is not initialized or user is not logged in.');
    }
  }

  // Method to check if user is logged
  isLoggedIn(): BehaviorSubject<boolean> {
    return this.loggedInSubject; // Return the BehaviorSubject for state management
  }

  // Method to load user profile details from Keycloak
  async loadUserProfile(): Promise<void> {
    if (await this.keycloakService.isLoggedIn()) {
      const userProfile = await this.keycloakService.loadUserProfile();
      const tokenParsed = this.keycloakService.getKeycloakInstance().tokenParsed;
      const phoneNumber = this.getFirstElement(userProfile['attributes']?.['phoneNumber']);
      const balance = this.getFirstElement(userProfile['attributes']?.['balance']);
      // Initialize userDetails with basic information
      this.userDetails = {
        email: userProfile.email,
        firstName: userProfile.firstName,
        lastName: userProfile.lastName,
        phoneNumber: phoneNumber,
        balance: balance,
        groups: tokenParsed && tokenParsed['groups'] ? this.extractGroups(tokenParsed['groups']) : ''
      };
      this.loggedInSubject.next(true); // Update login status

      if (this.keycloakInitialized) {
        this.initAdminAccount();
      } else {
        console.error('Cannot logout because Keycloak is not initialized');
      }
    }

  }
  // Method to initialize the admin account after his connection
  async initAdminAccount(): Promise<void> {

    // Check if keycloak is initialized
    if (!this.keycloakInitialized) {
      console.error('Cannot logout because Keycloak is not initialized');
      return;
    }

    // Check if the inforamtions about the user is storaged and if he has a email
    if (!this.userDetails || !this.userDetails.email) {
      console.error('No information about the user');
      return;
    }

    // Get the raw token
    const rawToken = await this.keycloakService.getToken();
    // Get the number of users in the database
    const nbrUser = await this.userService.numberOfUsers(rawToken);
    // If the number of users in the database is 1, the user will be add to the group Admin
    if (1 == nbrUser) {
      // Add the user in the group Admin
      await this.groupService.addGroupToUser(this.userDetails.email, "Admin");
    }

  }

  // Returns user details if available
  getUserDetails(): any {
    return this.userDetails;
  }

  // Get the user's token
  async getToken(): Promise<string> {
    return this.keycloakService.getToken();
  }

  // Method to get the first element from an array
  getFirstElement(array : any) {
    if (Array.isArray(array) && array.length > 0) {
      return array[0];
    }
    return null; // Return null if the array is empty or not an array
  }

  // Method to extract groups and return them as a comma-separated string
  private extractGroups(groups: string[]): string {
    return groups.join(', '); // Join the groups array into a single string with commas
  }

  isAdmin(): boolean {
    return this.userDetails?.groups?.includes('Admin') || false; // Check if the user is an admin
  }


}
