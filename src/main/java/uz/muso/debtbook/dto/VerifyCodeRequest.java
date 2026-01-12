package uz.muso.debtbook.dto;

import lombok.Data;

@Data
public class VerifyCodeRequest {

    private String phoneNumber;
    private String code;

    // faqat yangi user uchun
    private String shopName;
    private String shopPhone;
    private String shopAddress;

}
