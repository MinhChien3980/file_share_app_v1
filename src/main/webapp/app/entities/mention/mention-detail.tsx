import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './mention.reducer';

export const MentionDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const mentionEntity = useAppSelector(state => state.mention.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="mentionDetailsHeading">
          <Translate contentKey="fileShareAppV1App.mention.detail.title">Mention</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{mentionEntity.id}</dd>
          <dt>
            <Translate contentKey="fileShareAppV1App.mention.post">Post</Translate>
          </dt>
          <dd>{mentionEntity.post ? mentionEntity.post.id : ''}</dd>
          <dt>
            <Translate contentKey="fileShareAppV1App.mention.user">User</Translate>
          </dt>
          <dd>{mentionEntity.user ? mentionEntity.user.login : ''}</dd>
        </dl>
        <Button tag={Link} to="/mention" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/mention/${mentionEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default MentionDetail;
