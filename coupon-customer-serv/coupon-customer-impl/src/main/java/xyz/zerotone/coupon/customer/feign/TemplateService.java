package xyz.zerotone.coupon.customer.feign;


import xyz.zerotone.coupon.customer.feign.fallback.TemplateServiceFallback;
import xyz.zerotone.coupon.template.api.beans.CouponTemplateInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.Map;

//FeignClient 注解中的 path 属性是一个可选项，如果你要调用的目标服务有一个统一的前置访问路径，
// 比如 coupon-template-serv 所有接口的访问路径都以 /template 开头，那么你可以通过 path
// 属性来声明这个前置路径，这样一来，你就不用在每一个方法名上的注解中带上前置 Path 了
@FeignClient(value = "coupon-template-serv", path = "/template",
// 通过fallback指定降级逻辑
fallback = TemplateServiceFallback.class)
public interface TemplateService {
    // 读取优惠券
    @GetMapping("/getTemplate")
    CouponTemplateInfo getTemplate(@RequestParam("id") Long id);

    // 批量获取
    @GetMapping("/getBatch")
    Map<Long, CouponTemplateInfo> getTemplateInBatch(@RequestParam("ids") Collection<Long> ids);

    // 优惠券无效化
    @DeleteMapping("/deleteTemplate")
    void deleteTemplate(@RequestParam("id") Long id);

}