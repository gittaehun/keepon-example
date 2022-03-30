package bside.keepon.coupon.etc;

import bside.keepon.ResultType;

public class CouponException extends RuntimeException{
    private ResultType resultType;

    public CouponException(ResultType resultType, String message) {
        super(message);
        this.resultType = resultType;
    }

    public ResultType getResultType() {
        return resultType;
    }
}
