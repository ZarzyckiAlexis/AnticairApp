import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'belgianPhone'
})
export class PhonenumberPipe implements PipeTransform {

  transform(phoneNumber: string): string {
    // Check if phoneNumber is null or undefined
    if (phoneNumber == null) return '';

    // Convert to string
    const phoneStr = String(phoneNumber);

    // Remove all non-numeric characters
    const cleaned = phoneStr.replace(/\D/g, '');
    
    // Check if cleaned string is empty
    if (!cleaned) return '';

    // Handle mobile numbers (starting with 4, 5, 6, 7)
    if (/^(32)?(4|5|6|7)/.test(cleaned) || cleaned.startsWith('4')) {
      // Mobile format: +32 (0)473 12 34 56
      const mobileNumber = cleaned.replace(/^32/, '');
      return `+32 (0)${mobileNumber.slice(0,3)} ${mobileNumber.slice(3,5)} ${mobileNumber.slice(5,7)} ${mobileNumber.slice(7)}`;
    }
    
    // Handle landline numbers (usually starting with 02, 03, 09, etc.)
    if (/^(32)?(2|3|9)/.test(cleaned) || cleaned.startsWith('0')) {
      // Landline format: +32 (0)2 123 45 67
      const fixedNumber = cleaned.replace(/^32|^0/, '');
      return `+32 (0)${fixedNumber.slice(0,1)} ${fixedNumber.slice(1,4)} ${fixedNumber.slice(4,6)} ${fixedNumber.slice(6)}`;
    }
    
    // Default case
    return `+32 (0)${cleaned.slice(0,3)} ${cleaned.slice(3,5)} ${cleaned.slice(5,7)} ${cleaned.slice(7)}`;

}
}
