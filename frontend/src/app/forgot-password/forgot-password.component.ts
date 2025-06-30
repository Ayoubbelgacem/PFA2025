import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './forgot-password.component.html',
  styleUrls: ['../auth/auth.component.css']
})
export class ForgotPasswordComponent {
  email: string = '';
  message: string = '';
  error: string = '';

  constructor(private authService: AuthService, private router: Router) {}

  onSendResetLink(): void {
    this.authService.forgotPassword(this.email).subscribe({
      next: (res) => {
        this.message = res?.message || 'Un lien de réinitialisation a été envoyé.';
        this.error = '';
      },
      error: (err) => {
        this.error = err.error?.message || 'Erreur lors de l’envoi.';
        this.message = '';
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/auth']);
  }
}
