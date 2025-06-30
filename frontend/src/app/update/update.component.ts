import { Component, Input, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../services/user.service'; // 👈

@Component({
  selector: 'app-update',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './update.component.html',
  styleUrls: ['./update.component.css'],
  schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class UpdateComponent {
  @Input() user: any = {
    photoProfil: '',
    nom: '',
    prenom: '',
    email: ''
  };

  selectedFile: File | null = null;

  constructor(private userService: UserService) {}

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];

      const reader = new FileReader();
      reader.onload = (e: ProgressEvent<FileReader>) => {
        if (e.target?.result) {
          this.user.photoProfil = e.target.result as string;
        }
      };
      reader.readAsDataURL(this.selectedFile);
    }
  }

  triggerFileInput(): void {
    const fileInput = document.querySelector('.file-input') as HTMLElement;
    fileInput.click();
  }

  onSubmit(): void {
    console.log('Données utilisateur à mettre à jour:', this.user);

    this.userService.updateUser(this.user).subscribe({
      next: (res) => {
        console.log('Profil mis à jour avec succès', res);
        alert('Profil mis à jour ✅');
      },
      error: (err) => {
        console.error('Erreur lors de la mise à jour du profil', err);
        alert('Erreur ❌');
      }
    });
  }
}
