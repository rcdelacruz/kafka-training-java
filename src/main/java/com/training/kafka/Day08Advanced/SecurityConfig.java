package com.training.kafka.Day08Advanced;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Day 8: Security Configuration
 * 
 * Demonstrates various security configurations for Kafka clients:
 * - SSL/TLS encryption
 * - SASL authentication
 * - ACL configurations
 * - Best practices for production security
 */
public class SecurityConfig {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    
    /**
     * SSL/TLS Configuration for encrypted communication
     */
    public static Properties getSslConfig(String keystorePath, String keystorePassword,
                                         String truststorePath, String truststorePassword) {
        Properties props = new Properties();
        
        // SSL Configuration
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
        props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keystorePath);
        props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, keystorePassword);
        props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, keystorePassword);
        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, truststorePath);
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, truststorePassword);
        
        // SSL Protocol and cipher suites
        props.put(SslConfigs.SSL_PROTOCOL_CONFIG, "TLSv1.3");
        props.put(SslConfigs.SSL_ENABLED_PROTOCOLS_CONFIG, "TLSv1.3,TLSv1.2");
        props.put(SslConfigs.SSL_CIPHER_SUITES_CONFIG, 
            "TLS_AES_256_GCM_SHA384,TLS_CHACHA20_POLY1305_SHA256,TLS_AES_128_GCM_SHA256");
        
        // SSL endpoint identification
        props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "https");
        
        logger.info("SSL configuration created");
        return props;
    }
    
    /**
     * SASL/PLAIN Configuration for authentication
     */
    public static Properties getSaslPlainConfig(String username, String password) {
        Properties props = new Properties();
        
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        props.put(SaslConfigs.SASL_JAAS_CONFIG, 
            String.format("org.apache.kafka.common.security.plain.PlainLoginModule required " +
                         "username=\"%s\" password=\"%s\";", username, password));
        
        logger.info("SASL/PLAIN configuration created for user: {}", username);
        return props;
    }
    
    /**
     * SASL/SCRAM Configuration for secure authentication
     */
    public static Properties getSaslScramConfig(String username, String password) {
        Properties props = new Properties();
        
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
        props.put(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-512");
        props.put(SaslConfigs.SASL_JAAS_CONFIG, 
            String.format("org.apache.kafka.common.security.scram.ScramLoginModule required " +
                         "username=\"%s\" password=\"%s\";", username, password));
        
        logger.info("SASL/SCRAM configuration created for user: {}", username);
        return props;
    }
    
    /**
     * OAuth/OIDC Configuration for modern authentication
     */
    public static Properties getOAuthConfig(String clientId, String clientSecret, 
                                           String tokenEndpoint, String scope) {
        Properties props = new Properties();
        
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
        props.put(SaslConfigs.SASL_MECHANISM, "OAUTHBEARER");
        props.put(SaslConfigs.SASL_JAAS_CONFIG, 
            "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required;");
        
        // OAuth specific configurations
        props.put("sasl.oauthbearer.token.endpoint.url", tokenEndpoint);
        props.put("sasl.oauthbearer.client.id", clientId);
        props.put("sasl.oauthbearer.client.secret", clientSecret);
        props.put("sasl.oauthbearer.scope", scope);
        
        logger.info("OAuth configuration created for client: {}", clientId);
        return props;
    }
    
    /**
     * Complete SSL + SASL Configuration for production
     */
    public static Properties getProductionSecurityConfig(String keystorePath, String keystorePassword,
                                                        String truststorePath, String truststorePassword,
                                                        String username, String password) {
        Properties props = new Properties();
        
        // Combine SSL and SASL
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
        
        // SSL Configuration
        props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keystorePath);
        props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, keystorePassword);
        props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, keystorePassword);
        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, truststorePath);
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, truststorePassword);
        props.put(SslConfigs.SSL_PROTOCOL_CONFIG, "TLSv1.3");
        props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "https");
        
        // SASL Configuration
        props.put(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-512");
        props.put(SaslConfigs.SASL_JAAS_CONFIG, 
            String.format("org.apache.kafka.common.security.scram.ScramLoginModule required " +
                         "username=\"%s\" password=\"%s\";", username, password));
        
        logger.info("Production security configuration created");
        return props;
    }
    
    /**
     * Producer-specific security configuration
     */
    public static Properties getSecureProducerConfig(String bootstrapServers, Properties securityProps) {
        Properties props = new Properties();
        props.putAll(securityProps);
        
        // Basic producer config
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "secure-producer");
        
        // Security-enhanced settings
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        
        // Connection security
        props.put(ProducerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, 540000); // 9 minutes
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);
        
        return props;
    }
    
    /**
     * Consumer-specific security configuration
     */
    public static Properties getSecureConsumerConfig(String bootstrapServers, String groupId, 
                                                    Properties securityProps) {
        Properties props = new Properties();
        props.putAll(securityProps);
        
        // Basic consumer config
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "secure-consumer");
        
        // Security-enhanced settings
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        
        // Connection security
        props.put(ConsumerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, 540000); // 9 minutes
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 45000);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 15000);
        
        return props;
    }
    
    /**
     * Generate sample keystore and truststore creation commands
     */
    public static void printKeystoreCreationCommands() {
        logger.info("Sample commands to create keystores and truststores:");
        logger.info("");
        
        logger.info("1. Create CA (Certificate Authority):");
        logger.info("   openssl req -new -x509 -keyout ca-key -out ca-cert -days 365 -passout pass:ca-password");
        logger.info("");
        
        logger.info("2. Create server keystore:");
        logger.info("   keytool -keystore server.keystore.jks -alias server -validity 365 -genkey -keyalg RSA -storepass server-password");
        logger.info("");
        
        logger.info("3. Create certificate signing request:");
        logger.info("   keytool -keystore server.keystore.jks -alias server -certreq -file cert-file -storepass server-password");
        logger.info("");
        
        logger.info("4. Sign the certificate:");
        logger.info("   openssl x509 -req -CA ca-cert -CAkey ca-key -in cert-file -out cert-signed -days 365 -CAcreateserial -passin pass:ca-password");
        logger.info("");
        
        logger.info("5. Import CA certificate into keystore:");
        logger.info("   keytool -keystore server.keystore.jks -alias CARoot -import -file ca-cert -storepass server-password");
        logger.info("");
        
        logger.info("6. Import signed certificate into keystore:");
        logger.info("   keytool -keystore server.keystore.jks -alias server -import -file cert-signed -storepass server-password");
        logger.info("");
        
        logger.info("7. Create client truststore:");
        logger.info("   keytool -keystore client.truststore.jks -alias CARoot -import -file ca-cert -storepass truststore-password");
    }
    
    /**
     * Print ACL commands for setting up authorization
     */
    public static void printAclCommands() {
        logger.info("Sample ACL commands for authorization:");
        logger.info("");
        
        logger.info("1. Create topic ACLs:");
        logger.info("   kafka-acls --authorizer-properties zookeeper.connect=localhost:2181 \\");
        logger.info("     --add --allow-principal User:producer-user \\");
        logger.info("     --operation Write --topic user-events");
        logger.info("");
        
        logger.info("2. Create consumer group ACLs:");
        logger.info("   kafka-acls --authorizer-properties zookeeper.connect=localhost:2181 \\");
        logger.info("     --add --allow-principal User:consumer-user \\");
        logger.info("     --operation Read --topic user-events \\");
        logger.info("     --group consumer-group");
        logger.info("");
        
        logger.info("3. List ACLs:");
        logger.info("   kafka-acls --authorizer-properties zookeeper.connect=localhost:2181 --list");
        logger.info("");
        
        logger.info("4. Create admin ACLs:");
        logger.info("   kafka-acls --authorizer-properties zookeeper.connect=localhost:2181 \\");
        logger.info("     --add --allow-principal User:admin-user \\");
        logger.info("     --operation All --topic '*' --group '*'");
    }
    
    /**
     * Print security best practices
     */
    public static void printSecurityBestPractices() {
        logger.info("Kafka Security Best Practices:");
        logger.info("");
        
        logger.info("1. Network Security:");
        logger.info("   - Use SSL/TLS for all communications");
        logger.info("   - Implement network segmentation");
        logger.info("   - Use VPCs and security groups");
        logger.info("   - Regularly rotate certificates");
        logger.info("");
        
        logger.info("2. Authentication:");
        logger.info("   - Use SASL/SCRAM-SHA-512 for strong authentication");
        logger.info("   - Implement OAuth/OIDC for modern auth flows");
        logger.info("   - Use strong passwords and rotate regularly");
        logger.info("   - Implement multi-factor authentication");
        logger.info("");
        
        logger.info("3. Authorization:");
        logger.info("   - Enable ACLs for fine-grained access control");
        logger.info("   - Follow principle of least privilege");
        logger.info("   - Use resource-specific permissions");
        logger.info("   - Regular audit access permissions");
        logger.info("");
        
        logger.info("4. Data Protection:");
        logger.info("   - Enable encryption at rest");
        logger.info("   - Use encryption in transit (SSL/TLS)");
        logger.info("   - Implement data masking for sensitive data");
        logger.info("   - Use Schema Registry for data governance");
        logger.info("");
        
        logger.info("5. Monitoring and Auditing:");
        logger.info("   - Enable security event logging");
        logger.info("   - Monitor authentication failures");
        logger.info("   - Track authorization violations");
        logger.info("   - Implement alerting for security events");
    }
    
    /**
     * Main method for demonstration
     */
    public static void main(String[] args) {
        logger.info("Kafka Security Configuration Examples");
        logger.info("=====================================");
        
        // Print keystore creation commands
        printKeystoreCreationCommands();
        
        // Print ACL commands
        printAclCommands();
        
        // Print best practices
        printSecurityBestPractices();
        
        // Example configurations
        logger.info("Example Security Configurations:");
        logger.info("");
        
        // SSL configuration
        Properties sslProps = getSslConfig(
            "/path/to/client.keystore.jks", "keystore-password",
            "/path/to/client.truststore.jks", "truststore-password"
        );
        logger.info("SSL Properties created with {} entries", sslProps.size());
        
        // SASL configuration
        Properties saslProps = getSaslScramConfig("kafka-user", "secret-password");
        logger.info("SASL Properties created with {} entries", saslProps.size());
        
        // Production configuration
        Properties prodProps = getProductionSecurityConfig(
            "/path/to/client.keystore.jks", "keystore-password",
            "/path/to/client.truststore.jks", "truststore-password",
            "kafka-user", "secret-password"
        );
        logger.info("Production Security Properties created with {} entries", prodProps.size());
    }
}