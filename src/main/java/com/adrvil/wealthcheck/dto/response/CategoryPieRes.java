package com.adrvil.wealthcheck.dto.response;

import java.math.BigDecimal;

public record CategoryPieRes(
        String name,
        BigDecimal amount
) {
}
