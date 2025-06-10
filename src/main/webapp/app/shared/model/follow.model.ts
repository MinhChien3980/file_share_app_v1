import dayjs from 'dayjs';
import { IUser } from 'app/shared/model/user.model';

export interface IFollow {
  id?: number;
  createdAt?: dayjs.Dayjs;
  follower?: IUser | null;
  following?: IUser | null;
}

export const defaultValue: Readonly<IFollow> = {};
