/*
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */

package org.opennms.horizon.server;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.leangen.graphql.execution.ResolutionEnvironment;
import io.leangen.graphql.spqr.spring.web.GraphQLExecutor;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.opennms.horizon.inventory.dto.GeoLocation;
import org.opennms.horizon.inventory.dto.MonitoringLocationDTO;
import org.opennms.horizon.server.service.grpc.InventoryClient;
import org.opennms.horizon.server.test.util.ResourceFileReader;
import org.opennms.horizon.server.utils.ServerHeaderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Slf4j
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = RestServerApplication.class)
public class GraphQLControllerTest {
    private static final String DESCRIPTIVE_DISPLAY_NAME = "[{index}]: {0}";

    private static final String ENDPOINT = "/graphql";
    private static final String ACCESS_TOKEN = "test-token-12345";

    private static final String QUERY = """
        mutation {
            createLocation(location: {
                location: "foo",
                address: "bar",
                latitude: 0.0
                longitude: 0.0
            }) {
                id
                location
                latitude
                longitude
                address
            }
        }
        """;
    private static final MonitoringLocationDTO RESPONSE_DTO = MonitoringLocationDTO.newBuilder()
        .setId(5L)
        .setLocation("foo")
        .setAddress("bar")
        .setGeoLocation(GeoLocation.newBuilder()
            .setLatitude(0)
            .setLongitude(0)
            .build())
        .build();

    @Autowired
    private WebTestClient webClient;

    @MockBean
    private InventoryClient inventoryClient;

    @SpyBean
    @SuppressWarnings("rawtypes")
    private GraphQLExecutor graphQLExecutor;

    @MockBean
    private ServerHeaderUtil mockHeaderUtil;

    @BeforeEach
    void setUp() {
        doReturn(ACCESS_TOKEN)
            .when(mockHeaderUtil)
            .getAuthHeader(any(ResolutionEnvironment.class));
    }

    @Test
    void doesNotAllowRequestsViaGet() {
        webClient.get()
            .uri(ENDPOINT + "?query={q}", Map.of("q", QUERY))
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + ACCESS_TOKEN)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.METHOD_NOT_ALLOWED)
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.error").isEqualTo("Method Not Allowed")
            .jsonPath("$.message").isEqualTo("Request method 'GET' is not supported.")
            .jsonPath("$.trace").doesNotExist();
    }

    @Test
    void allowsPostRequests() {
        when(inventoryClient.createLocation(any(), any())).thenReturn(RESPONSE_DTO);

        String body = createPayload();

        webClient.post()
            .uri(ENDPOINT)
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + ACCESS_TOKEN)
            .bodyValue(body)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody().json("""
                {
                  "data": {
                    "createLocation": {
                      "id": 5,
                      "location": "foo",
                      "address": "bar",
                      "longitude": 0.0,
                      "latitude": 0.0
                    }
                  }
                }
                """);
    }

    public static Stream<Arguments> invalidRequests() {
        return Stream.of(
            readArgumentPair("/test/data/error-mishandling.json")
        );
    }

    private static Arguments readArgumentPair(String filePath) {
        return Arguments.of(filePath, ResourceFileReader.read(filePath));
    }

    @MethodSource("invalidRequests")
    @ParameterizedTest(name = DESCRIPTIVE_DISPLAY_NAME)
    void wellFormedButInvalidRequestShouldReturn200WithErrorDetails(
        String description,
        String requestBody
    ) {
        webClient.post()
            .uri(ENDPOINT)
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + ACCESS_TOKEN)
            .bodyValue(requestBody)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.data").doesNotExist()
            .jsonPath("$.errors").isArray()
            .jsonPath("$.trace").doesNotExist();
    }

    public static Stream<Arguments> aliasOverloading() {
        return Stream.of(
            readArgumentPair("/test/data/alias-overloading.json")
        );
    }

    @MethodSource("aliasOverloading")
    @ParameterizedTest(name = DESCRIPTIVE_DISPLAY_NAME)
    void limitsAliasOverloading(
        String description,
        String requestBody
    ) {
        webClient.post()
            .uri(ENDPOINT)
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + ACCESS_TOKEN)
            .bodyValue(requestBody)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.data").doesNotExist()
            .jsonPath("$.errors").isArray()
            .jsonPath("$.errors[*].message").value(Matchers.everyItem(
                Matchers.matchesRegex("^Validation error.*: Alias '.+' is repeated too many times$")
            ))
            .jsonPath("$.trace").doesNotExist();
    }

    public static Stream<Arguments> directiveOverloading() {
        return Stream.of(
            readArgumentPair("/test/data/directive-overloading.json")
        );
    }


    @MethodSource("directiveOverloading")
    @ParameterizedTest(name = DESCRIPTIVE_DISPLAY_NAME)
    void limitsDirectiveOverloading(
        String description,
        String requestBody
    ) {
        webClient.post()
            .uri(ENDPOINT)
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + ACCESS_TOKEN)
            .bodyValue(requestBody)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.data").doesNotExist()
            .jsonPath("$.errors").isArray()
            .jsonPath("$.errors[*].message").value(Matchers.everyItem(
                Matchers.matchesRegex("^Validation error.*: Directive '.+' is repeated too many times$")
            ))
            .jsonPath("$.trace").doesNotExist();
    }

    public static Stream<Arguments> introspectionRequests() {
        return Stream.of(
            readArgumentPair("/test/data/introspection-query.json"),
            readArgumentPair("/test/data/introspection-circular.json")
        );
    }

    @MethodSource("introspectionRequests")
    @ParameterizedTest(name = DESCRIPTIVE_DISPLAY_NAME)
    void introspectionNotAllowedWhenDisabled(
        String description,
        String requestBody
    ) {
        webClient.post()
            .uri(ENDPOINT)
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + ACCESS_TOKEN)
            .bodyValue(requestBody)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.data").doesNotExist()
            .jsonPath("$.errors").isArray()
            .jsonPath("$.errors[*].message").value(Matchers.everyItem(
                Matchers.matchesRegex("^Validation error.*: Field '.+' in type '__.+' is undefined$")
            ))
            .jsonPath("$.trace").doesNotExist();
    }

    public static Stream<Arguments> fieldDuplicationRequests() {
        return Stream.of(
            readArgumentPair("/test/data/field-duplication.json")
        );
    }

    @MethodSource("fieldDuplicationRequests")
    @ParameterizedTest(name = DESCRIPTIVE_DISPLAY_NAME)
    void limitsFieldDuplication(
        String description,
        String requestBody
    ) {
        webClient.post()
            .uri(ENDPOINT)
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + ACCESS_TOKEN)
            .bodyValue(requestBody)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.data").doesNotExist()
            .jsonPath("$.errors").isArray()
            .jsonPath("$.errors[*].message").value(Matchers.everyItem(
                Matchers.matchesRegex("^Validation error.*: Field '.+' is repeated too many times$")
            ))
            .jsonPath("$.trace").doesNotExist();
    }

    @ValueSource(strings = {
        "NONSENSE", "{\"query\":"
    })
    @ParameterizedTest
    void invalidRequestsBodyShouldReturn400Error(String requestBody) {
        webClient.post()
            .uri(ENDPOINT)
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + ACCESS_TOKEN)
            .bodyValue(requestBody)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.error").isEqualTo("Bad Request")
            .jsonPath("$.message").isEqualTo("Failed to read HTTP message")
            .jsonPath("$.trace").doesNotExist();
    }

    @Test
    @SuppressWarnings("unchecked")
    void anUnexpectedWebLayerErrorDoesNotLeakInternalDetails() {
        doThrow(new RuntimeException("internal error details"))
            .when(graphQLExecutor).execute(any(), any());

        webClient.post()
            .uri(ENDPOINT)
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + ACCESS_TOKEN)
            .bodyValue(createPayload())
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.error").isEqualTo("Internal Server Error")
            .jsonPath("$.message").isEqualTo("Internal Server Error")
            .jsonPath("$.trace").doesNotExist();
    }

    @Test
    void anUnexpectedErrorDoesNotLeakInternalDetails() {
        when(inventoryClient.createLocation(any(), any()))
            .thenThrow(new RuntimeException("internal error details"));

        webClient.post()
            .uri(ENDPOINT)
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + ACCESS_TOKEN)
            .bodyValue(createPayload())
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.errors[0].message").isEqualTo(
                "Exception while fetching data (/createLocation) : Internal Error"
            )
            .jsonPath("$.errors[0].path[0]").isEqualTo("createLocation")
            .jsonPath("$.trace").doesNotExist();
    }

    @Test
    void anUnexpectedGRPCErrorDoesNotLeakInternalDetails() {
        when(inventoryClient.deleteLocation(anyLong(), anyString()))
            .thenThrow(new StatusRuntimeException(Status.DATA_LOSS));

        String query = """
            mutation {
                deleteLocation(id: 1)
            }
            """;

        webClient.post()
            .uri(ENDPOINT)
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + ACCESS_TOKEN)
            .bodyValue(createPayload(query))
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.errors[0].message").isEqualTo(
                "Exception while fetching data (/deleteLocation) : Internal Error"
            )
            .jsonPath("$.errors[0].path[0]").isEqualTo("deleteLocation")
            .jsonPath("$.trace").doesNotExist();
    }

    private String createPayload() {
        return createPayload(QUERY);
    }

    private String createPayload(String query) {
        try {
            return new JSONObject().put("query", query.replace("\n", "")).toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
