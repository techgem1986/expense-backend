package com.expenseapp.shared.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Service for recording custom business metrics to Prometheus via Micrometer.
 * All metrics are prefixed with "expense." for easy identification.
 */
@Service
public class MonitoringMetricsService {

    // --- Transaction Counters ---
    private final Counter transactionCreatedCounter;
    private final Counter transactionDeletedCounter;
    private final Counter transactionIncomeCounter;
    private final Counter transactionExpenseCounter;
    private final Counter transactionTransferCounter;

    // --- Budget Counters ---
    private final Counter budgetCreatedCounter;
    private final Counter budgetExceededCounter;
    private final Counter budgetAlertTriggeredCounter;

    // --- Account Counters ---
    private final Counter accountCreatedCounter;
    private final Counter accountDeletedCounter;

    // --- User Counters ---
    private final Counter userRegisteredCounter;
    private final Counter userLoginCounter;

    // --- Timers ---
    private final Timer transactionCreationTimer;
    private final Timer transactionQueryTimer;
    private final Timer budgetCalculationTimer;

    // --- Gauges (tracked values) ---
    private final AtomicReference<Double> totalIncomeGauge;
    private final AtomicReference<Double> totalExpensesGauge;
    private final AtomicReference<Double> totalTransactionsGauge;
    private final AtomicReference<Double> activeBudgetsGauge;
    private final AtomicReference<Double> activeAccountsGauge;
    private final AtomicReference<Double> registeredUsersGauge;

    public MonitoringMetricsService(MeterRegistry meterRegistry) {
        // Initialize counters
        this.transactionCreatedCounter = Counter.builder("expense.transaction.created.total")
                .description("Total number of transactions created")
                .register(meterRegistry);

        this.transactionDeletedCounter = Counter.builder("expense.transaction.deleted.total")
                .description("Total number of transactions deleted")
                .register(meterRegistry);

        this.transactionIncomeCounter = Counter.builder("expense.transaction.income.total")
                .description("Total number of income transactions")
                .register(meterRegistry);

        this.transactionExpenseCounter = Counter.builder("expense.transaction.expense.total")
                .description("Total number of expense transactions")
                .register(meterRegistry);

        this.transactionTransferCounter = Counter.builder("expense.transaction.transfer.total")
                .description("Total number of transfer transactions")
                .register(meterRegistry);

        this.budgetCreatedCounter = Counter.builder("expense.budget.created.total")
                .description("Total number of budgets created")
                .register(meterRegistry);

        this.budgetExceededCounter = Counter.builder("expense.budget.exceeded.total")
                .description("Total number of times budgets were exceeded")
                .register(meterRegistry);

        this.budgetAlertTriggeredCounter = Counter.builder("expense.budget.alert.triggered.total")
                .description("Total number of budget alert thresholds triggered")
                .register(meterRegistry);

        this.accountCreatedCounter = Counter.builder("expense.account.created.total")
                .description("Total number of accounts created")
                .register(meterRegistry);

        this.accountDeletedCounter = Counter.builder("expense.account.deleted.total")
                .description("Total number of accounts soft-deleted")
                .register(meterRegistry);

        this.userRegisteredCounter = Counter.builder("expense.user.registered.total")
                .description("Total number of user registrations")
                .register(meterRegistry);

        this.userLoginCounter = Counter.builder("expense.user.login.total")
                .description("Total number of user logins")
                .register(meterRegistry);

        // Initialize timers
        this.transactionCreationTimer = Timer.builder("expense.transaction.creation.duration")
                .description("Time taken to create a transaction")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(meterRegistry);

        this.transactionQueryTimer = Timer.builder("expense.transaction.query.duration")
                .description("Time taken to query transactions")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(meterRegistry);

        this.budgetCalculationTimer = Timer.builder("expense.budget.calculation.duration")
                .description("Time taken to calculate budget spending")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(meterRegistry);

        // Initialize gauges (atomic references)
        this.totalIncomeGauge = new AtomicReference<>(0.0);
        meterRegistry.gauge("expense.transaction.income.amount", totalIncomeGauge,
                AtomicReference::get);

        this.totalExpensesGauge = new AtomicReference<>(0.0);
        meterRegistry.gauge("expense.transaction.expense.amount", totalExpensesGauge,
                AtomicReference::get);

        this.totalTransactionsGauge = new AtomicReference<>(0.0);
        meterRegistry.gauge("expense.transaction.count.active", totalTransactionsGauge,
                AtomicReference::get);

        this.activeBudgetsGauge = new AtomicReference<>(0.0);
        meterRegistry.gauge("expense.budget.count.active", activeBudgetsGauge,
                AtomicReference::get);

        this.activeAccountsGauge = new AtomicReference<>(0.0);
        meterRegistry.gauge("expense.account.count.active", activeAccountsGauge,
                AtomicReference::get);

        this.registeredUsersGauge = new AtomicReference<>(0.0);
        meterRegistry.gauge("expense.user.count.registered", registeredUsersGauge,
                AtomicReference::get);
    }

    // ====================
    // Transaction Metrics
    // ====================

    public void recordTransactionCreated(String transactionType) {
        transactionCreatedCounter.increment();
        switch (transactionType.toUpperCase()) {
            case "INCOME" -> transactionIncomeCounter.increment();
            case "EXPENSE" -> transactionExpenseCounter.increment();
            case "TRANSFER" -> transactionTransferCounter.increment();
        }
    }

    public void recordTransactionDeleted() {
        transactionDeletedCounter.increment();
    }

    public Timer.Sample startTransactionCreationTimer() {
        return Timer.start();
    }

    public void stopTransactionCreationTimer(Timer.Sample sample) {
        sample.stop(transactionCreationTimer);
    }

    public Timer.Sample startTransactionQueryTimer() {
        return Timer.start();
    }

    public void stopTransactionQueryTimer(Timer.Sample sample) {
        sample.stop(transactionQueryTimer);
    }

    public void updateTotalIncomeAmount(BigDecimal amount) {
        totalIncomeGauge.set(amount.doubleValue());
    }

    public void updateTotalExpensesAmount(BigDecimal amount) {
        totalExpensesGauge.set(amount.doubleValue());
    }

    public void updateActiveTransactionCount(long count) {
        totalTransactionsGauge.set((double) count);
    }

    // ====================
    // Budget Metrics
    // ====================

    public void recordBudgetCreated() {
        budgetCreatedCounter.increment();
    }

    public void recordBudgetExceeded() {
        budgetExceededCounter.increment();
    }

    public void recordBudgetAlertTriggered() {
        budgetAlertTriggeredCounter.increment();
    }

    public Timer.Sample startBudgetCalculationTimer() {
        return Timer.start();
    }

    public void stopBudgetCalculationTimer(Timer.Sample sample) {
        sample.stop(budgetCalculationTimer);
    }

    public void updateActiveBudgetCount(long count) {
        activeBudgetsGauge.set((double) count);
    }

    // ====================
    // Account Metrics
    // ====================

    public void recordAccountCreated() {
        accountCreatedCounter.increment();
    }

    public void recordAccountDeleted() {
        accountDeletedCounter.increment();
    }

    public void updateActiveAccountCount(long count) {
        activeAccountsGauge.set((double) count);
    }

    // ====================
    // User Metrics
    // ====================

    public void recordUserRegistered() {
        userRegisteredCounter.increment();
    }

    public void recordUserLogin() {
        userLoginCounter.increment();
    }

    public void updateRegisteredUserCount(long count) {
        registeredUsersGauge.set((double) count);
    }
}