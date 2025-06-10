import dayjs from 'dayjs';
import { IPost } from 'app/shared/model/post.model';
import { IUser } from 'app/shared/model/user.model';
import { ReactionType } from 'app/shared/model/enumerations/reaction-type.model';

export interface IReaction {
  id?: number;
  type?: keyof typeof ReactionType;
  reactedAt?: dayjs.Dayjs;
  post?: IPost | null;
  user?: IUser | null;
}

export const defaultValue: Readonly<IReaction> = {};
