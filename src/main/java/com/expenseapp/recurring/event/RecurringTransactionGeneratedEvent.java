package com.expenseapp.recurring.event;

import com.expenseapp.recurring.domain.RecurringTransaction;
import com.expenseapp.transaction.domain.Transaction;
import java.time.LocalDate;

/**
 * Event published when a recurring transaction is automatically generated.
 */
public class RecurringTransactionGeneratedEvent {

    private final RecurringTransaction recurringTransaction;
    private final Transaction generatedTransaction;
    private final LocalDate executionDate;

    public RecurringTransactionGeneratedEvent(RecurringTransaction recurringTransaction, 
                                            Transaction generatedTransaction, 
                                            LocalDate executionDate) {
        this.recurringTransaction = recurringTransaction;
        this.generatedTransaction = generatedTransaction;
        this.executionDate = executionDate;
    }

    public RecurringTransaction getRecurringTransaction() {
        return recurringTransaction;
    }

    public Transaction getGeneratedTransaction() {
        return generatedTransaction;
    }

    public LocalDate getExecutionDate() {
        return executionDate;
    }

    public String getNotificationMessage() {
        String type = generatedTransaction.isIncome() ? "Income" : "Expense";
        return String.format("%s generated: %s - ₹%s (%s)", 
            type, 
            recurringTransaction.getName(), 
            generatedTransaction.getAmount(), 
            executionDate);
    }
}