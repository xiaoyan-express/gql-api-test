package com.xyz;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;

public class SpacexGqlApiTests {
    private final static String spacexGqlUrl = "https://api.spacex.land/graphql";
    private static RequestSpecification reqSpecification;

    private final static String newLine = "\\n"; //System.getProperty("line.separator");
    private final static String queryTpl = "{\"query\":\"%s\",\"variables\":{}}";

    private final static String listUsersQueryTpl = String.join(
            newLine,
            "query {",
            "   users(limit: %d) {",
            "       id",
            "       name",
            "   }",
            "}"
    );

    private final static String createUserMutationTpl = String.join(
            newLine,
            "mutation {",
            "   insert_users(",
            "       objects: [",
            "           {",
            "               id: \\\"%s\\\"",
            "               name: \\\"%s\\\"",
            "           }",
            "       ]",
            "   )",
            "   {",
            "       affected_rows",
            "       returning {",
            "           id",
            "           name",
            "       }",
            "   }",
            "}"
    );

    private final static String deleteUserMutationTpl = String.join(
            newLine,
            "mutation {",
            "   delete_users(",
            "       where: {",
            "           id: {",
            "               _eq: \\\"%s\\\"",
            "           }",
            "       }",
            "   )",
            "   {",
            "       affected_rows",
            "       returning {",
            "           id",
            "           name",
            "       }",
            "   }",
            "}"
    );

    @BeforeClass
    public static void createReqSpec() {
        reqSpecification = new RequestSpecBuilder()
                .setBasePath("https://api.spacex.land")
                .setPort(443)
                .build();
    }

    @Test
    public void listUsers() {
        String listUsersQuery = String.format(listUsersQueryTpl, 2);
        String listUsersQueryString = String.format(queryTpl, listUsersQuery);
        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
        .when()
                .body(listUsersQueryString)
                .port(443)
                .post(spacexGqlUrl)
        .then()
                .statusCode(200)
                .log().all();
    }

    @Test
    public void insertAndDeleteUser() {
        String userId = UUID.randomUUID().toString();
        String userName = userId + "test";

        // Create a user
        String createUserMutation = String.format(createUserMutationTpl, userId, userName);
        String createUserMutationString = String.format(queryTpl, createUserMutation);

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
        .when()
                .body(createUserMutationString)
                .port(443)
                .post(spacexGqlUrl)
        .then()
                .statusCode(200)
                .log().all();

        // Delete the created user
        String deleteUserMutation = String.format(deleteUserMutationTpl, userId);
        String deleteUserMutationString = String.format(queryTpl, deleteUserMutation);
        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
        .when()
                .body(deleteUserMutationString)
                .port(443)
                .post(spacexGqlUrl)
        .then()
                .statusCode(200)
                .log().all();
    }
}
