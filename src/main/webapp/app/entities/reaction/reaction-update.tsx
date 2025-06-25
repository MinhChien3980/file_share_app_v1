import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities as getPosts } from 'app/entities/post/post.reducer';
import { getUsers } from 'app/modules/administration/user-management/user-management.reducer';
import { ReactionType } from 'app/shared/model/enumerations/reaction-type.model';
import { createEntity, getEntity, reset, updateEntity } from './reaction.reducer';

export const ReactionUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const posts = useAppSelector(state => state.post.entities);
  const users = useAppSelector(state => state.userManagement.users);
  const reactionEntity = useAppSelector(state => state.reaction.entity);
  const loading = useAppSelector(state => state.reaction.loading);
  const updating = useAppSelector(state => state.reaction.updating);
  const updateSuccess = useAppSelector(state => state.reaction.updateSuccess);
  const reactionTypeValues = Object.keys(ReactionType);

  const handleClose = () => {
    navigate(`/reaction${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getPosts({}));
    dispatch(getUsers({}));
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
    values.reactedAt = convertDateTimeToServer(values.reactedAt);

    const entity = {
      ...reactionEntity,
      ...values,
      post: posts.find(it => it.id.toString() === values.post?.toString()),
      user: users.find(it => it.id.toString() === values.user?.toString()),
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
          reactedAt: displayDefaultDateTime(),
        }
      : {
          type: 'LIKE',
          ...reactionEntity,
          reactedAt: convertDateTimeFromServer(reactionEntity.reactedAt),
          post: reactionEntity?.post?.id,
          user: reactionEntity?.user?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="fileShareAppV1App.reaction.home.createOrEditLabel" data-cy="ReactionCreateUpdateHeading">
            <Translate contentKey="fileShareAppV1App.reaction.home.createOrEditLabel">Create or edit a Reaction</Translate>
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
                  id="reaction-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('fileShareAppV1App.reaction.type')}
                id="reaction-type"
                name="type"
                data-cy="type"
                type="select"
              >
                {reactionTypeValues.map(reactionType => (
                  <option value={reactionType} key={reactionType}>
                    {translate(`fileShareAppV1App.ReactionType.${reactionType}`)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label={translate('fileShareAppV1App.reaction.reactedAt')}
                id="reaction-reactedAt"
                name="reactedAt"
                data-cy="reactedAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                id="reaction-post"
                name="post"
                data-cy="post"
                label={translate('fileShareAppV1App.reaction.post')}
                type="select"
              >
                <option value="" key="0" />
                {posts
                  ? posts.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <ValidatedField
                id="reaction-user"
                name="user"
                data-cy="user"
                label={translate('fileShareAppV1App.reaction.user')}
                type="select"
              >
                <option value="" key="0" />
                {users
                  ? users.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.login}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/reaction" replace color="info">
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

export default ReactionUpdate;
