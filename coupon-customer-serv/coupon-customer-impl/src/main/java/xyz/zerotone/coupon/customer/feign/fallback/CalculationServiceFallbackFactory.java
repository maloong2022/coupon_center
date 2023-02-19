package xyz.zerotone.coupon.customer.feign.fallback;


import xyz.zerotone.coupon.calculation.api.beans.ShoppingCart;
import xyz.zerotone.coupon.calculation.api.beans.SimulationOrder;
import xyz.zerotone.coupon.calculation.api.beans.SimulationResponse;
import xyz.zerotone.coupon.customer.feign.CalculationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

//想要在降级方法中获取到异常的具体原因，
//那么你就要借助 fallback 工厂的方式来指定降级逻辑了
@Slf4j
@Component
public class CalculationServiceFallbackFactory implements FallbackFactory<CalculationService> {

    @Override
    public CalculationService create(Throwable cause) {
        // 使用这种方法你可以捕捉到具体的异常cause
        return new CalculationService() {

            @Override
            public ShoppingCart checkout(ShoppingCart settlement) {
                log.info("fallback factory method test");
                return null;
            }

            @Override
            public SimulationResponse simulate(SimulationOrder simulator) {
                log.info("fallback factory method test");
                return null;
            }
        };
    }
}