package com.adrvil.wealthcheck.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyNetRes(
        LocalDate date,
        BigDecimal net
) {
}
