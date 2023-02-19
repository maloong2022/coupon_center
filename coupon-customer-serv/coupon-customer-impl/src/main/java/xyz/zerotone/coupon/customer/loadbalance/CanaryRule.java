package xyz.zerotone.coupon.customer.loadbalance;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.SelectedInstanceCallback;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.http.HttpHeaders;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;
import xyz.zerotone.coupon.customer.constant.Constant;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public class CanaryRule implements ReactorServiceInstanceLoadBalancer {
    private ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
    private String serviceId;
    // 定义一个轮询策略的种子
    final AtomicInteger position;

    public CanaryRule(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider, String serviceId) {
        this.serviceId = serviceId;
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        this.position = new AtomicInteger(new Random().nextInt(1000));
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider.getIfAvailable(NoopServiceInstanceListSupplier::new);

        return supplier.get(request).next().map(serviceInstances -> processInstanceResponse(supplier, serviceInstances, request));
    }

    private Response<ServiceInstance> processInstanceResponse(ServiceInstanceListSupplier supplier, List<ServiceInstance> serviceInstances, Request request) {
        Response<ServiceInstance> serviceInstanceResponse = getInstanceResponse(serviceInstances, request);

        if (supplier instanceof SelectedInstanceCallback && serviceInstanceResponse.hasServer()) {
            ((SelectedInstanceCallback) supplier).selectedServiceInstance(serviceInstanceResponse.getServer());
        }
        return serviceInstanceResponse;
    }

    private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> serviceInstances, Request request) {
        //  注册中心无可用实例 抛出异常
        if (CollectionUtils.isEmpty(serviceInstances)) {
            log.warn("No instance available {}", serviceId);
        }

        // 从请求Header中获取特定的流量打标值
        // 注意： 以下代码仅适合于WebClient调用，如果使用RestTemplate or Feign则需要额外适配
        DefaultRequestContext context = (DefaultRequestContext) request.getContext();
        RequestData requestData = (RequestData) context.getClientRequest();
        HttpHeaders headers = requestData.getHeaders();

        String trafficVersion = headers.getFirst(Constant.TRAFFIC_VERSION);

        //  如果没有找到打标标记，或者标记为空，则使用RoundRobin规则进行轮询
        if (StringUtils.isBlank(trafficVersion)) {
            // 过滤掉所有金丝雀测试的节点（Metadata有值的节点）
            List<ServiceInstance> noneCanaryInstances = serviceInstances.stream().filter(e -> !e.getMetadata().containsKey(Constant.TRAFFIC_VERSION)).collect(Collectors.toList());
            return getRoundRobinInstance(noneCanaryInstances);
        }

        // 找到所有金丝雀服务器，用RoundRobin算法挑出一台
        List<ServiceInstance> canaryInstances = serviceInstances.stream().filter(e -> {
            String trafficVersionInMetadata = e.getMetadata().get(Constant.TRAFFIC_VERSION);
            return StringUtils.equalsIgnoreCase(trafficVersionInMetadata, trafficVersion);
        }).collect(Collectors.toList());
        return getRoundRobinInstance(canaryInstances);
    }

    // 使用轮询机制获取节点
    private Response<ServiceInstance> getRoundRobinInstance(List<ServiceInstance> canaryInstances) {
        // 如果没有可用节点，则返回空
        if (canaryInstances.isEmpty()) {
            log.warn("No servers available for service: " + serviceId);
            return new EmptyResponse();
        }

        int pos = Math.abs(this.position.incrementAndGet());
        ServiceInstance instance = canaryInstances.get(pos % canaryInstances.size());
        return new DefaultResponse(instance);

    }
}
