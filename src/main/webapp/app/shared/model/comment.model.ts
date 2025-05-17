import dayjs from 'dayjs';
import { IPost } from 'app/shared/model/post.model';
import { IUser } from 'app/shared/model/user.model';

export interface IComment {
  id?: number;
  content?: string;
  createdAt?: dayjs.Dayjs;
  post?: IPost | null;
  user?: IUser | null;
  parentComment?: IComment | null;
}

export const defaultValue: Readonly<IComment> = {};
