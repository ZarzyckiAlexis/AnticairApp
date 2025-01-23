import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../../../service/admin.service';

@Component({
  selector: 'app-force-password-reset',
  templateUrl: './force-password-reset.component.html',
  styleUrls: ['./force-password-reset.component.css']
})
export class ForcePasswordResetComponent implements OnInit {
  users: any[] = []; // Liste des utilisateurs Keycloak

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.adminService.getUsers().subscribe((data: any) => {
      this.users = data; // Récupération des utilisateurs depuis Keycloak
    });
  }

  forcePasswordReset(userId: string): void {
    this.adminService.forcePasswordReset(userId).subscribe(() => {
      alert('Réinitialisation du mot de passe réussie.');
      this.loadUsers();
    });
  }
}
