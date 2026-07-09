package com.eventostec.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Configuração do cliente S3 (AWS SDK v2) usado para upload de imagens de eventos.
 */
@Configuration
public class AWSConfig {

    @Value("${aws.region}")
    private String awsRegion;

    /**
     * Cria o {@link S3Client} na região configurada, usando as credenciais
     * padrão do ambiente (IAM role na EC2 ou {@code ~/.aws} localmente).
     *
     * @return o cliente S3 pronto para uso
     */
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                .httpClient(UrlConnectionHttpClient.create())
                .build();
    }
}
