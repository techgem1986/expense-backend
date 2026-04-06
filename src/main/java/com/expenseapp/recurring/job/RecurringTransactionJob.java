package com.expenseapp.recurring.job;

import com.expenseapp.recurring.domain.RecurringTransaction;
import com.expenseapp.recurring.domain.RecurringTransaction.Frequency;
import com.expenseapp.recurring.service.RecurringTransactionService;
import com.expenseapp.transaction.domain.Transaction;
import com.expenseapp.transaction.service.TransactionService;
import com.expenseapp.user.domain.User;
import com.expenseapp.user.service.UserService;
import com.expenseapp.recurring.event.RecurringTransactionGeneratedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Scheduled job for processing recurring transactions.
 * Runs daily at 2:00 AM to generate any due recurring transactions.
 */
@Component
public class RecurringTransactionJob {

    private static final Logger log = LoggerFactory.getLogger(RecurringTransactionJob.class);

    @Autowired
    private RecurringTransactionService recurringTransactionService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserService userService;

    /**
     * Scheduled task to process recurring transactions.
     * Runs daily at 2:00 AM.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void processRecurringTransactions() {
        log.info("Starting recurring transaction processing for date: {}", LocalDate.now());
        
        try {
            List<RecurringTransaction> dueTransactions = recurringTransactionService.getDueRecurringTransactions(LocalDate.now());
            
            log.info("Found {} recurring transactions due for processing", dueTransactions.size());
            
            for (RecurringTransaction recurringTransaction : dueTransactions) {
                try {
                    processRecurringTransaction(recurringTransaction);
                } catch (Exception e) {
                    log.error("Failed to process recurring transaction: {}", recurringTransaction.getId(), e);
                }
            }
            
            log.info("Completed recurring transaction processing");
            
        } catch (Exception e) {
            log.error("Error during recurring transaction processing", e);
        }
    }

    private void processRecurringTransaction(RecurringTransaction recurringTransaction) {
        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();
        int currentMonth = today.getMonthValue();
        
        // Check if a transaction already exists for this recurring transaction in the current month
        boolean alreadyExists = transactionService.existsByRecurringTransactionIdAndMonth(
            recurringTransaction.getId(), currentYear, currentMonth);
        
        if (alreadyExists) {
            log.info("Skipping recurring transaction '{}' - already generated for this month ({}-{})", 
                recurringTransaction.getName(), currentYear, currentMonth);
            
            // Still update the next execution date to prevent repeated checks
            recurringTransactionService.updateNextExecutionDate(recurringTransaction.getId(), 
                calculateNextExecutionDate(recurringTransaction));
            return;
        }
        
        // Create a new transaction from the recurring template
        Transaction generatedTransaction = new Transaction();
        generatedTransaction.setUser(recurringTransaction.getUser());
        generatedTransaction.setCategory(recurringTransaction.getCategory());
        generatedTransaction.setAmount(recurringTransaction.getAmount());
        generatedTransaction.setType(com.expenseapp.transaction.domain.Transaction.TransactionType.valueOf(recurringTransaction.getType().name()));
        generatedTransaction.setDescription(recurringTransaction.getDescription());
        generatedTransaction.setTransactionDate(today);
        generatedTransaction.setIsRecurringInstance(true);
        generatedTransaction.setLinkedRecurringTransactionId(recurringTransaction.getId());
        
        // Set account information for money transfers
        generatedTransaction.setFromAccount(recurringTransaction.getFromAccount());
        generatedTransaction.setToAccount(recurringTransaction.getToAccount());

        // Save the generated transaction
        Transaction savedTransaction = transactionService.createTransaction(generatedTransaction);

        // Update the next execution date for the recurring transaction
        recurringTransactionService.updateNextExecutionDate(recurringTransaction.getId(), calculateNextExecutionDate(recurringTransaction));

        // Publish event for the generated transaction
        RecurringTransactionGeneratedEvent event = new RecurringTransactionGeneratedEvent(
            recurringTransaction, savedTransaction, today
        );

        log.info("Generated recurring transaction: {} - Amount: ₹{}, User: {}", 
            recurringTransaction.getName(), 
            recurringTransaction.getAmount(), 
            recurringTransaction.getUser().getEmail());

        // TODO: Publish event to event bus for notifications, analytics, etc.
        // This would typically use an event publisher or message broker
    }

    private LocalDate calculateNextExecutionDate(RecurringTransaction recurringTransaction) {
        LocalDate startDate = recurringTransaction.getStartDate();
        Frequency frequency = recurringTransaction.getFrequency();
        Integer dayOfMonth = recurringTransaction.getDayOfMonth();
        LocalDate endDate = recurringTransaction.getEndDate();
        
        // For the next execution after the current one, we need to advance by one frequency period
        switch (frequency) {
            case MONTHLY:
                // Get the current next execution date and advance by one month
                LocalDate currentNext = recurringTransaction.getNextExecutionDate();
                if (currentNext != null) {
                    // Calculate for the month after current next execution
                    LocalDate nextMonth = currentNext.plusMonths(1);
                    int daysInNextMonth = nextMonth.lengthOfMonth();
                    int actualDay = Math.min(dayOfMonth != null ? dayOfMonth : currentNext.getDayOfMonth(), daysInNextMonth);
                    return nextMonth.withDayOfMonth(actualDay);
                } else {
                    // Fallback: calculate from start date
                    return recurringTransaction.calculateNextExecutionDate(startDate, frequency, dayOfMonth);
                }
            default:
                // Default to monthly behavior
                LocalDate nextExecution = recurringTransaction.getNextExecutionDate();
                if (nextExecution != null) {
                    return nextExecution.plusMonths(1);
                } else {
                    return recurringTransaction.calculateNextExecutionDate(startDate, frequency, dayOfMonth);
                }
        }
    }
}