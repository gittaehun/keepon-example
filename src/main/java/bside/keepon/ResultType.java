package bside.keepon;

public enum ResultType {
    signup_complete("10"), success("00"), fail("99"), no_mbr("98"), not_supported_sns("97"),
    duplicate_mbr("96"), withdrawed_mbr("93"), sns_error("95"), failed_to_find_avaliable_rsa("94")
    ,duplicate_coupon("89"),fail_img_server_upload("88"), fail_img_too_large("80"), fail_img_type("81");

    private String code;

    ResultType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
