import axios from 'axios';
import { createAsyncThunk, isFulfilled, isPending } from '@reduxjs/toolkit';
import { cleanEntity } from 'app/shared/util/entity-utils';
import { EntityState, IQueryParams, createEntitySlice, serializeAxiosError } from 'app/shared/reducers/reducer.utils';
import { ITag, defaultValue } from 'app/shared/model/tag.model';

const initialState: EntityState<ITag> = {
  loading: false,
  errorMessage: null,
  entities: [],
  entity: defaultValue,
  updating: false,
  totalItems: 0,
  updateSuccess: false,
};

const apiUrl = 'api/tags';
const apiSearchUrl = 'api/tags/_search';

// Actions

export const searchEntities = createAsyncThunk(
  'tag/search_entity',
  async ({ query, page, size, sort }: IQueryParams) => {
    const requestUrl = `${apiSearchUrl}?query=${query}${sort ? `&page=${page}&size=${size}&sort=${sort}` : ''}`;
    return axios.get<ITag[]>(requestUrl);
  },
  { serializeError: serializeAxiosError },
);

export const getEntities = createAsyncThunk(
  'tag/fetch_entity_list',
  async ({ page, size, sort }: IQueryParams) => {
    const requestUrl = `${apiUrl}?${sort ? `page=${page}&size=${size}&sort=${sort}&` : ''}cacheBuster=${new Date().getTime()}`;
    return axios.get<ITag[]>(requestUrl);
  },
  { serializeError: serializeAxiosError },
);

export const getEntity = createAsyncThunk(
  'tag/fetch_entity',
  async (id: string | number) => {
    const requestUrl = `${apiUrl}/${id}`;
    return axios.get<ITag>(requestUrl);
  },
  { serializeError: serializeAxiosError },
);

export const createEntity = createAsyncThunk(
  'tag/create_entity',
  async (entity: ITag, thunkAPI) => {
    const result = await axios.post<ITag>(apiUrl, cleanEntity(entity));
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError },
);

export const updateEntity = createAsyncThunk(
  'tag/update_entity',
  async (entity: ITag, thunkAPI) => {
    const result = await axios.put<ITag>(`${apiUrl}/${entity.id}`, cleanEntity(entity));
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError },
);

export const partialUpdateEntity = createAsyncThunk(
  'tag/partial_update_entity',
  async (entity: ITag, thunkAPI) => {
    const result = await axios.patch<ITag>(`${apiUrl}/${entity.id}`, cleanEntity(entity));
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError },
);

export const deleteEntity = createAsyncThunk(
  'tag/delete_entity',
  async (id: string | number, thunkAPI) => {
    const requestUrl = `${apiUrl}/${id}`;
    const result = await axios.delete<ITag>(requestUrl);
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError },
);

// slice

export const TagSlice = createEntitySlice({
  name: 'tag',
  initialState,
  extraReducers(builder) {
    builder
      .addCase(getEntity.fulfilled, (state, action) => {
        state.loading = false;
        state.entity = action.payload.data;
      })
      .addCase(deleteEntity.fulfilled, state => {
        state.updating = false;
        state.updateSuccess = true;
        state.entity = {};
      })
      .addMatcher(isFulfilled(getEntities, searchEntities), (state, action) => {
        const { data, headers } = action.payload;

        return {
          ...state,
          loading: false,
          entities: data,
          totalItems: parseInt(headers['x-total-count'], 10),
        };
      })
      .addMatcher(isFulfilled(createEntity, updateEntity, partialUpdateEntity), (state, action) => {
        state.updating = false;
        state.loading = false;
        state.updateSuccess = true;
        state.entity = action.payload.data;
      })
      .addMatcher(isPending(getEntities, getEntity, searchEntities), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.loading = true;
      })
      .addMatcher(isPending(createEntity, updateEntity, partialUpdateEntity, deleteEntity), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.updating = true;
      });
  },
});

export const { reset } = TagSlice.actions;

// Reducer
export default TagSlice.reducer;
