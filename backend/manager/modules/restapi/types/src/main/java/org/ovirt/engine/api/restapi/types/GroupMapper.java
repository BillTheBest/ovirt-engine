package org.ovirt.engine.api.restapi.types;


import org.ovirt.engine.api.model.Domain;
import org.ovirt.engine.api.model.Group;

import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.ad_groups;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.StringHelper;

public class GroupMapper {

    @Mapping(from = ad_groups.class, to = Group.class)
    public static Group map(ad_groups entity, Group template) {
        Group model = template != null ? template : new Group();
        model.setName(entity.getname());
        model.setId(entity.getid().toString());
        if(!StringHelper.isNullOrEmpty(entity.getdomain())){
            Domain dom = new Domain();
            dom.setName(entity.getdomain());
            dom.setId(new NGuid(entity.getdomain().getBytes(), true).toString());
            model.setDomain(dom);
        }
        return model;
    }

    @Mapping(from = DbUser.class, to = Group.class)
    public static Group map(DbUser entity, Group template) {
        Group model = template != null ? template : new Group();
        model.setName(entity.getname());
        model.setId(entity.getuser_id().toString());
        if(!StringHelper.isNullOrEmpty(entity.getdomain())){
            Domain dom = new Domain();
            dom.setName(entity.getdomain());
            dom.setId(new NGuid(entity.getdomain().getBytes(), true).toString());
            model.setDomain(dom);
        }
        return model;
    }
}
