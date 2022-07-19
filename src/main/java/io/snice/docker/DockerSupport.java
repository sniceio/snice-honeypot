package io.snice.docker;

import io.snice.networking.common.NetworkingUtils;

import java.net.Inet4Address;
import java.util.List;
import java.util.Optional;

/**
 * Not strictly only for docker support but it is more common that we need to
 * figure things out, such as our actual IP address, when we running within docker.
 */
public class DockerSupport {

    /**
     * Boolean to be set to indicate that we are running inside a docker instance.
     * A value of "true" or "yes" (case insensitive) will yield true, all other
     * combinations will be false.
     */
    public static final String SNICE_DOCKER = "SNICE_DOCKER";

    /**
     * Used for passing in the host ip of the main interface. If this property
     * is specified, we will not try and figure this out automatically.
     */
    public static final String SNICE_PRIMARY_HOST_IP = "SNICE_PRIMARY_HOST_IP";

    /**
     * Used for passing in the name of the main network interface.
     * If this is specified, we will not try and figure it out automatically.
     * <p>
     * If this is specified but the {@link #SNICE_PRIMARY_HOST_IP} isn't, then we will try
     * and find out the IP associated with this interface. If we cannot because
     * the interface doesn't exist, the {@link Builder#build()} method will
     * blow up.
     */
    public static final String SNICE_PRIMARY_INTERFACE = "SNICE_PRIMARY_INTERFACE";

    /**
     * The default gateway. This must be passed in since there is (as far as I know)
     * no reliable way of finding this out other than actually queering the host operating
     * system for this info (e.g. using the route command on Linux)
     */
    public static final String SNICE_DEFAULT_GATEWAY = "SNICE_DEFAULT_GATEWAY";

    public static boolean isRunningInsideDocker() {
        final String flag = System.getenv(SNICE_DOCKER);
        return "true".equalsIgnoreCase(flag) || "yes".equalsIgnoreCase(flag);
    }

    public static Builder of() {
        return new Builder();
    }

    private final String hostIp;
    private final String interfaceName;
    private final Optional<String> defaultGateway;

    private DockerSupport(final String hostIp, final String interfaceName, final Optional<String> defaultGateway) {
        this.hostIp = hostIp;
        this.interfaceName = interfaceName;
        this.defaultGateway = defaultGateway;
    }

    public String getPrimaryHostIp() {
        return hostIp;
    }

    public String getPrimaryInterfaceName() {
        return interfaceName;
    }

    public Optional<String> getDefaultGateway() {
        return defaultGateway;
    }

    @Override
    public String toString() {
        return "{ primaryHostIp: " + hostIp + ", primaryInterface: "
                + interfaceName + ", defaultGateway: " + defaultGateway.orElse("N/A") + "}";
    }

    public static class Builder {

        private boolean readFromSystemProperties = false;
        private String hostIp;
        private String interfaceName;
        private String defaultGateway;

        /**
         * Read configuration that has been passed in via system properties.
         * This is not done by default so you have to call this method.
         */
        public Builder withReadFromSystemProperties() {
            readFromSystemProperties = true;
            return this;
        }

        public DockerSupport build() {
            final var interfaceName = ensureInterfaceName();
            final var hostIp = ensureHostIp();
            return new DockerSupport(hostIp, interfaceName, maybeDefaultGateway());
        }

        /**
         * Figure out the host ip, if it hasn't already been specified.
         * <p>
         * Note: bloody side effect programming. Need to redo this.
         *
         * @return
         */
        private String ensureHostIp() {
            if (hostIp == null) {
                hostIp = System.getenv(SNICE_PRIMARY_HOST_IP);
            }

            if (hostIp != null) {
                return hostIp;
            }

            if (interfaceName != null) {
                return NetworkingUtils.getInet4Address(interfaceName).map(Inet4Address::getHostAddress)
                        .orElseThrow(() -> new IllegalArgumentException("Unable to locate an interface with name " + interfaceName));
            }

            final List<String> ignoreIfs = isRunningInsideDocker() ? List.of() : List.of("docker0");
            final var nic = NetworkingUtils.findPrimaryInterface(ignoreIfs);
            interfaceName = nic.getName();
            return NetworkingUtils.getInet4Address(nic).getHostAddress();
        }

        private String ensureInterfaceName() {
            if (interfaceName == null) {
                interfaceName = System.getenv(SNICE_PRIMARY_INTERFACE);
            }

            if (interfaceName != null) {
                return interfaceName;
            }

            final List<String> ignoreIfs = isRunningInsideDocker() ? List.of() : List.of("docker0");
            final var nic = NetworkingUtils.findPrimaryInterface(ignoreIfs);
            interfaceName = nic.getName();

            return interfaceName;
        }

        private Optional<String> maybeDefaultGateway() {
            if (defaultGateway != null) {
                return Optional.of(defaultGateway);
            }

            return Optional.ofNullable(System.getenv(SNICE_DEFAULT_GATEWAY));
        }


    }
}
