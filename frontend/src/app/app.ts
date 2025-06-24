import { Component } from '@angular/core';
import { AuthComponent } from './auth/auth.component'; // Notez le .component

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [AuthComponent], // AuthComponent doit être standalone
  template: '<app-auth></app-auth>',
})
export class AppComponent {
  title = 'PFA2025';
}