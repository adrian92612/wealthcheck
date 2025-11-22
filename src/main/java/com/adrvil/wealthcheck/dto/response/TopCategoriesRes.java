package com.adrvil.wealthcheck.dto.response;

import java.util.List;

public record TopCategoriesRes(
        List<CategoryPieRes> topIncomeCategories,
        List<CategoryPieRes> topExpenseCategories
) {
}
