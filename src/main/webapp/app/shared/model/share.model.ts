import dayjs from 'dayjs';
import { IPost } from 'app/shared/model/post.model';
import { IUser } from 'app/shared/model/user.model';

export interface IShare {
  id?: number;
  createdAt?: dayjs.Dayjs;
  post?: IPost | null;
  user?: IUser | null;
}

export const defaultValue: Readonly<IShare> = {};
