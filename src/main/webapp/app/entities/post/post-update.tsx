import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, isNumber, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getUsers } from 'app/modules/administration/user-management/user-management.reducer';
import { getEntities as getTags } from 'app/entities/tag/tag.reducer';
import { Privacy } from 'app/shared/model/enumerations/privacy.model';
import { createEntity, getEntity, reset, updateEntity } from './post.reducer';

export const PostUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const users = useAppSelector(state => state.userManagement.users);
  const tags = useAppSelector(state => state.tag.entities);
  const postEntity = useAppSelector(state => state.post.entity);
  const loading = useAppSelector(state => state.post.loading);
  const updating = useAppSelector(state => state.post.updating);
  const updateSuccess = useAppSelector(state => state.post.updateSuccess);
  const privacyValues = Object.keys(Privacy);

  const handleClose = () => {
    navigate(`/post${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getUsers({}));
    dispatch(getTags({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    if (values.id !== undefined && typeof values.id !== 'number') {
      values.id = Number(values.id);
    }
    values.createdAt = convertDateTimeToServer(values.createdAt);
    values.updatedAt = convertDateTimeToServer(values.updatedAt);
    if (values.locationLat !== undefined && typeof values.locationLat !== 'number') {
      values.locationLat = Number(values.locationLat);
    }
    if (values.locationLong !== undefined && typeof values.locationLong !== 'number') {
      values.locationLong = Number(values.locationLong);
    }
    values.scheduledAt = convertDateTimeToServer(values.scheduledAt);
    if (values.viewCount !== undefined && typeof values.viewCount !== 'number') {
      values.viewCount = Number(values.viewCount);
    }
    if (values.commentCount !== undefined && typeof values.commentCount !== 'number') {
      values.commentCount = Number(values.commentCount);
    }
    if (values.shareCount !== undefined && typeof values.shareCount !== 'number') {
      values.shareCount = Number(values.shareCount);
    }
    if (values.reactionCount !== undefined && typeof values.reactionCount !== 'number') {
      values.reactionCount = Number(values.reactionCount);
    }

    const entity = {
      ...postEntity,
      ...values,
      user: users.find(it => it.id.toString() === values.user?.toString()),
      tags: mapIdList(values.tags),
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {
          createdAt: displayDefaultDateTime(),
          updatedAt: displayDefaultDateTime(),
          scheduledAt: displayDefaultDateTime(),
        }
      : {
          privacy: 'PUBLIC',
          ...postEntity,
          createdAt: convertDateTimeFromServer(postEntity.createdAt),
          updatedAt: convertDateTimeFromServer(postEntity.updatedAt),
          scheduledAt: convertDateTimeFromServer(postEntity.scheduledAt),
          user: postEntity?.user?.id,
          tags: postEntity?.tags?.map(e => e.id.toString()),
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="fileShareAppV1App.post.home.createOrEditLabel" data-cy="PostCreateUpdateHeading">
            <Translate contentKey="fileShareAppV1App.post.home.createOrEditLabel">Create or edit a Post</Translate>
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew ? (
                <ValidatedField
                  name="id"
                  required
                  readOnly
                  id="post-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('fileShareAppV1App.post.content')}
                id="post-content"
                name="content"
                data-cy="content"
                type="textarea"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('fileShareAppV1App.post.createdAt')}
                id="post-createdAt"
                name="createdAt"
                data-cy="createdAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('fileShareAppV1App.post.updatedAt')}
                id="post-updatedAt"
                name="updatedAt"
                data-cy="updatedAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('fileShareAppV1App.post.locationName')}
                id="post-locationName"
                name="locationName"
                data-cy="locationName"
                type="text"
              />
              <ValidatedField
                label={translate('fileShareAppV1App.post.locationLat')}
                id="post-locationLat"
                name="locationLat"
                data-cy="locationLat"
                type="text"
                validate={{
                  max: { value: 90, message: translate('entity.validation.max', { max: 90 }) },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <ValidatedField
                label={translate('fileShareAppV1App.post.locationLong')}
                id="post-locationLong"
                name="locationLong"
                data-cy="locationLong"
                type="text"
                validate={{
                  max: { value: 180, message: translate('entity.validation.max', { max: 180 }) },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <ValidatedField
                label={translate('fileShareAppV1App.post.privacy')}
                id="post-privacy"
                name="privacy"
                data-cy="privacy"
                type="select"
              >
                {privacyValues.map(privacy => (
                  <option value={privacy} key={privacy}>
                    {translate(`fileShareAppV1App.Privacy.${privacy}`)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label={translate('fileShareAppV1App.post.scheduledAt')}
                id="post-scheduledAt"
                name="scheduledAt"
                data-cy="scheduledAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField
                label={translate('fileShareAppV1App.post.viewCount')}
                id="post-viewCount"
                name="viewCount"
                data-cy="viewCount"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <ValidatedField
                label={translate('fileShareAppV1App.post.commentCount')}
                id="post-commentCount"
                name="commentCount"
                data-cy="commentCount"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <ValidatedField
                label={translate('fileShareAppV1App.post.shareCount')}
                id="post-shareCount"
                name="shareCount"
                data-cy="shareCount"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <ValidatedField
                label={translate('fileShareAppV1App.post.reactionCount')}
                id="post-reactionCount"
                name="reactionCount"
                data-cy="reactionCount"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  validate: v => isNumber(v) || translate('entity.validation.number'),
                }}
              />
              <ValidatedField id="post-user" name="user" data-cy="user" label={translate('fileShareAppV1App.post.user')} type="select">
                <option value="" key="0" />
                {users
                  ? users.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.login}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <ValidatedField
                label={translate('fileShareAppV1App.post.tags')}
                id="post-tags"
                data-cy="tags"
                type="select"
                multiple
                name="tags"
              >
                <option value="" key="0" />
                {tags
                  ? tags.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.name}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/post" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">
                  <Translate contentKey="entity.action.back">Back</Translate>
                </span>
              </Button>
              &nbsp;
              <Button color="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp;
                <Translate contentKey="entity.action.save">Save</Translate>
              </Button>
            </ValidatedForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default PostUpdate;
