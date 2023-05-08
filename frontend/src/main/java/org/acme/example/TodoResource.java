package org.acme.example;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.List;


@Path("/api")
@Produces("application/json")
@Consumes("application/json")
public class TodoResource {


    @Context
    ContainerRequestContext requestContext;

    @ConfigProperty(name = "quarkus.rest-client.todo-http.url")
    URL serverURL;

    @ConfigProperty(name = "quarkus.http.ssl.certificate.key-store-file")
    String keyStoreFile;

    @ConfigProperty(name = "quarkus.http.ssl.certificate.key-store-password")
    String keyStoreFilePassword;

    @ConfigProperty(name = "quarkus.http.ssl.certificate.trust-store-file")
    String trustStoreFile;

    @ConfigProperty(name = "quarkus.http.ssl.certificate.trust-store-password")
    String trustStoreFilePassword;

    ToDoService service;


    @PostConstruct
    void init() throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        InputStream inputStreamKeyStore = this.getClass()
                .getClassLoader()
                .getResourceAsStream(keyStoreFile);
        keyStore.load(inputStreamKeyStore, keyStoreFilePassword.toCharArray());
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        InputStream inputStreamTrustStore = this.getClass()
                .getClassLoader()
                .getResourceAsStream(trustStoreFile);
        trustStore.load(inputStreamTrustStore, trustStoreFilePassword.toCharArray());

        this.service =  RestClientBuilder.newBuilder()
                .baseUrl(serverURL)
                .keyStore(keyStore, keyStoreFilePassword)
                .trustStore(keyStore)
                .register(BasicClientRequestFilter.class)
                .build(ToDoService.class);

    }

    @OPTIONS
    public Response opt() {
        return Response.ok().build();
    }

    @GET
    public List<Todo> getAll() {
       return service.list();
    }

    @POST
    public Response create(Todo item) {
        service.create(item);
        return Response.status(Status.CREATED).entity(item).build();
    }

    @PATCH
    @Path("/{id}")
    public Response update(@PathParam("id") String id, Todo todo) {
        Todo item = service.update(id, todo);
        return Response.ok(item).build();
    }

    @DELETE
    @Transactional
    public Response deleteCompleted() {
        service.deleteCompleted();
        return Response.noContent().build();
    }

    @DELETE
    @Transactional
    @Path("/{id}")
    public Response deleteOne(@PathParam("id") String id) {
        service.deleteById(id);
        return Response.noContent().build();
    }

}