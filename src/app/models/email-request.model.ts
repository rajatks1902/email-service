export interface EmailRequest {
  to: string;
  subject?: string;
  body?: string;
  attachment?: File;
  attachmentName?: string;
  replacements?: Record<string, string>;
}

export interface EmailResponse {
  success: boolean;
  message: string;
}

export interface ReplacementEntry {
  key: string;
  value: string;
}
