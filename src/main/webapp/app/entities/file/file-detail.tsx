import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './file.reducer';

export const FileDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const fileEntity = useAppSelector(state => state.file.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="fileDetailsHeading">
          <Translate contentKey="fileShareAppV1App.file.detail.title">File</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{fileEntity.id}</dd>
          <dt>
            <span id="fileName">
              <Translate contentKey="fileShareAppV1App.file.fileName">File Name</Translate>
            </span>
          </dt>
          <dd>{fileEntity.fileName}</dd>
          <dt>
            <span id="fileUrl">
              <Translate contentKey="fileShareAppV1App.file.fileUrl">File Url</Translate>
            </span>
          </dt>
          <dd>{fileEntity.fileUrl}</dd>
          <dt>
            <span id="mimeType">
              <Translate contentKey="fileShareAppV1App.file.mimeType">Mime Type</Translate>
            </span>
          </dt>
          <dd>{fileEntity.mimeType}</dd>
          <dt>
            <span id="fileSize">
              <Translate contentKey="fileShareAppV1App.file.fileSize">File Size</Translate>
            </span>
          </dt>
          <dd>{fileEntity.fileSize}</dd>
          <dt>
            <span id="uploadedAt">
              <Translate contentKey="fileShareAppV1App.file.uploadedAt">Uploaded At</Translate>
            </span>
          </dt>
          <dd>{fileEntity.uploadedAt ? <TextFormat value={fileEntity.uploadedAt} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <Translate contentKey="fileShareAppV1App.file.post">Post</Translate>
          </dt>
          <dd>{fileEntity.post ? fileEntity.post.id : ''}</dd>
        </dl>
        <Button tag={Link} to="/file" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/file/${fileEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default FileDetail;
