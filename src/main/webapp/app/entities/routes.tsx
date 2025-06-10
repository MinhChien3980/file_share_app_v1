import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Post from './post';
import File from './file';
import Comment from './comment';
import Favorite from './favorite';
import Follow from './follow';
import Notification from './notification';
import Tag from './tag';
import Reaction from './reaction';
import Share from './share';
import Mention from './mention';
/* jhipster-needle-add-route-import - JHipster will add routes here */

export default () => {
  return (
    <div>
      <ErrorBoundaryRoutes>
        {/* prettier-ignore */}
        <Route path="post/*" element={<Post />} />
        <Route path="file/*" element={<File />} />
        <Route path="comment/*" element={<Comment />} />
        <Route path="favorite/*" element={<Favorite />} />
        <Route path="follow/*" element={<Follow />} />
        <Route path="notification/*" element={<Notification />} />
        <Route path="tag/*" element={<Tag />} />
        <Route path="reaction/*" element={<Reaction />} />
        <Route path="share/*" element={<Share />} />
        <Route path="mention/*" element={<Mention />} />
        {/* jhipster-needle-add-route-path - JHipster will add routes here */}
      </ErrorBoundaryRoutes>
    </div>
  );
};
