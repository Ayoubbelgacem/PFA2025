import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule], // facultatif selon ce que tu affiches
  template: `<h2>Bienvenue sur le tableau de bord</h2>`
})
export class DashboardComponent {}
