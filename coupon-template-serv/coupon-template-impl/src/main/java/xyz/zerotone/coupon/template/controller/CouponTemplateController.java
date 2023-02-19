package xyz.zerotone.coupon.template.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import xyz.zerotone.coupon.template.api.beans.CouponTemplateInfo;
import xyz.zerotone.coupon.template.api.beans.PagedCouponTemplateInfo;
import xyz.zerotone.coupon.template.api.beans.TemplateSearchParams;
import xyz.zerotone.coupon.template.service.intf.CouponTemplateService;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/template")
public class CouponTemplateController {

    @Autowired
    private CouponTemplateService couponTemplateService;

    // 创建优惠券
    @PostMapping("/addTemplate")
    public CouponTemplateInfo addTemplate(@Valid @RequestBody CouponTemplateInfo request) {
        log.info("Create coupon template: data={}", request);
        return couponTemplateService.createTemplate(request);
    }

    @PostMapping("/cloneTemplate")
    public CouponTemplateInfo cloneTemplate(@RequestParam("id") Long templateId) {
        log.info("Clone coupon template: data={}", templateId);
        return couponTemplateService.cloneTemplate(templateId);
    }

    // 读取优惠券
    @GetMapping("/getTemplate")
    @SentinelResource(value = "getTemplate")
    public CouponTemplateInfo getTemplate(@RequestParam("id") Long id) {
        log.info("Load template, id={}", id);
        return couponTemplateService.loadTemplateInfo(id);
    }

    // 批量获取
    @GetMapping("/getBatch")
    @SentinelResource(value = "getTemplateInBatch",
            fallback = "getTemplateInBatch_fallback",
            blockHandler = "getTemplateInBatch_block")
    public Map<Long, CouponTemplateInfo> getTemplateInBatch(@RequestParam("ids") Collection<Long> ids) {
        log.info("getTemplateInBatch: {}", JSON.toJSONString(ids));
        // 可以测试异常比例、异常数熔断
//        if (ids.size() == 2) {
//            throw new RuntimeException("异常");
//        }
        // 可以测试慢调用熔断
//        try {
//            Thread.sleep(500 * ids.size());
//        } catch (Exception e) {
//        }
        return couponTemplateService.getTemplateInfoMap(ids);
    }

    // 什么是 BlockException 呢？这个异常类是 Sentinel 组件自带的类，当一个请求被 Sentinel 规则拦截，
    // 这个异常便会被抛出。比如请求被 Sentinel 流控策略阻拦住，或者请求被熔断策略阻断了，
    // 这些情况下你可以使用 SentinelResource 的 blockHandler 注解来指定降级逻辑。
    // 但是对于其它 RuntimeException 的异常类型它就无能为力了。
    public Map getTemplateInBatch_block(Collection ids, BlockException exception) {
        log.info("接口被限流");
        return Maps.newHashMap();
    }

    // 如何指定一段通用的降级逻辑，来应对 BlockException 以外的 RuntimeException 呢？
    // 你可以使用 SentinelResource 中的另一个属性：fallback。
    // 这里你需要注意，如果降级方法的方法签名是 BlockException，那么 fallback 是无法正常工作的。
    // 这点和 blockHandler 属性的用法是不一样的。我在注解中同时使用了 fallback 和 blockHandler 属性，
    // 如果服务抛出 BlockException，则执行 blockHandler 属性指定的方法，
    // 其他异常就由 fallback 属性所对应的降级方法接管。
    // 接口被降级时的方法
    public Map getTemplateInBatch_fallback(Collection ids) {
        log.info("接口被降级");
        return Maps.newHashMap();
    }

    // 搜索模板
    @PostMapping("/search")
    public PagedCouponTemplateInfo search(@Valid @RequestBody TemplateSearchParams request) {
        log.info("search templates, payload={}", request);
        return couponTemplateService.search(request);
    }

    // 优惠券无效化
    @DeleteMapping("/deleteTemplate")
    public void deleteTemplate(@RequestParam("id") Long id) {
        log.info("Load template, id={}", id);
        couponTemplateService.deleteTemplate(id);
    }
}
