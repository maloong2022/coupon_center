package xyz.zerotone.coupon.customer.feign;


import xyz.zerotone.coupon.calculation.api.beans.ShoppingCart;
import xyz.zerotone.coupon.calculation.api.beans.SimulationOrder;
import xyz.zerotone.coupon.calculation.api.beans.SimulationResponse;
import xyz.zerotone.coupon.customer.feign.fallback.CalculationServiceFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "coupon-calculation-serv", path = "/calculator",// 通过抽象工厂来定义降级逻辑
fallbackFactory = CalculationServiceFallbackFactory.class)
public interface CalculationService {

    // 订单结算
    @PostMapping("/checkout")
    ShoppingCart checkout(ShoppingCart settlement);

    // 优惠券试算
    @PostMapping("/simulate")
    SimulationResponse simulate(SimulationOrder simulator);
}