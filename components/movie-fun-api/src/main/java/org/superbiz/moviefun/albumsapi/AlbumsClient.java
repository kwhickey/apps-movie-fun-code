package org.superbiz.moviefun.albumsapi;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.converters.Auto;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpMethod.GET;

@Component
public class AlbumsClient {
    private static Logger logger = LoggerFactory.getLogger(AlbumsClient.class);
    private String albumsUrl;
    private RestOperations restOperations;
    private URI albumsUri;
    private RestOperations noLoadBalancedRestTemplate;
    private EurekaClient discoveryClient;

    @Autowired
    public AlbumsClient(String albumsUrl, RestOperations restOperations, RestOperations noLoadBalancedRestTemplate, EurekaClient discoveryClient) throws URISyntaxException {
        this.albumsUrl = albumsUrl + "/albums";
        this.albumsUri = new URIBuilder(albumsUrl).setPath("/albums").build();
        logger.info("URI: Built albums URI with string {} and URI {}", albumsUri.toString(), albumsUrl);
        this.restOperations = restOperations;
        this.noLoadBalancedRestTemplate = noLoadBalancedRestTemplate;
        this.discoveryClient = discoveryClient;
    }

    public void addAlbum(AlbumInfo album) {
        restOperations.postForEntity(albumsUrl, album, AlbumInfo.class);
    }

    public AlbumInfo find(long id) {
        return restOperations.getForEntity(albumsUrl + "/" + id, AlbumInfo.class).getBody();
    }

    public List<AlbumInfo> getAlbums() {
        ParameterizedTypeReference<List<AlbumInfo>> albumListType = new ParameterizedTypeReference<List<AlbumInfo>>() {
        };

        InstanceInfo inst = null;
        if (discoveryClient != null) {
            inst = discoveryClient.getNextServerFromEureka("ALBUM-SERVICE", false);
            if (inst == null) {
                logger.warn("NULL instance (non-secure) returned from discoveryClient. Could not find servers for 'ALBUM-SERVICE'");
            } else {
                logger.info("Instance ID for ALBUM-SERVICE found. ID {}, InstanceID {}", inst.getId(), inst.getInstanceId());
                logger.info("Instance Details (non-secure):\n{}", ToStringBuilder.reflectionToString(inst));
            }
        }
        else {
            logger.error("Cannot get from Eureka. Local 'discoveryClient' is NULL");
        }

        inst = null;
        if (discoveryClient != null) {
            inst = discoveryClient.getNextServerFromEureka("ALBUM-SERVICE", true);
            if (inst == null) {
                logger.warn("NULL instance (secure) returned from discoveryClient. Could not find servers for 'ALBUM-SERVICE'");
            } else {
                logger.info("Instance ID for ALBUM-SERVICE found. ID {}, InstanceID {}", inst.getId(), inst.getInstanceId());
                logger.info("Instance Details (secure):\n{}", ToStringBuilder.reflectionToString(inst));
            }
        }
        else {
            logger.error("Cannot get from Eureka. Local 'discoveryClient' is NULL");
        }

        List<AlbumInfo> results = null;
        try {
            logger.info("Trying to get albums via URL. URL = {}", albumsUrl);
            results = restOperations.exchange(albumsUrl, GET, null, albumListType).getBody();
        }
        catch (Exception e) {
            logger.warn("Was not able to get albums via albumsUrl: {}", albumsUrl);
            logger.error("Ex Type: {}. Message was: {}. Causing message: {}", e.getClass().getName(), e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "<UNKNOWN>");
        }
        try {
            logger.info("Trying to get albums again with URI instead of URL. URI = {}", albumsUri);
            results = restOperations.exchange(albumsUri, GET, null, albumListType).getBody();
        }
        catch (Exception e) {
            logger.warn("Was not able to get albums via albumsUri: {}", albumsUri);
            logger.error("Ex Type: {}. Message was: {}. Causing message: {}", e.getClass().getName(), e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "<UNKNOWN>");
        }

        try {
            logger.info("Trying to get albums again with URI instead of URL. URI = {}", albumsUri);
            results = restOperations.exchange(albumsUri, GET, null, albumListType).getBody();

            logger.info("Retrieving ALBUM-SERVICE homepage URL from Eureka using EurekaClient");
            if (discoveryClient != null) {
                if (inst == null) {
                    logger.warn("NULL instance returned from discoveryClient. Could not find servers for 'ALBUM-SERVICE'");
                } else {
                    logger.info("Returning homepageUrl for ALBUM-SERVICE: {}", inst.getHomePageUrl());
                    results = restOperations.exchange(inst.getHomePageUrl() + "albums", GET, null, albumListType).getBody();
                }
            }
        }
        catch (Exception e) {
            logger.warn("Was not able to get albums via discoveryClient homepage URL");
            logger.error("Ex Type: {}. Message was: {}. Causing message: {}", e.getClass().getName(), e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "<UNKNOWN>");
        }

        try {
            String httpsUrl = "https://" + inst.getHostName() + ":" + inst.getSecurePort() + "/albums";
            logger.info("Trying to get albums again with custom RestTemplate and custom built HTTPS url: {}", httpsUrl);
            results = noLoadBalancedRestTemplate.exchange(httpsUrl, GET, null, albumListType).getBody();
        }
        catch (Exception e) {
            logger.warn("Was not able to get albums via custom https URL");
            logger.error("Ex Type: {}. Message was: {}. Causing message: {}", e.getClass().getName(), e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "<UNKNOWN>");
        }

        return results;
    }
}
