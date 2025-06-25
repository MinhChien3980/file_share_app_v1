import dayjs from 'dayjs';
import { IPost } from 'app/shared/model/post.model';

export interface IFile {
  id?: number;
  fileName?: string;
  fileUrl?: string;
  mimeType?: string;
  fileSize?: number;
  uploadedAt?: dayjs.Dayjs;
  post?: IPost | null;
}

export const defaultValue: Readonly<IFile> = {};
