package com.forthreal

import com.forthreal.api.SampleController
import com.forthreal.domain.User
import com.forthreal.configuration.AppConfiguration
import com.forthreal.mapper.UserMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.utility.DockerImageName

@EnableWebMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.NONE,
    replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = [AuthTest.Initializer::class],
    classes = [AppConfiguration::class,AppMain::class,SampleController::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation::class)
open class AuthTest {
    private val logger = LoggerFactory.getLogger(AuthTest::class.java)
    @Autowired
    private lateinit var server: ServletWebServerApplicationContext
    @Autowired
    private lateinit var userMapper: UserMapper
    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var mockMvc: MockMvc

    @BeforeAll
    fun startContainers() {
        val secConfigurer = SecurityMockMvcConfigurers.springSecurity()!!
        mockMvc = MockMvcBuilders.webAppContextSetup(server)
            .apply<DefaultMockMvcBuilder>(secConfigurer).build()
        userMapper.createTable()
    }

    class Initializer: ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Container
        private val mySQLContainer: MySQLContainer<*> = MySQLContainer(DockerImageName.parse("mysql:5.7.34"))
            .waitingFor(Wait.forLogMessage(".*Container is started.*", 1))

        init {
            mySQLContainer.start()
        }

        override fun initialize(applicationContext: ConfigurableApplicationContext) {
            TestPropertyValues.of("spring.datasource.username=${mySQLContainer.username}",
                "spring.datasource.password=${mySQLContainer.password}",
                "spring.datasource.url=${mySQLContainer.jdbcUrl}",
                "spring.datasource.platform=mysql",
                "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver")
                .applyTo(applicationContext)
        }

    }


    @Test
    @Order(0)
    @DisplayName("Creation of a new DB user")
    fun testCreateUser() {
        val user = User(username = "user1", password = passwordEncoder.encode("pwd"))
        Assertions.assertDoesNotThrow { userMapper.addUser(user) }
    }

    @Test
    @Order(1)
    @DisplayName("Protected area - should redirect to login")
    fun testRedirect() {
        val request = MockMvcRequestBuilders.request(HttpMethod.GET, "/protected")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}")
        val result = mockMvc.perform(request).andReturn()
        Assertions.assertEquals(302, result.response.status)
    }

    @Test
    @Order(2)
    @DisplayName("Perform login with the new credentials")
    @WithMockUser(username = "user1", password = "pwd")
    fun testLogin() {
        val request = MockMvcRequestBuilders.request(HttpMethod.GET, "/protected")
        Assertions.assertDoesNotThrow {
            val result = mockMvc.perform(request).andExpect(status().isOk)
        }
    }

}