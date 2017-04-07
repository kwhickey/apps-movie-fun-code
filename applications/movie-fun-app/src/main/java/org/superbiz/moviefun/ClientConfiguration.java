package org.superbiz.moviefun;

import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.converters.Auto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.eureka.CloudEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestOperations;
import org.superbiz.moviefun.albumsapi.AlbumsClient;
import org.superbiz.moviefun.moviesapi.MoviesClient;

import javax.annotation.Resource;
import java.net.URISyntaxException;

@Configuration
public class ClientConfiguration {
    private static Logger logger = LoggerFactory.getLogger(ClientConfiguration.class);

    @Value("${albums.url}") String albumsUrl;
    @Value("${movies.url}") String moviesUrl;

    @Autowired
    public EurekaClient eurekaClient;

    @Resource(name="noLoadBalancedRestTemplate")
    public RestOperations noLoadBalancedRestTemplate;

    @Bean
    public AlbumsClient albumsClient(RestOperations restOperations) throws URISyntaxException {
        if (this.eurekaClient == null) {
            String msg = "this.eurekaClient was not autowired and was null. Erroring out";
            logger.error(msg);
            throw new RuntimeException(msg);
        }
//        return new AlbumsClient(albumsUrl, restOperations, noLoadBalancedRestTemplate, eurekaClient);
        return new AlbumsClient(albumsUrl, restOperations);
    }

    @Bean
    public MoviesClient moviesClient(RestOperations restOperations) {
        return new MoviesClient(moviesUrl, restOperations);
    }
}
