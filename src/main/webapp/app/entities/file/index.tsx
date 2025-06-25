import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import File from './file';
import FileDetail from './file-detail';
import FileUpdate from './file-update';
import FileDeleteDialog from './file-delete-dialog';

const FileRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<File />} />
    <Route path="new" element={<FileUpdate />} />
    <Route path=":id">
      <Route index element={<FileDetail />} />
      <Route path="edit" element={<FileUpdate />} />
      <Route path="delete" element={<FileDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default FileRoutes;
