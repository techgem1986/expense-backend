package com.expenseapp.transaction.event;

import com.expenseapp.transaction.domain.Transaction;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a new transaction is created.
 */
public class TransactionCreatedEvent extends ApplicationEvent {

    private final Transaction transaction;

    public TransactionCreatedEvent(Object source, Transaction transaction) {
        super(source);
        this.transaction = transaction;
    }

    public Transaction getTransaction() {
        return transaction;
    }
}