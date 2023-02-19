package xyz.zerotone.coupon.calculation.template;


import xyz.zerotone.coupon.calculation.api.beans.ShoppingCart;

public interface RuleTemplate {

    // 计算优惠券
    ShoppingCart calculate(ShoppingCart settlement);
}
