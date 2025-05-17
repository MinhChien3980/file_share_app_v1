import dayjs from 'dayjs';
import { IUser } from 'app/shared/model/user.model';

export interface INotification {
  id?: number;
  message?: string;
  isRead?: boolean;
  createdAt?: dayjs.Dayjs;
  user?: IUser | null;
}

export const defaultValue: Readonly<INotification> = {
  isRead: false,
};
