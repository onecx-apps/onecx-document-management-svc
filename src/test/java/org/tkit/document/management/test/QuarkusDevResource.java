package org.tkit.document.management.test;

import java.util.HashMap;
import java.util.Map;

import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QuarkusDevResource implements QuarkusTestResourceLifecycleManager {

    private String dockerHostIp = null;

    @Override
    public Map<String, String> start() {
        DockerClientFactory.instance().client();

        try {

            Map<String, String> properties = new HashMap<>();
            return properties;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Could not start localstack server", e);
        }
    }

    @Override
    public void stop() {
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
