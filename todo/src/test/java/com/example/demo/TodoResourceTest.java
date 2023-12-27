package com.example.demo;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@QuarkusTest
class TodoResourceTest {

    @Test
    void shouldGetAllTodos() {
        given()
                .when().get("/api/todos")
                .then()
                .statusCode(200);
    }

    @Test
    void shouldCreateATodo(){
        Todo todo = new Todo();
        todo.description = "Take Quarkus MS Learn";
        todo.details = "Take the MS Learn on deploying Quarkus to Azure Container Apps";
        todo.done = true;

        given().body(todo)
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .when().post("/api/todos")
                .then()
                .statusCode(201);
    }

//    @Test
//    void testHelloEndpoint() {
//        given()
//          .when().get("/C:/Program Files/Git/api/todos")
//          .then()
//             .statusCode(200)
//             .body(is("Hello RESTEasy"));
//    }

}