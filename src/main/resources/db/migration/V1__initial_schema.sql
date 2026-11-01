-- ============================================================================
-- V1 - Initial schema for the Expense Management Service (PostgreSQL)
-- ============================================================================
-- Single source of truth for the database schema. Consolidated from the
-- previous ad-hoc script sets (docker-entrypoint-initdb.d, Liquibase DDL).
--
-- The script is fully idempotent (IF NOT EXISTS everywhere) so it can also be
-- applied safely to a pre-existing database that was created by the old
-- docker-entrypoint-initdb.d / Hibernate ddl-auto scripts and then baselined
-- by Flyway (see spring.flyway.baseline-on-migrate).
--
-- Column set / types / nullability match the JPA entities:
--   users, categories, accounts, transactions, recurring_transactions,
--   budgets, budget_categories, alerts
-- ============================================================================

-- ---------------------------------------------------------------------------
-- TABLE: users
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id              BIGSERIAL       PRIMARY KEY,
    email           VARCHAR(255)    NOT NULL UNIQUE,
    password_hash   VARCHAR(255)    NOT NULL,
    first_name      VARCHAR(100),
    last_name       VARCHAR(100),
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------------
-- TABLE: categories (user-specific; unique per (name, user_id))
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS categories (
    id              BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL,
    description     TEXT,
    type            VARCHAR(20)     NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
    user_id         BIGINT          NOT NULL REFERENCES users(id),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_categories_name_user UNIQUE (name, user_id)
);

-- ---------------------------------------------------------------------------
-- TABLE: accounts
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS accounts (
    id                BIGSERIAL       PRIMARY KEY,
    user_id           BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name              VARCHAR(255)    NOT NULL,
    account_type      VARCHAR(50)     NOT NULL
                        CHECK (account_type IN ('SAVINGS','SALARY','CURRENT','INVESTMENT','MUTUAL_FUND','CREDIT_CARD')),
    bank_name         VARCHAR(255),
    account_number    VARCHAR(100),
    ifsc_code         VARCHAR(20),
    opening_balance   DECIMAL(15,2)   NOT NULL DEFAULT 0.00 CHECK (opening_balance >= 0),
    current_balance   DECIMAL(15,2)   NOT NULL DEFAULT 0.00 CHECK (current_balance >= 0),
    description       TEXT,
    is_active         BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------------
-- TABLE: transactions
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS transactions (
    id                              BIGSERIAL    PRIMARY KEY,
    user_id                         BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id                     BIGINT       REFERENCES categories(id),
    amount                          DECIMAL(15,2) NOT NULL CHECK (amount > 0),
    type                            VARCHAR(20)  NOT NULL CHECK (type IN ('INCOME', 'EXPENSE', 'TRANSFER')),
    description                     TEXT,
    transaction_date                DATE         NOT NULL,
    is_recurring_instance           BOOLEAN      NOT NULL DEFAULT FALSE,
    linked_recurring_transaction_id BIGINT,
    from_account_id                 BIGINT       REFERENCES accounts(id) ON DELETE SET NULL,
    to_account_id                   BIGINT       REFERENCES accounts(id) ON DELETE SET NULL,
    created_at                      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------------
-- TABLE: recurring_transactions
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS recurring_transactions (
    id                  BIGSERIAL    PRIMARY KEY,
    user_id             BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id         BIGINT       REFERENCES categories(id),
    name                VARCHAR(255) NOT NULL,
    amount              DECIMAL(15,2) NOT NULL CHECK (amount > 0),
    type                VARCHAR(20)  NOT NULL CHECK (type IN ('INCOME', 'EXPENSE', 'TRANSFER')),
    description         TEXT,
    frequency           VARCHAR(20)  NOT NULL DEFAULT 'MONTHLY' CHECK (frequency IN ('MONTHLY')),
    day_of_month        INTEGER      NOT NULL CHECK (day_of_month BETWEEN 1 AND 31),
    start_date          DATE         NOT NULL,
    end_date            DATE,
    next_execution_date DATE         NOT NULL,
    is_active           BOOLEAN      NOT NULL DEFAULT TRUE,
    from_account_id     BIGINT       REFERENCES accounts(id),
    to_account_id       BIGINT       REFERENCES accounts(id),
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------------
-- TABLE: budgets
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS budgets (
    id                BIGSERIAL       PRIMARY KEY,
    user_id           BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name              VARCHAR(255)    NOT NULL,
    limit_amount      DECIMAL(15,2)   NOT NULL CHECK (limit_amount > 0),
    period            VARCHAR(20)     NOT NULL CHECK (period IN ('MONTHLY', 'YEARLY')),
    alert_threshold   DECIMAL(5,2)    NOT NULL DEFAULT 0.80 CHECK (alert_threshold BETWEEN 0.01 AND 1.00),
    start_date        DATE            NOT NULL,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------------
-- TABLE: budget_categories (junction; JPA entity BudgetCategory owns an id)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS budget_categories (
    id              BIGSERIAL       PRIMARY KEY,
    budget_id       BIGINT          NOT NULL REFERENCES budgets(id) ON DELETE CASCADE,
    category_id     BIGINT          NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    limit_amount    DECIMAL(15,2),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------------
-- TABLE: alerts
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS alerts (
    id                    BIGSERIAL    PRIMARY KEY,
    user_id               BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type                  VARCHAR(50)  NOT NULL,
    message               TEXT         NOT NULL,
    related_entity_type   VARCHAR(50),
    related_entity_id     BIGINT,
    is_read               BOOLEAN      NOT NULL DEFAULT FALSE,
    read_at               TIMESTAMP WITH TIME ZONE,
    created_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ===========================================================================
-- INDEXES
-- ===========================================================================
CREATE INDEX IF NOT EXISTS idx_transactions_user_id      ON transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_user_date    ON transactions(user_id, transaction_date);
CREATE INDEX IF NOT EXISTS idx_transactions_category     ON transactions(category_id);
CREATE INDEX IF NOT EXISTS idx_transactions_recurring    ON transactions(is_recurring_instance, linked_recurring_transaction_id);
CREATE INDEX IF NOT EXISTS idx_transactions_from_account ON transactions(from_account_id);
CREATE INDEX IF NOT EXISTS idx_transactions_to_account   ON transactions(to_account_id);

CREATE INDEX IF NOT EXISTS idx_recurring_transactions_user           ON recurring_transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_recurring_transactions_next_execution ON recurring_transactions(next_execution_date);
CREATE INDEX IF NOT EXISTS idx_recurring_transactions_active         ON recurring_transactions(is_active);

CREATE INDEX IF NOT EXISTS idx_budgets_user              ON budgets(user_id);
CREATE INDEX IF NOT EXISTS idx_budgets_user_period       ON budgets(user_id, period);

CREATE INDEX IF NOT EXISTS idx_alerts_user               ON alerts(user_id);
CREATE INDEX IF NOT EXISTS idx_alerts_user_unread        ON alerts(user_id, is_read);

CREATE INDEX IF NOT EXISTS idx_accounts_user             ON accounts(user_id);
CREATE INDEX IF NOT EXISTS idx_accounts_user_active      ON accounts(user_id, is_active);
CREATE INDEX IF NOT EXISTS idx_accounts_user_type        ON accounts(user_id, account_type);

CREATE INDEX IF NOT EXISTS idx_categories_user_id        ON categories(user_id);

-- ===========================================================================
-- GRANTS - grant full privileges on application objects to the connecting role
-- (no-op when the connecting role already owns the objects, which is the case
--  when Flyway runs as the application datasource user)
-- ===========================================================================
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN
        SELECT tablename
        FROM pg_tables
        WHERE schemaname = 'public'
          AND tablename NOT LIKE 'flyway_schema_history%'
    LOOP
        EXECUTE format('GRANT ALL PRIVILEGES ON TABLE public.%I TO CURRENT_USER', r.tablename);
    END LOOP;
END $$;

DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN
        SELECT sequence_name
        FROM information_schema.sequences
        WHERE sequence_schema = 'public'
    LOOP
        EXECUTE format('GRANT ALL PRIVILEGES ON SEQUENCE public.%I TO CURRENT_USER', r.sequence_name);
    END LOOP;
END $$;
