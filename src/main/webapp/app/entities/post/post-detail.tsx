import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './post.reducer';

export const PostDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const postEntity = useAppSelector(state => state.post.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="postDetailsHeading">
          <Translate contentKey="fileShareAppV1App.post.detail.title">Post</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{postEntity.id}</dd>
          <dt>
            <span id="content">
              <Translate contentKey="fileShareAppV1App.post.content">Content</Translate>
            </span>
          </dt>
          <dd>{postEntity.content}</dd>
          <dt>
            <span id="createdAt">
              <Translate contentKey="fileShareAppV1App.post.createdAt">Created At</Translate>
            </span>
          </dt>
          <dd>{postEntity.createdAt ? <TextFormat value={postEntity.createdAt} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="updatedAt">
              <Translate contentKey="fileShareAppV1App.post.updatedAt">Updated At</Translate>
            </span>
          </dt>
          <dd>{postEntity.updatedAt ? <TextFormat value={postEntity.updatedAt} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="locationName">
              <Translate contentKey="fileShareAppV1App.post.locationName">Location Name</Translate>
            </span>
          </dt>
          <dd>{postEntity.locationName}</dd>
          <dt>
            <span id="locationLat">
              <Translate contentKey="fileShareAppV1App.post.locationLat">Location Lat</Translate>
            </span>
          </dt>
          <dd>{postEntity.locationLat}</dd>
          <dt>
            <span id="locationLong">
              <Translate contentKey="fileShareAppV1App.post.locationLong">Location Long</Translate>
            </span>
          </dt>
          <dd>{postEntity.locationLong}</dd>
          <dt>
            <span id="privacy">
              <Translate contentKey="fileShareAppV1App.post.privacy">Privacy</Translate>
            </span>
          </dt>
          <dd>{postEntity.privacy}</dd>
          <dt>
            <span id="scheduledAt">
              <Translate contentKey="fileShareAppV1App.post.scheduledAt">Scheduled At</Translate>
            </span>
          </dt>
          <dd>{postEntity.scheduledAt ? <TextFormat value={postEntity.scheduledAt} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="viewCount">
              <Translate contentKey="fileShareAppV1App.post.viewCount">View Count</Translate>
            </span>
          </dt>
          <dd>{postEntity.viewCount}</dd>
          <dt>
            <span id="commentCount">
              <Translate contentKey="fileShareAppV1App.post.commentCount">Comment Count</Translate>
            </span>
          </dt>
          <dd>{postEntity.commentCount}</dd>
          <dt>
            <span id="shareCount">
              <Translate contentKey="fileShareAppV1App.post.shareCount">Share Count</Translate>
            </span>
          </dt>
          <dd>{postEntity.shareCount}</dd>
          <dt>
            <span id="reactionCount">
              <Translate contentKey="fileShareAppV1App.post.reactionCount">Reaction Count</Translate>
            </span>
          </dt>
          <dd>{postEntity.reactionCount}</dd>
          <dt>
            <Translate contentKey="fileShareAppV1App.post.user">User</Translate>
          </dt>
          <dd>{postEntity.user ? postEntity.user.login : ''}</dd>
          <dt>
            <Translate contentKey="fileShareAppV1App.post.tags">Tags</Translate>
          </dt>
          <dd>
            {postEntity.tags
              ? postEntity.tags.map((val, i) => (
                  <span key={val.id}>
                    <a>{val.name}</a>
                    {postEntity.tags && i === postEntity.tags.length - 1 ? '' : ', '}
                  </span>
                ))
              : null}
          </dd>
        </dl>
        <Button tag={Link} to="/post" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/post/${postEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default PostDetail;
