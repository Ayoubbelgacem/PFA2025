import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reset-password.component.html',
  styleUrls: ['../auth/auth.component.css'] // ou autre fichier CSS
})
export class ResetPasswordComponent {
  token: string = '';
  password: string = '';
  confirmPassword: string = '';
  message: string = '';
  error: string = '';

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.token = params['token'] || '';
    });
  }

  onResetPassword(): void {
    if (this.password !== this.confirmPassword) {
      this.error = "Les mots de passe ne correspondent pas.";
      this.message = '';
      return;
    }

    this.authService.resetPassword(this.token, this.password).subscribe({
      next: (res) => {
        this.message = res?.message || 'Mot de passe réinitialisé avec succès.';
        this.error = '';
      },
      error: (err) => {
        this.error = err.error?.message || 'Erreur lors de la réinitialisation.';
        this.message = '';
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/auth']);
  }
}
