package demo;

import java.util.Objects;
import java.util.Set;

import cn.jiguang.common.ClientConfig;
import cn.jiguang.common.ServiceHelper;
import cn.jiguang.common.connection.ApacheHttpClient;
import cn.jiguang.common.resp.DefaultResult;
import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;
import cn.jpush.api.push.model.notification.PlatformNotification;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

@SpringBootApplication
@EnableConfigurationProperties(JPushDemoApplication.JPushProperties.class)
@ManagedResource
public class JPushDemoApplication {

    private final JPushClient jPushClient;

    public JPushDemoApplication(JPushProperties properties) {
        ClientConfig clientConfig = ClientConfig.getInstance();
        this.jPushClient = new JPushClient(properties.masterSecret, properties.appKey, null, clientConfig);
        String authCode = ServiceHelper.getBasicAuthorization(properties.appKey, properties.masterSecret);
        this.jPushClient.getPushClient().setHttpClient(new ApacheHttpClient(authCode, null, clientConfig));
    }

    public static void main(String[] args) {
        SpringApplication.run(JPushDemoApplication.class, args);
    }

    @ManagedOperation
    public String subscribe(String registrationId, String tag) throws Exception {
        DefaultResult result = this.jPushClient.updateDeviceTagAlias(registrationId, null, Set.of(tag), Set.of());
        return result.toString();
    }

    @ManagedOperation
    public String pushToRegistrationId(String title, String content, String registrationId) throws Exception {
        return push(title, content, Audience.registrationId(registrationId));
    }

    @ManagedOperation
    public String pushToTag(String title, String content, String tag) throws Exception {
        return push(title, content, Audience.tag_and(tag));
    }

    private String push(String title, String content, Audience audience) throws Exception {
        PushPayload payload = PushPayload.newBuilder()
                .setPlatform(Platform.all())
                .setAudience(audience)
                .setNotification(Notification.newBuilder()
                        .addPlatformNotification(AndroidNotification.newBuilder()
                                .setTitle(title)
                                .setAlert(content)
                                .build())
                        .addPlatformNotification(IosNotification.newBuilder()
                                .setAlert(content)
                                .build())
                        .build())
                .build();
        PushResult result = this.jPushClient.sendPush(payload);
        return result.toString();
    }

    @ConfigurationProperties(prefix = "jpush")
    static class JPushProperties {

        private final String masterSecret;

        private final String appKey;

        @ConstructorBinding
        public JPushProperties(String masterSecret, String appKey) {
            Objects.requireNonNull(masterSecret, "masterSecret must not be null");
            Objects.requireNonNull(appKey, "appKey must not be null");
            this.masterSecret = masterSecret;
            this.appKey = appKey;
        }

    }

}
