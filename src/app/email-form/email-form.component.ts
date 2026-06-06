import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  FormArray,
  Validators,
  AbstractControl
} from '@angular/forms';
import { EmailService } from '../services/email.service';
import { ReplacementEntry, EmailResponse } from '../models/email-request.model';

@Component({
  selector: 'app-email-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './email-form.component.html',
  styleUrls: ['./email-form.component.scss']
})
export class EmailFormComponent implements OnInit {

  form!: FormGroup;
  settingsForm!: FormGroup;

  selectedFile: File | null = null;
  isLoading = false;
  response: EmailResponse | null = null;
  error: string | null = null;
  showSettings = false;
  healthStatus: 'unknown' | 'ok' | 'error' = 'unknown';
  readonly bodyPlaceholder = 'Leave blank to use the default body template\n\nYou can write plain text or HTML here.\nUse {{name}}, {{message}} etc. to match your replacements.';

  constructor(
    private fb: FormBuilder,
    public emailService: EmailService
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      to:           ['', [Validators.required, Validators.email]],
      subject:      [''],
      body:         [''],
      replacements: this.fb.array([])
    });

    this.settingsForm = this.fb.group({
      apiUrl: [this.emailService.getApiUrl(), Validators.required]
    });
  }

  // ── replacements ───────────────────────────────────────────────────────────

  get replacements(): FormArray {
    return this.form.get('replacements') as FormArray;
  }

  addReplacement(): void {
    this.replacements.push(
      this.fb.group({ key: ['', Validators.required], value: [''] })
    );
  }

  removeReplacement(index: number): void {
    this.replacements.removeAt(index);
  }

  // ── file ───────────────────────────────────────────────────────────────────

  onFileChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFile = input.files?.[0] ?? null;
  }

  clearFile(): void {
    this.selectedFile = null;
    const input = document.getElementById('attachment') as HTMLInputElement;
    if (input) { input.value = ''; }
  }

  // ── settings ───────────────────────────────────────────────────────────────

  toggleSettings(): void {
    this.showSettings = !this.showSettings;
    if (this.showSettings) {
      this.settingsForm.patchValue({ apiUrl: this.emailService.getApiUrl() });
      this.healthStatus = 'unknown';
    }
  }

  saveSettings(): void {
    if (this.settingsForm.valid) {
      this.emailService.setApiUrl(this.settingsForm.value.apiUrl);
      this.showSettings = false;
    }
  }

  testConnection(): void {
    this.healthStatus = 'unknown';
    this.emailService.setApiUrl(this.settingsForm.value.apiUrl);
    this.emailService.checkHealth().subscribe({
      next:  () => { this.healthStatus = 'ok'; },
      error: () => { this.healthStatus = 'error'; }
    });
  }

  // ── submit ─────────────────────────────────────────────────────────────────

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.response  = null;
    this.error     = null;

    const replacementsMap: Record<string, string> = {};
    (this.replacements.value as ReplacementEntry[]).forEach(entry => {
      if (entry.key?.trim()) {
        replacementsMap[entry.key.trim()] = entry.value ?? '';
      }
    });

    this.emailService.sendEmail({
      to:           this.form.value.to,
      subject:      this.form.value.subject || undefined,
      body:         this.form.value.body    || undefined,
      attachment:   this.selectedFile       ?? undefined,
      replacements: Object.keys(replacementsMap).length ? replacementsMap : undefined
    }).subscribe({
      next:  res  => { this.response = res;             this.isLoading = false; },
      error: err  => {
        this.error     = err?.error?.message ?? 'Request failed. Check the API URL in Settings.';
        this.isLoading = false;
      }
    });
  }

  reset(): void {
    this.form.reset();
    this.replacements.clear();
    this.selectedFile = null;
    this.response     = null;
    this.error        = null;
    const input = document.getElementById('attachment') as HTMLInputElement;
    if (input) { input.value = ''; }
  }

  // ── helpers ────────────────────────────────────────────────────────────────

  fieldInvalid(name: string): boolean {
    const ctrl: AbstractControl | null = this.form.get(name);
    return !!(ctrl && ctrl.invalid && (ctrl.dirty || ctrl.touched));
  }

  replacementCtrl(index: number, field: string): AbstractControl {
    return (this.replacements.at(index) as FormGroup).get(field)!;
  }
}
