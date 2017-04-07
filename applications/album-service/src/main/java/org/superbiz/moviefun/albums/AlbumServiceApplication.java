package org.superbiz.moviefun.albums;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;
import org.superbiz.moviefun.blobstore.S3Store;

import java.io.InputStream;

@SpringBootApplication
@EnableEurekaClient
public class AlbumServiceApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AlbumServiceApplication.class);

    @Override
    public void run(String... strings) {
        // TODO: Swallow the exception if it doesn't take
        try {
            BlobStore blobStore = blobStore();
            if (!blobStore.get("albums.csv").isPresent()) {
                logger.info("Did not find albums.csv seeded into blob store. Adding it from classpath.");
                ClassLoader loader = this.getClass().getClassLoader();
                InputStream fileStream = loader.getResourceAsStream("albums.csv");
                if (fileStream == null) {
                    logger.warn("Did not find album.csv in classpath to load.");
                    return;
                }

                Blob csvBlob = new Blob("albums.csv", fileStream, "text/csv");
                blobStore.put(csvBlob);
            }
        }
        catch (Exception e) {
            logger.error("ERROR: Unable to access blob store to see if albums.csv can be processed", e);
        }
    }

    public static void main(String... args) {
        SpringApplication.run(AlbumServiceApplication.class, args);
    }

    @Value("${s3.endpointUrl}") String s3EndpointUrl;
    @Value("${s3.accessKey}") String s3AccessKey;
    @Value("${s3.secretKey}") String s3SecretKey;
    @Value("${s3.bucketName}") String s3BucketName;

    @Bean
    public BlobStore blobStore() {
        AWSCredentials credentials = new BasicAWSCredentials(s3AccessKey, s3SecretKey);
        AmazonS3Client s3Client = new AmazonS3Client(credentials);
        final S3ClientOptions clientOptions = S3ClientOptions.builder().setPathStyleAccess(true).disableChunkedEncoding().build();
        s3Client.setS3ClientOptions(clientOptions);
        s3Client.setEndpoint(s3EndpointUrl);
        return new S3Store(s3Client, s3BucketName);
    }
}
