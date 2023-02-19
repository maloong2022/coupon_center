package xyz.zerotone.coupon.customer.feign.fallback;


import xyz.zerotone.coupon.customer.feign.TemplateService;
import xyz.zerotone.coupon.template.api.beans.CouponTemplateInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

@Slf4j
@Component
public class TemplateServiceFallback implements TemplateService {

    @Override
    public CouponTemplateInfo getTemplate(Long id) {
        log.info("fallback getTemplate");
        return null;
    }

    @Override
    public Map<Long, CouponTemplateInfo> getTemplateInBatch(Collection<Long> ids) {
        log.info("fallback getTemplateInBatch");
        return null;
    }

    @Override
    public void deleteTemplate(Long id) {
        log.info("fallback deleteTemplate");
    }
}