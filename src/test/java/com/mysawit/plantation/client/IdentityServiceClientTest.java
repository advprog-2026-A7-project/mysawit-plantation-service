package com.mysawit.plantation.client;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class IdentityServiceClientTest {

    @Test
    void getUserNameReturnsNullForBlankUserId() {
        IdentityServiceClient client = new IdentityServiceClient("http://identity", "secret");

        assertNull(client.getUserName(" "));
    }

    @Test
    void getUserNameReturnsTrimmedNameAndSendsInternalApiKey() {
        IdentityServiceClient client = new IdentityServiceClient("http://identity", "secret");
        MockRestServiceServer server = bindServer(client);
        server.expect(requestTo("http://identity/api/internal/users/user-1"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Internal-Api-Key", "secret"))
                .andRespond(withSuccess("{\"name\":\" Alice \"}", MediaType.APPLICATION_JSON));

        assertEquals("Alice", client.getUserName("user-1"));
        server.verify();
    }

    @Test
    void getUserNameReturnsNullWhenResponseHasNoName() {
        IdentityServiceClient client = new IdentityServiceClient("http://identity", "");
        MockRestServiceServer server = bindServer(client);
        server.expect(requestTo("http://identity/api/internal/users/user-1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertNull(client.getUserName("user-1"));
        server.verify();
    }

    @Test
    void getUserNameReturnsNullWhenIdentityServiceFails() {
        IdentityServiceClient client = new IdentityServiceClient("http://identity", "");
        MockRestServiceServer server = bindServer(client);
        server.expect(requestTo("http://identity/api/internal/users/user-1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        assertNull(client.getUserName("user-1"));
        server.verify();
    }

    private MockRestServiceServer bindServer(IdentityServiceClient client) {
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(client, "restTemplate");
        return MockRestServiceServer.bindTo(restTemplate).build();
    }
}
