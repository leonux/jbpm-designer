package org.jbpm.designer.assets.services;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.jbpm.designer.assets.domain.AssetXml;
import org.jbpm.designer.assets.persistence.AssetEntity;

@Stateless
public class AssetResourceBean implements AssetResource {

    @PersistenceContext(name="org.jbpm.designer")
    private EntityManager em;

    public Response createAsset(AssetXml asset, UriInfo uriInfo) {
        AssetEntity entity = domain2entity(asset);
        em.persist(entity);
        em.flush();

        System.out.println("Created customer " + entity.getId());
        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        builder.path(Long.toString(entity.getId()));
        return Response.created(builder.build()).build();

    }

    public AssetXml getAsset(int id) {
        AssetEntity customer = em.getReference(AssetEntity.class, id);
        return entity2domain(customer);
    }

    public static AssetEntity domain2entity(AssetXml customer) {
        AssetEntity entity = new AssetEntity();
        entity.setId(customer.getId());
        return entity;
    }

    public static AssetXml entity2domain(AssetEntity entity) {
        AssetXml cust = new AssetXml();
        cust.setId(entity.getId());
        
        return cust;
    }

}
