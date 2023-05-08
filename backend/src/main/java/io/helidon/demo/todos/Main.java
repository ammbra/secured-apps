package io.helidon.demo.todos;
//
import io.dekorate.certmanager.annotation.*;
import io.dekorate.kubernetes.annotation.*;
import io.helidon.config.Config;
import io.helidon.config.OverrideSources;
import io.helidon.microprofile.server.Server;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.logging.LogManager;

import static io.helidon.config.ConfigSources.*;
import static io.helidon.config.PollingStrategies.regular;

/**
 * Main class to start the service.
 */
@KubernetesApplication( ports = @Port(name = "http", containerPort = 8854),
        mounts = {@Mount(name = "config-volume", path = "/conf"), @Mount(name="files-volume", path = "backup-tls/")},
        configMapVolumes = @ConfigMapVolume(configMapName = "helidon-examples-todo-backend", volumeName = "config-volume"),
        pvcVolumes = @PersistentVolumeClaimVolume(volumeName = "files-volume", claimName = "upload-claim-backend"),
        serviceType = ServiceType.LoadBalancer, ingress = @Ingress(expose = true))
@Certificate(name="backend-cert", secretName = "backend-tls", duration = "2160h", renewBefore = "360h",
        subject = @Subject(organizations="some organization"),
        privateKey = @CertificatePrivateKey(algorithm = PrivateKeyAlgorithm.RSA, encoding = PrivateKeyEncoding.PKCS1, size = 3072),
        usages = {"server-auth", "client-auth"},
        dnsNames = {"client.mycompany.com","server.mycompany.com"},
        issuerRef = @IssuerRef(name="ca-issuer", group = "cert-manager.io", kind = "Issuer"),
        keystores = @CertificateKeystores(
                jks = @CertificateKeystore(create = true, passwordSecretRef = @LocalObjectReference(name = "back-jks-password-secret", key="password")),
                pkcs12 = @CertificateKeystore(create = true, passwordSecretRef = @LocalObjectReference(name = "back-pkcs12-password-secret", key="password"))
        ))
public final class Main {

    /**
     * Cannot be instantiated.
     */
    private Main() {
    }

    /**
     * Application main entry point.
     *
     * @param args command line arguments
     * @throws IOException if an error occurred while reading logging
     * configuration
     */
    public static void main(final String[] args) throws IOException {
        LogManager.getLogManager().readConfiguration(
                Main.class.getResourceAsStream("/logging.properties"));
        Config config = buildConfig();

        // as we need to use custom filter
        // we need to build Server with custom config

        Server server = Server.builder()
                .config(config)
                .build();

        server.start();
    }

    /**
     * Load the configuration from all sources.
     * @return the configuration root
     */
    static Config buildConfig() {
        return Config.builder()
                .sources(List.of(
                        environmentVariables(),
                        classpath("application.yaml")))
                .overrides(OverrideSources.file("conf/overrides.properties")
                        .pollingStrategy(regular(Duration.ofSeconds(1))))
                .build();
    }
}
