import post from 'app/entities/post/post.reducer';
import file from 'app/entities/file/file.reducer';
import comment from 'app/entities/comment/comment.reducer';
import favorite from 'app/entities/favorite/favorite.reducer';
import follow from 'app/entities/follow/follow.reducer';
import notification from 'app/entities/notification/notification.reducer';
import tag from 'app/entities/tag/tag.reducer';
import reaction from 'app/entities/reaction/reaction.reducer';
import share from 'app/entities/share/share.reducer';
import mention from 'app/entities/mention/mention.reducer';
/* jhipster-needle-add-reducer-import - JHipster will add reducer here */

const entitiesReducers = {
  post,
  file,
  comment,
  favorite,
  follow,
  notification,
  tag,
  reaction,
  share,
  mention,
  /* jhipster-needle-add-reducer-combine - JHipster will add reducer here */
};

export default entitiesReducers;
