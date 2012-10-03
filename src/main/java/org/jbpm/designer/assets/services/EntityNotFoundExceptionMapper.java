package org.jbpm.designer.assets.services;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class EntityNotFoundExceptionMapper implements ExceptionMapper<EntityNotFoundException>
{
   public Response toResponse(EntityNotFoundException exception)
   {
      return Response.status(Response.Status.NOT_FOUND).build();
   }
}
