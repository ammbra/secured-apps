package org.acme.example;


import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import java.util.List;


@Path("api/backend")
@RegisterRestClient(configKey = "todo-http")
public interface ToDoService {

	@GET
	@Produces("application/json")
	List<Todo> list();

	@PUT
    @Path("/{id}")
    @Transactional
    public Todo update(@PathParam("id") String id, Todo todo);

	@POST
	@Transactional
	public Todo create(Todo todo);

	@DELETE
	@Path("/{id}")
	@Transactional
	public Response deleteById(@PathParam("id") String id);

	@DELETE
	@Transactional
	public Response deleteCompleted();

}