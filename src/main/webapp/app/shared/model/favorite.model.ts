import dayjs from 'dayjs';
import { IPost } from 'app/shared/model/post.model';
import { IUser } from 'app/shared/model/user.model';

export interface IFavorite {
  id?: number;
  savedAt?: dayjs.Dayjs;
  post?: IPost | null;
  user?: IUser | null;
}

export const defaultValue: Readonly<IFavorite> = {};
