package xyz.zerotone.coupon.template.converter;

import xyz.zerotone.coupon.template.api.beans.CouponTemplateInfo;
import xyz.zerotone.coupon.template.dao.entity.CouponTemplate;

public class CouponTemplateConverter {

    public static CouponTemplateInfo convertToTemplateInfo(CouponTemplate template) {

        return CouponTemplateInfo.builder()
                .id(template.getId())
                .name(template.getName())
                .desc(template.getDescription())
                .type(template.getCategory().getCode())
                .shopId(template.getShopId())
                .available(template.getAvailable())
                .rule(template.getRule())
                .build();
    }
}
