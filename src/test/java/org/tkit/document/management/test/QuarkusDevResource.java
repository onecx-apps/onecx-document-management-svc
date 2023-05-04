package org.tkit.document.management.test;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QuarkusDevResource implements QuarkusTestResourceLifecycleManager {

    private static final DockerImageName MOCKING_IMAGE_NAME = DockerImageName.parse("harbor.1000kit.org/1000kit/mocking-server")
            .withTag("master");

    private String dockerHostIp = null;

    GenericContainer<?> mockingServer = new GenericContainer<>(MOCKING_IMAGE_NAME)
            .waitingFor(Wait.forListeningPort())
            .withExposedPorts(8080)
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("Mocking")));

    @Override
    public Map<String, String> start() {
        DockerClientFactory.instance().client();

        try {

            mockingServer.start();

            if ("localhost".equals(mockingServer.getHost())) {
                dockerHostIp = mockingServer.getContainerInfo().getNetworkSettings().getNetworks().entrySet().iterator().next()
                        .getValue().getGateway();
                log.info("DockerHostIp is: {}", dockerHostIp);
            }

            log.info("MOCKING: " + getUrl(mockingServer, 8080, ""));

            Map<String, String> properties = new HashMap<>();
            return properties;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Could not start localstack server", e);
        }
    }

    @Override
    public void stop() {
        if (mockingServer != null) {
            mockingServer.close();
        }
    }

    private String getUrl(GenericContainer<?> container, Integer port, String path) {
        String url = String.format("http://%s:%s%s", container.getHost(), container.getMappedPort(port), path);

        return getWithDockerHost(url);

    }

    private String getWithDockerHost(String localhostString) {

        if (dockerHostIp != null) {
            String dockerHostString = localhostString.replace("127.0.0.1", dockerHostIp).replace("localhost", dockerHostIp);
            log.info("setting Docker Host Ip from: {} to {}", localhostString, dockerHostString);
            return dockerHostString;
        } else {
            return localhostString;
        }

    }

}
