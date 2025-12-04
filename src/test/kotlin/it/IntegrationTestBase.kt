package it

import com.fasterxml.jackson.databind.ObjectMapper
import com.personal.Application
import it.infra.config.IntegrationTestConfig
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
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
        val mongoContainer = SharedMongoContainer.instance

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
    }

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    @Autowired
    protected lateinit var restTemplate: RestTemplate
}

object Network {
    val instance: Network = Network.newNetwork()
}

object SharedMongoContainer {
    val instance: MongoDBContainer =
        MongoDBContainer("mongo:8.0.13").apply {
            withNetwork(it.Network.instance)
            waitingFor(Wait.forListeningPort())
        }
}
