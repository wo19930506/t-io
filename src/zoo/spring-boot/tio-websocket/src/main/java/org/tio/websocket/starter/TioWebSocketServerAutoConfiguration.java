package org.tio.websocket.starter;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.tio.cluster.redisson.RedissonTioClusterTopic;
import org.tio.common.starter.RedisInitializer;
import org.tio.core.intf.GroupListener;
import org.tio.core.stat.IpStatListener;
import org.tio.server.ServerTioConfig;
import org.tio.websocket.server.WsServerAioListener;
import org.tio.websocket.server.handler.IWsMsgHandler;
import org.tio.websocket.starter.configuration.TioWebSocketServerClusterProperties;
import org.tio.websocket.starter.configuration.TioWebSocketServerProperties;
import org.tio.websocket.starter.configuration.TioWebSocketServerRedisClusterProperties;
import org.tio.websocket.starter.configuration.TioWebSocketServerSslProperties;


/**
 * @author fanpan26
 * */
@Configuration
@Import(TioWebSocketServerInitializerConfiguration.class)
@ConditionalOnBean(TioWebSocketServerMarkerConfiguration.Marker.class)
@EnableConfigurationProperties({ TioWebSocketServerProperties.class,
        TioWebSocketServerClusterProperties.class,
        TioWebSocketServerRedisClusterProperties.class,
        TioWebSocketServerSslProperties.class})
public class TioWebSocketServerAutoConfiguration {

    /**
     *  cluster topic channel
     * */
    private static final String CLUSTER_TOPIC_CHANNEL = "tio_ws_spring_boot_starter";

    @Autowired(required = false)
    private IWsMsgHandler tioWebSocketMsgHandler;

    @Autowired(required = false)
    private IpStatListener tioWebSocketIpStatListener;

    @Autowired(required = false)
    private GroupListener tioWebSocketGroupListener;

    @Autowired(required = false)
    private WsServerAioListener tioWebSocketServerAioListener;

    @Autowired
    private TioWebSocketServerClusterProperties clusterProperties;

    @Autowired
    private TioWebSocketServerRedisClusterProperties redisConfig;

    @Autowired
    private TioWebSocketServerProperties serverProperties;

    @Autowired
    private TioWebSocketServerSslProperties serverSslProperties;

    @Autowired(required = false)
    private RedissonTioClusterTopic redissonTioClusterTopic;

    @Autowired(required = false)
    private TioWebSocketClassScanner tioWebSocketClassScanner;

    /**
     * Tio WebSocket Server bootstrap
     * */
    @Bean
    public TioWebSocketServerBootstrap webSocketServerBootstrap() {
        return new TioWebSocketServerBootstrap(serverProperties,
                clusterProperties,
                serverSslProperties,
                redissonTioClusterTopic,
                tioWebSocketMsgHandler,
                tioWebSocketIpStatListener,
                tioWebSocketGroupListener,
                tioWebSocketServerAioListener,
                tioWebSocketClassScanner);
    }

    @Bean
    public ServerTioConfig serverTioConfig(TioWebSocketServerBootstrap bootstrap){
        return bootstrap.getServerTioConfig();
    }

    @Bean(destroyMethod="shutdown")
    @ConditionalOnProperty(value = "tio.websocket.cluster.enabled",havingValue = "true",matchIfMissing = true)
    public RedisInitializer redisInitializer(ApplicationContext applicationContext) {
        return new RedisInitializer(redisConfig, applicationContext);
    }


    /**
     *  RedissonTioClusterTopic  with  RedisInitializer
     * */
    @Bean
    @ConditionalOnBean(RedisInitializer.class)
    public RedissonTioClusterTopic redissonTioClusterTopic(RedisInitializer redisInitializer) {
        return new RedissonTioClusterTopic(CLUSTER_TOPIC_CHANNEL,redisInitializer.getRedissonClient());
    }

    @Bean(destroyMethod = "destroy")
    @ConditionalOnProperty(value = "tio.websocket.server.use-scanner",havingValue = "true",matchIfMissing = true)
    public TioWebSocketClassScanner tioWebSocketClassScanner(ApplicationContext applicationContext) {
        return new TioWebSocketClassScanner(applicationContext);
    }
}
