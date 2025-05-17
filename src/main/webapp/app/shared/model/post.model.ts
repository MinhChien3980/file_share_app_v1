import dayjs from 'dayjs';
import { IUser } from 'app/shared/model/user.model';
import { ITag } from 'app/shared/model/tag.model';
import { Privacy } from 'app/shared/model/enumerations/privacy.model';

export interface IPost {
  id?: number;
  content?: string;
  createdAt?: dayjs.Dayjs;
  updatedAt?: dayjs.Dayjs;
  locationName?: string | null;
  locationLat?: number | null;
  locationLong?: number | null;
  privacy?: keyof typeof Privacy;
  scheduledAt?: dayjs.Dayjs | null;
  viewCount?: number;
  commentCount?: number;
  shareCount?: number;
  reactionCount?: number;
  user?: IUser | null;
  tags?: ITag[] | null;
}

export const defaultValue: Readonly<IPost> = {};
