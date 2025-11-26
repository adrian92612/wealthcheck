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
    OVERVIEW("overview"),
    DELETED_USER_CATEGORIES("deleted-user-categories"),
    DELETED_USER_WALLETS("deleted-user-wallets"),
    DELETED_USER_TRANSACTIONS("deleted-user-transactions"),
    DAILY_NET("daily-net"),
    TOP_CATEGORIES("top-categories"),
    MONEY_GOAL("money-goal"),
    MONEY_BUDGET("money-budget"),
    ;
    private final String value;
}
