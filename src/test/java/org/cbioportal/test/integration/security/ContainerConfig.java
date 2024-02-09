package org.cbioportal.test.integration.security;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.cbioportal.test.integration.MysqlInitializer;
import org.cbioportal.test.integration.OAuth2KeycloakInitializer;
import org.cbioportal.test.integration.OAuth2ResourceServerKeycloakInitializer;
import org.cbioportal.test.integration.SamlKeycloakInitializer;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class ContainerConfig {
    
    public final static int CBIO_PORT = 8080;
    
    public final static int MOCKSERVER_PORT = 8085;
    public final static String DOWNLOAD_FOLDER = "/tmp/browser_downloads";
    
    private static final String KEYCLOAK_IMAGE_VERSION = "quay.io/keycloak/keycloak:22.0.5";
    private static final String MYSQL_IMAGE_VERSION = "mysql:5.7";
    private static final String MOCKSERVER_IMAGE_VERSION = "docker.io/mockserver/mockserver:5.15.0";

    static final MySQLContainer mysqlContainer;
    static final GenericContainer mockServerContainer;
    static final KeycloakContainer keycloakContainer;
    static final ChromeDriver chromeDriver;

    static {

        keycloakContainer = new KeycloakContainer(KEYCLOAK_IMAGE_VERSION)
            .withRealmImportFile("security/keycloak-configuration-generated.json")
            .withAdminUsername("admin")
            .withAdminPassword("admin")
            .withEnv("KC_HOSTNAME", "host.testcontainers.internal")
            .withEnv("KC_HOSTNAME_ADMIN", "localhost");

        mockServerContainer = new GenericContainer(MOCKSERVER_IMAGE_VERSION)
            .withExposedPorts(1080);
        mockServerContainer.setPortBindings(ImmutableList.of(String.format("%s:1080", MOCKSERVER_PORT)));

        mysqlContainer = (MySQLContainer) new MySQLContainer(MYSQL_IMAGE_VERSION)
            .withClasspathResourceMapping("cgds.sql", "/docker-entrypoint-initdb.d/a_schema.sql", BindMode.READ_ONLY)
            .withClasspathResourceMapping("seed_mini.sql", "/docker-entrypoint-initdb.d/b_seed.sql", BindMode.READ_ONLY)
            .withStartupTimeout(Duration.ofMinutes(10));

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--headless");
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", DOWNLOAD_FOLDER);
        prefs.put("profile.default_content_settings.popups", 0);
        prefs.put("download.prompt_for_download", "false");
        prefs.put("download.directory_upgrade", "true");
        options.setExperimentalOption("prefs", prefs);
        chromeDriver = new ChromeDriver(options);

        mysqlContainer.start();
        mockServerContainer.start();
        keycloakContainer.start();
    }

    // Update application properties with connection info on Keycloak container
    public static class MySamlKeycloakInitializer extends SamlKeycloakInitializer {
        @Override
        public void initialize(
            ConfigurableApplicationContext configurableApplicationContext) {
            super.initializeImpl(configurableApplicationContext, keycloakContainer);
        }
    }

    // Update application properties with connection info on Keycloak container
    public static class MyOAuth2KeycloakInitializer extends OAuth2KeycloakInitializer {
        @Override
        public void initialize(
            ConfigurableApplicationContext configurableApplicationContext) {
            super.initializeImpl(configurableApplicationContext, keycloakContainer);
        }
    }

    // Update application properties with connection info on Mysql container
    public static class MyMysqlInitializer extends MysqlInitializer {
        @Override
        public void initialize(
            ConfigurableApplicationContext configurableApplicationContext) {
            super.initializeImpl(configurableApplicationContext, mysqlContainer);
        }
    }

    public static class MyOAuth2ResourceServerKeycloakInitializer extends OAuth2ResourceServerKeycloakInitializer {
        @Override
        public void initialize(
            ConfigurableApplicationContext configurableApplicationContext) {
            super.initializeImpl(configurableApplicationContext, keycloakContainer);
        }
    } 

    // Expose the ports for the cBioPortal Spring application and keycloak inside 
    // the Chrome container. Each address is available on http://host.testcontainers.internal:<port>
    // in the browser container.
    public static class PortInitializer implements
        ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues values = TestPropertyValues.of(
                "server.port=" + CBIO_PORT
            );
            values.applyTo(applicationContext);
        }
    }

}