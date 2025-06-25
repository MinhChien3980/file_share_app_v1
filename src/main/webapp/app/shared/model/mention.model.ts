import { IPost } from 'app/shared/model/post.model';
import { IUser } from 'app/shared/model/user.model';

export interface IMention {
  id?: number;
  post?: IPost | null;
  user?: IUser | null;
}

export const defaultValue: Readonly<IMention> = {};
