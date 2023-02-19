package xyz.zerotone.gateway.dynamic;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
public class GatewayService {

    @Autowired
    private RouteDefinitionWriter routeDefinitionWriter;

    @Autowired
    private ApplicationEventPublisher publisher;

    // RouteDefinition List 对象,它是 Gateway 网关组件用来封装路由规则的标准类，
    // 在里面包含了谓词、过滤器和 metadata 等一系列构造路由规则所需要的元素。
    // 在主体逻辑部分，我调用了 Gateway 内置的路由编辑类 RouteDefinitionWriter，
    // 将路由规则写入上下文，再调用 ApplicationEventPublisher 类发布一个路由刷新事件。
    public void updateRoutes(List<RouteDefinition> routes) {
        if (CollectionUtils.isEmpty(routes)) {
            log.info("No routes found");
            return;
        }

        routes.forEach(r -> {
            try {
                routeDefinitionWriter.save(Mono.just(r)).subscribe();
                // 为了防止启动的时候没有路由报错，采用先更新后看时候需要删除的方式
                if (r.getMetadata() != null && r.getMetadata().containsKey("delete") && (boolean) r.getMetadata().get("delete")) {
                    routeDefinitionWriter.delete(Mono.just(r.getId())).subscribe();
                }
                publisher.publishEvent(new RefreshRoutesEvent(this));
            } catch (Exception e) {
                log.error("cannot update route, id={}", r.getId());
            }
        });
    }

}
