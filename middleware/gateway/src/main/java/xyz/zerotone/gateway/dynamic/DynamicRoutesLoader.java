package xyz.zerotone.gateway.dynamic;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

// 我们需要在两个场景下加载配置文件，
// 一个是项目首次启动的时候，从 Nacos 读取文件用来初始化路由表；
// 另一个场景是当 Nacos 的配置项发生变化的时候，动态获取配置项。
// DynamicRoutesLoader 的类，它实现了 InitializingBean 接口，
// 后者是 Spring 框架提供的标准接口。它的作用是在当前类所有的属性加载完成后，
// 执行一段定义在 afterPropertiesSet 方法中的自定义逻辑。
@Slf4j
@Configuration
public class DynamicRoutesLoader implements InitializingBean {

    @Autowired
    private NacosConfigManager configService;

    @Autowired
    private NacosConfigProperties configProps;

    @Autowired
    private DynamicRoutesListener dynamicRoutesListener;

    private static final String ROUTES_CONFIG = "routes-config.json";

    @Override
    public void afterPropertiesSet() throws Exception {
        String routes = configService.getConfigService().getConfig(
                ROUTES_CONFIG, configProps.getGroup(), 10000);
        // 调用 Nacos 提供的 NacosConfigManager 类加载指定的路由配置文件，配置文件名是 routes-config.json；
        dynamicRoutesListener.receiveConfigInfo(routes);

        // 注册到 routes-config.json 文件的监听列表中，这样一来，每次这个文件发生变动，监听器都能够获取到通知
        configService.getConfigService().addListener(ROUTES_CONFIG,
                configProps.getGroup(),
                dynamicRoutesListener);
    }

}
