package com.adrvil.wealthcheck.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CacheName {
    USER_CATEGORIES("user-categories"),
    CATEGORY("category"),
    USER_WALLETS("user-wallets"),
    WALLET("wallet"),
    USER_TRANSACTIONS("user-transactions"),
    TRANSACTION("transaction"),
    RECENT_TRANSACTIONS("recent-transactions"),
    TOP_TRANSACTIONS("top-transactions"),
    OVERVIEW("overview");

    private final String value;
}
