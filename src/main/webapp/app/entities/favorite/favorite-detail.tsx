import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './favorite.reducer';

export const FavoriteDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const favoriteEntity = useAppSelector(state => state.favorite.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="favoriteDetailsHeading">
          <Translate contentKey="fileShareAppV1App.favorite.detail.title">Favorite</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{favoriteEntity.id}</dd>
          <dt>
            <span id="savedAt">
              <Translate contentKey="fileShareAppV1App.favorite.savedAt">Saved At</Translate>
            </span>
          </dt>
          <dd>{favoriteEntity.savedAt ? <TextFormat value={favoriteEntity.savedAt} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <Translate contentKey="fileShareAppV1App.favorite.post">Post</Translate>
          </dt>
          <dd>{favoriteEntity.post ? favoriteEntity.post.id : ''}</dd>
          <dt>
            <Translate contentKey="fileShareAppV1App.favorite.user">User</Translate>
          </dt>
          <dd>{favoriteEntity.user ? favoriteEntity.user.login : ''}</dd>
        </dl>
        <Button tag={Link} to="/favorite" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/favorite/${favoriteEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default FavoriteDetail;
