package it

import com.fasterxml.jackson.databind.ObjectMapper
import com.personal.Application
import dasniko.testcontainers.keycloak.KeycloakContainer
import it.infra.config.IntegrationTestConfig
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.wait.strategy.Wait

@SpringBootTest(
    classes = [Application::class, IntegrationTestConfig::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("Integration")
class IntegrationTestBase {
    companion object {
        const val CLIENT_ID = "client-1"
        const val CLIENT_SECRET = "secret"

        val mongoContainer = SharedMongoContainer.instance
        val keycloakContainer = SharedKeycloakContainer.instance

        @JvmStatic
        @DynamicPropertySource
        fun registerKeycloakProps(registry: DynamicPropertyRegistry) {
            registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri") {
                "${keycloakContainer.authServerUrl}/realms/task"
            }

            keycloakContainer.start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun registerMongoProps(registry: DynamicPropertyRegistry) {
            mongoContainer.start()
            val uri = mongoContainer.replicaSetUrl
            val appDbUri = if (uri.endsWith("/test")) uri.replace("/test", "/appdb") else "$uri/appdb"
            registry.add("spring.data.mongodb.uri") { appDbUri }
        }
    }

    @BeforeAll
    fun sanityCheck() {
        check(mongoContainer.isRunning) { "MongoDB container failed to start" }
        check(keycloakContainer.isRunning) { "Keycloak container failed to start" }
    }

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    @Autowired
    protected lateinit var restTemplate: RestTemplate

    @Autowired
    lateinit var mongoTemplate: MongoTemplate

    /**
     * Obtain a client-credentials token from Keycloak, using:
     *   client_id     = "client-1"
     *   client_secret = "secret"
     *
     * @return a valid Bearer token (String) that we can pass to MockMvc calls.
     */
    protected fun obtainAccessToken(): String {
        val tokenUri = "${keycloakContainer.authServerUrl}/realms/task/protocol/openid-connect/token"

        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_FORM_URLENCODED }
        val params = LinkedMultiValueMap<String, String>()
        params.add("grant_type", "client_credentials")
        params.add("client_id", CLIENT_ID)
        params.add("client_secret", CLIENT_SECRET)

        val request = HttpEntity(params, headers)
        val response = restTemplate.postForEntity(tokenUri, request, Map::class.java)

        @Suppress("UNCHECKED_CAST")
        val body = response.body as Map<String, Any>
        return body["access_token"] as String
    }
}

object Network {
    val instance: Network = Network.newNetwork()
}

object SharedKeycloakContainer {
    val instance: KeycloakContainer =
        KeycloakContainer("quay.io/keycloak/keycloak:26.2.4")
            .apply {
                withRealmImportFile("task-realm.json")
                withAdminUsername("admin")
                withAdminPassword("admin")
                withNetwork(it.Network.instance)
                withEnv("KC_FEATURES", "admin-fine-grained-authz:v1,token-exchange")
                withExposedPorts(8080, 9000)
                // Wait for Keycloak to be ready using the management health endpoint
                waitingFor(
                    Wait
                        .forHttp("/health/ready")
                        .forPort(9000)
                        .forStatusCode(200)
                        .withStartupTimeout(java.time.Duration.ofMinutes(2))
                )
            }
}

object SharedMongoContainer {
    val instance: MongoDBContainer =
        MongoDBContainer("mongo:8.0.13").apply {
            withNetwork(it.Network.instance)
            waitingFor(Wait.forListeningPort())
        }
}
