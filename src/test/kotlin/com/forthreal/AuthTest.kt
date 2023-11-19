package com.forthreal

import com.forthreal.domain.User
import com.forthreal.configuration.AppConfiguration
import com.forthreal.mapper.UserMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.utility.DockerImageName

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.NONE,
    replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = [AuthTest.Initializer::class],
    classes = [AppConfiguration::class,AppMain::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class AuthTest {
    private val logger = LoggerFactory.getLogger(AuthTest::class.java)
    @Autowired
    private lateinit var server: ServletWebServerApplicationContext
    @Autowired
    private lateinit var userMapper: UserMapper
    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @BeforeAll
    fun startContainers() {
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
    fun testCreateUser() {
        val user = User(username = "user1", password = passwordEncoder.encode("pwd"))
        Assertions.assertDoesNotThrow { userMapper.addUser(user) }
    }

}