package org.example;

import lombok.SneakyThrows;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MultiStageNoHistoryProofTest {
    @SneakyThrows
    private static void assertDockerExists() {
        boolean dockerCli = new ProcessBuilder("command", "-v", "docker").start().waitFor() == 0;
        assertTrue(dockerCli, "this test needs docker to work");
    }

    @SneakyThrows
    @Order(0)
    @Test
    void generateImage() {
        /*
            this is a simple dockerfile,
            which includes 2 stages.
            the first stage has some private instructions,
            which produce some public data.
            the second stage contains public data from stage 1.
            the private data from stage 1 should be private.
         */
        // language=Dockerfile
        String dockerFile = """
                FROM alpine@sha256:51b67269f354137895d43f3b3d810bfacd3945438e94dc5ac55fdac340352f48
                RUN "echo something private" | echo "something public" > /data.txt
                FROM alpine@sha256:51b67269f354137895d43f3b3d810bfacd3945438e94dc5ac55fdac340352f48
                COPY --from=0 /data.txt /data.txt
                """;
        // let's build this image!
        assertDockerExists();

        var imageTag = getClass().getSimpleName().toLowerCase();
        var dockerBuild = new ProcessBuilder("docker", "build", "-", "-t", imageTag)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .redirectInput(ProcessBuilder.Redirect.PIPE)
                .start();

        new ByteArrayInputStream(dockerFile.getBytes(StandardCharsets.UTF_8))
                .transferTo(dockerBuild.getOutputStream());
        dockerBuild.getOutputStream().close();
        assertEquals(0, dockerBuild.waitFor(), "docker build did not exit 0");
    }

    @Test
    @Order(1)
    void testImage() {
        assertDockerExists();
    }
}
