import React from 'react';
import { Translate } from 'react-jhipster';

import MenuItem from 'app/shared/layout/menus/menu-item';

const EntitiesMenu = () => {
  return (
    <>
      {/* prettier-ignore */}
      <MenuItem icon="asterisk" to="/post">
        <Translate contentKey="global.menu.entities.post" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/file">
        <Translate contentKey="global.menu.entities.file" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/comment">
        <Translate contentKey="global.menu.entities.comment" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/favorite">
        <Translate contentKey="global.menu.entities.favorite" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/follow">
        <Translate contentKey="global.menu.entities.follow" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/notification">
        <Translate contentKey="global.menu.entities.notification" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/tag">
        <Translate contentKey="global.menu.entities.tag" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/reaction">
        <Translate contentKey="global.menu.entities.reaction" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/share">
        <Translate contentKey="global.menu.entities.share" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/mention">
        <Translate contentKey="global.menu.entities.mention" />
      </MenuItem>
      {/* jhipster-needle-add-entity-to-menu - JHipster will add entities to the menu here */}
    </>
  );
};

export default EntitiesMenu;
