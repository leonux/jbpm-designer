package org.jbpm.designer.assets.services;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jbpm.designer.assets.domain.AssetXml;
import org.jbpm.designer.assets.persistence.AssetEntity;


/**
 */
@Path("/asset")
public interface AssetResource
{
    
   @POST
   @Consumes("application/xml")
   Response createAsset(AssetXml asset, @Context UriInfo uriInfo);

   @GET
   @Path("{id}")
   @Produces("application/xml")
   AssetXml getAsset(@PathParam("id") int id);
   
}
