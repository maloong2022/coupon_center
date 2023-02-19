package xyz.zerotone.gateway.dynamic;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.config.listener.Listener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Executor;


@Slf4j
@Component
public class DynamicRoutesListener implements Listener {

    @Autowired
    private GatewayService gatewayService;

    @Override
    public Executor getExecutor() {
        log.info("getExecutor");
        return null;
    }

    // 使用JSON转换，将plain text变为RouteDefinition
    //Listener 接口是 Nacos Config 提供的标准监听器接口，
    // 当被监听的 Nacos 配置文件发生变化的时候，框架会自动调用
    // receiveConfigInfo 方法执行自定义逻辑。
    // 在这段方法里，我将接收到的文本对象 configInfo 转换成了 List类，
    // 并调用 GatewayService 完成路由表的更新。
    @Override
    public void receiveConfigInfo(String configInfo) {
        log.info("received routes changes {}", configInfo);

        List<RouteDefinition> definitionList = JSON.parseArray(configInfo, RouteDefinition.class);
        gatewayService.updateRoutes(definitionList);
    }
}
