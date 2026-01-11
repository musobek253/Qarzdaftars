package uz.muso.debtbook.service.telegram;

import lombok.Data;

@Data
public class TelegramSession {
    private Long chatId;
    private BotState state = BotState.START;

    // Auth flow temporary data
    private String tempEmail;
    private String tempCode;
    private String tempShopName;

    // Customer flow
    private String tempCustomerName;
    private String tempCustomerPhone;

    // Debt flow
    private Long tempDebtCustomerId;
    private java.math.BigDecimal tempDebtAmount;
}
