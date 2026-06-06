import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { EmailRequest, EmailResponse } from '../models/email-request.model';
import { environment } from '../../environments/environment';

const API_URL_KEY = 'emailServiceApiUrl';

@Injectable({ providedIn: 'root' })
export class EmailService {

  constructor(private http: HttpClient) {}

  getApiUrl(): string {
    return localStorage.getItem(API_URL_KEY) ?? environment.defaultApiUrl;
  }

  setApiUrl(url: string): void {
    localStorage.setItem(API_URL_KEY, url.replace(/\/$/, '')); // strip trailing slash
  }

  sendEmail(request: EmailRequest): Observable<EmailResponse> {
    const form = new FormData();
    form.append('to', request.to);

    if (request.subject?.trim()) {
      form.append('subject', request.subject.trim());
    }
    if (request.body?.trim()) {
      form.append('body', request.body.trim());
    }
    if (request.attachment) {
      form.append('attachment', request.attachment, request.attachment.name);
      form.append('attachmentName', request.attachment.name);
    }
    if (request.replacements && Object.keys(request.replacements).length > 0) {
      form.append('replacements', JSON.stringify(request.replacements));
    }

    return this.http.post<EmailResponse>(`${this.getApiUrl()}/api/email/send`, form);
  }

  checkHealth(): Observable<EmailResponse> {
    return this.http.get<EmailResponse>(`${this.getApiUrl()}/api/email/health`);
  }
}
