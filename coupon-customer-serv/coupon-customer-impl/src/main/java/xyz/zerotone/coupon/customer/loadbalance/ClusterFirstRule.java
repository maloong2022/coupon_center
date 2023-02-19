package xyz.zerotone.coupon.customer.loadbalance;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.SelectedInstanceCallback;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.core.env.Environment;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@Slf4j
public class ClusterFirstRule implements ReactorServiceInstanceLoadBalancer {
    private ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
    private String serviceId;
    private Environment environment;
    // 定义一个轮询策略的种子
    final AtomicInteger position;

    public ClusterFirstRule(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider, String serviceId, Environment environment) {
        this.environment = environment;
        this.serviceId = serviceId;
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        this.position = new AtomicInteger(new Random().nextInt(1000));
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider.getIfAvailable(NoopServiceInstanceListSupplier::new);

        return supplier.get(request).next().map(serviceInstances -> processInstanceResponse(supplier, serviceInstances));
    }

    private Response<ServiceInstance> processInstanceResponse(ServiceInstanceListSupplier supplier, List<ServiceInstance> serviceInstances) {
        Response<ServiceInstance> serviceInstanceResponse = getInstanceResponse(serviceInstances);

        if (supplier instanceof SelectedInstanceCallback && serviceInstanceResponse.hasServer()) {
            ((SelectedInstanceCallback) supplier).selectedServiceInstance(serviceInstanceResponse.getServer());
        }
        return serviceInstanceResponse;
    }

    //集群优先
    private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> serviceInstances) {
        //  注册中心无可用实例 抛出异常
        if (CollectionUtils.isEmpty(serviceInstances)) {
            log.warn("No instance available {}", serviceId);
        }

        String clusterName = environment.resolvePlaceholders("${spring.cloud.nacos.discovery.cluster-name:}");
        List<ServiceInstance> instanceList = serviceInstances.stream().filter(v -> {
            Map<String, String> metadata = v.getMetadata();
            String serviceClusterName = metadata.get("nacos.cluster");
            return clusterName.equals(serviceClusterName);
        }).collect(Collectors.toList());
        //有同集群下服务 RoundRobin算法挑选
        if (CollectionUtils.isNotEmpty(instanceList)) {
            return getRoundRobinInstance(instanceList);
        } else {
            return getRoundRobinInstance(serviceInstances);
        }
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
