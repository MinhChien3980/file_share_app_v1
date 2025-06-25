import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Share from './share';
import ShareDetail from './share-detail';
import ShareUpdate from './share-update';
import ShareDeleteDialog from './share-delete-dialog';

const ShareRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Share />} />
    <Route path="new" element={<ShareUpdate />} />
    <Route path=":id">
      <Route index element={<ShareDetail />} />
      <Route path="edit" element={<ShareUpdate />} />
      <Route path="delete" element={<ShareDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default ShareRoutes;
