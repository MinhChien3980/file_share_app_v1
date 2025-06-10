import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './share.reducer';

export const ShareDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const shareEntity = useAppSelector(state => state.share.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="shareDetailsHeading">
          <Translate contentKey="fileShareAppV1App.share.detail.title">Share</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{shareEntity.id}</dd>
          <dt>
            <span id="createdAt">
              <Translate contentKey="fileShareAppV1App.share.createdAt">Created At</Translate>
            </span>
          </dt>
          <dd>{shareEntity.createdAt ? <TextFormat value={shareEntity.createdAt} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <Translate contentKey="fileShareAppV1App.share.post">Post</Translate>
          </dt>
          <dd>{shareEntity.post ? shareEntity.post.id : ''}</dd>
          <dt>
            <Translate contentKey="fileShareAppV1App.share.user">User</Translate>
          </dt>
          <dd>{shareEntity.user ? shareEntity.user.login : ''}</dd>
        </dl>
        <Button tag={Link} to="/share" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/share/${shareEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default ShareDetail;
