import { Component, OnInit, OnDestroy, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserService } from '../services/user.service';
import { UpdateComponent } from '../update/update.component'; // 👈 Import du composant

@Component({
  selector: 'app-home',
  standalone: true,
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css'],
  imports: [
    CommonModule,
    UpdateComponent // 👈 ajoute le composant ici !
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class HomeComponent implements OnInit, OnDestroy {
  user: any = null;
  showUpdateForm = false;
  private scriptElement?: HTMLScriptElement;

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    this.loadScript();
    this.userService.getCurrentUser().subscribe({
      next: (data) => {
        console.log('Données utilisateur reçues :', data);
        this.user = data;
      },
      error: (err) => console.error('Erreur utilisateur:', err)
    });
  }



onDeleteAccount(event: Event): void {
  event.preventDefault();

  if (confirm('Êtes-vous sûr de vouloir supprimer votre compte ? Cette action est irréversible.')) {
    this.userService.deleteAccount().subscribe({
      next: () => {
        alert('Votre compte a été supprimé avec succès.');
        // Redirection possible, ex :
        // window.location.href = '/login';
      },
      error: (err) => {
        console.error('Erreur lors de la suppression:', err);
        alert('Erreur lors de la suppression du compte.');
      }
    });
  }
}









  ngOnDestroy(): void {
    if (this.scriptElement) {
      document.body.removeChild(this.scriptElement);
    }
  }

  private loadScript(): void {
    this.scriptElement = document.createElement('script');
    this.scriptElement.src = 'assets/styles/js/script.js';
    this.scriptElement.type = 'text/javascript';
    this.scriptElement.async = true;
    document.body.appendChild(this.scriptElement);
  }
}
