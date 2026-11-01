package com.expenseapp.account.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTypeTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @ParameterizedTest
    @CsvSource({
            "SALARY, SALARY",
            "salary, SALARY",
            "Salary, SALARY",
            "  Salary  , SALARY",
            "SAVINGS, SAVINGS",
            "Savings, SAVINGS",
            "CURRENT, CURRENT",
            "INVESTMENT, INVESTMENT",
            "MUTUAL_FUND, MUTUAL_FUND",
            "mutual_fund, MUTUAL_FUND",
            "Mutual Funds, MUTUAL_FUND",
            "MUTUAL_FUNDS, MUTUAL_FUND",
            "CREDIT_CARD, CREDIT_CARD",
            "credit-cards, CREDIT_CARD",
            "Credit Cards, CREDIT_CARD",
    })
    void fromValueAcceptsConstantNamesDisplayNamesAndVariants(String raw, AccountType expected) {
        assertThat(AccountType.fromValue(raw)).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void fromValueReturnsNullForBlankInput(String raw) {
        assertThat(AccountType.fromValue(raw)).isNull();
    }

    @Test
    void fromValueReturnsNullForNullInput() {
        assertThat(AccountType.fromValue(null)).isNull();
    }

    @Test
    void fromValueRejectsUnknownValue() {
        assertThatThrownBy(() -> AccountType.fromValue("NOT_A_TYPE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown account type");
    }

    @Test
    void serializesToTheStableEnumName() throws Exception {
        assertThat(mapper.writeValueAsString(AccountType.SALARY)).isEqualTo("\"SALARY\"");
        assertThat(mapper.writeValueAsString(AccountType.MUTUAL_FUND)).isEqualTo("\"MUTUAL_FUND\"");
    }

    @Test
    void deserializesFromBothTheEnumNameAndTheDisplayName() throws Exception {
        assertThat(mapper.readValue("\"SALARY\"", AccountType.class)).isEqualTo(AccountType.SALARY);
        assertThat(mapper.readValue("\"Salary\"", AccountType.class)).isEqualTo(AccountType.SALARY);
        assertThat(mapper.readValue("\"Mutual Funds\"", AccountType.class))
                .isEqualTo(AccountType.MUTUAL_FUND);
    }
}
