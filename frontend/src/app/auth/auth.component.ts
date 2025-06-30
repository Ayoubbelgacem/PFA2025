import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';

interface SignUpForm {
  nom: string;
  prenom: string;
  email: string;
  password: string;
  role: string;
}

interface SignInForm {
  email: string;
  password: string;
}

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './auth.component.html',
  styleUrls: ['./auth.component.css']
})
export class AuthComponent {
  signUpForm: SignUpForm = {
    nom: '',
    prenom: '',
    email: '',
    password: '',
    role: 'collaborateur'
  };

  signInForm: SignInForm = {
    email: '',
    password: ''
  };

  isRightPanelActive = false;
  errorMessageSignUp = '';
  errorMessageSignIn = '';

  constructor(private authService: AuthService, private router: Router) {}

  togglePanel(state: boolean): void {
    this.isRightPanelActive = state;
    this.errorMessageSignUp = '';
    this.errorMessageSignIn = '';
  }

  onSignUp(): void {
    this.errorMessageSignUp = '';
    this.authService.register(this.signUpForm).subscribe({
      next: (res) => {
        alert("Inscription réussie !");
        this.togglePanel(false);
      },
      error: (err) => {
        this.errorMessageSignUp = err.error?.message || "Erreur d'inscription.";
        console.error(err);
      }
    });
  }

  onForgotPassword(): void {
  this.router.navigate(['/forgot-password']);
}



  onSignIn(): void {
  this.errorMessageSignIn = '';
  this.authService.login(this.signInForm).subscribe({
    next: () => {
      this.router.navigate(['/home']);
    },
    error: (err) => {
      this.errorMessageSignIn = 'Email ou mot de passe invalide';
      console.error(err);
    }
  });
}
}
