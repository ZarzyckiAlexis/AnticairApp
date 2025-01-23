import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { ThemeService } from '../../service/theme.service';
import { ListingService } from '../../service/listing.service';
import { Antiquity } from '../../modele/DtoListing';
import { forkJoin } from 'rxjs';
import { mergeMap } from 'rxjs/operators';
import { ImageServiceService } from '../../service/image-service.service';

@Component({
  selector: 'app-payment-conf',
  templateUrl: './payment-conf.component.html',
  styleUrls: ['./payment-conf.component.css']
})
export class PaymentConfComponent implements OnInit {

  currentTheme: 'dark' | 'light' = 'light';
  paymentStatus: 'success' | 'error' | null = null;
  invoiceNumber: string | null = null;
  paymentId: string | null = null;
  payerId: string | null = null;
  antiquity: Antiquity | null = null;
  loading: boolean = true;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private listingService: ListingService,
    private themeService: ThemeService,
    private imageService: ImageServiceService
  ) {}

  ngOnInit(): void {
    // Subscribe to Theme event
    this.themeService.theme$.subscribe(theme => {
      this.currentTheme = theme;
    });

    // Extract URL parameters
    this.paymentId = this.route.snapshot.queryParamMap.get('paymentId');
    this.payerId = this.route.snapshot.queryParamMap.get('PayerID');

    if (this.paymentId && this.payerId) {
      // Call the API to execute the payment
      this.executePayment(this.paymentId, this.payerId);
    } else {
      this.paymentStatus = 'error';
      this.loading = false;
    }
  }

  executePayment(paymentId: string, payerId: string): void {
    this.listingService.executePayment(paymentId, payerId).subscribe(
      (response: any) => {
        this.paymentStatus = 'success';
        console.log('Payment executed successfully', response);
        this.invoiceNumber = response.invoiceNumber;
        this.loading = false;
      },
      error => {
        console.log('Payment execution failed', error.error.message);
        if (error.error.message && error.error.message.includes("PAYMENT_ALREADY_DONE")) {
          this.paymentStatus = 'success';
        } else {
          this.paymentStatus = 'error';
          console.error('Payment execution failed', error);
        }
        this.loading = false;
      }
    );
  }

  retryPayment(): void {
    this.router.navigate(['/sell']);
  }
}