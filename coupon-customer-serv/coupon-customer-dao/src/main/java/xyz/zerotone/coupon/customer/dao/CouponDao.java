package xyz.zerotone.coupon.customer.dao;

import xyz.zerotone.coupon.customer.api.enums.CouponStatus;
import xyz.zerotone.coupon.customer.dao.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouponDao extends JpaRepository<Coupon, Long> {

    long countByUserIdAndTemplateIdAndStatus(Long userId, Long templateId, CouponStatus status);

    @Modifying
    @Query("update Coupon c set c.status = :status where c.templateId = :templateId")
    int deleteCouponInBatch(@Param("templateId") Long templateId, @Param("status")CouponStatus status);

}
