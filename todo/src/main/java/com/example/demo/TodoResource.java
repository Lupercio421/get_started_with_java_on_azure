package com.example.demo;

import org.jboss.logging.Logger;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/api/todos")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class TodoResource {

    @Inject
    Logger logger;

    @POST
    @Transactional
    public Response createTodo(Todo todo, @Context UriInfo uriInfo) {
        logger.info("Creating todo: " + todo);
        Todo.persist(todo);
        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(todo.id.toString());
        return Response.created(uriBuilder.build()).entity(todo).build();
    }

    @GET
    public List<Todo> getTodos() {
        logger.info("Getting all todos");
        return Todo.listAll();
    }
}
