import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './reaction.reducer';

export const ReactionDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const reactionEntity = useAppSelector(state => state.reaction.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="reactionDetailsHeading">
          <Translate contentKey="fileShareAppV1App.reaction.detail.title">Reaction</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{reactionEntity.id}</dd>
          <dt>
            <span id="type">
              <Translate contentKey="fileShareAppV1App.reaction.type">Type</Translate>
            </span>
          </dt>
          <dd>{reactionEntity.type}</dd>
          <dt>
            <span id="reactedAt">
              <Translate contentKey="fileShareAppV1App.reaction.reactedAt">Reacted At</Translate>
            </span>
          </dt>
          <dd>{reactionEntity.reactedAt ? <TextFormat value={reactionEntity.reactedAt} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <Translate contentKey="fileShareAppV1App.reaction.post">Post</Translate>
          </dt>
          <dd>{reactionEntity.post ? reactionEntity.post.id : ''}</dd>
          <dt>
            <Translate contentKey="fileShareAppV1App.reaction.user">User</Translate>
          </dt>
          <dd>{reactionEntity.user ? reactionEntity.user.login : ''}</dd>
        </dl>
        <Button tag={Link} to="/reaction" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/reaction/${reactionEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default ReactionDetail;
