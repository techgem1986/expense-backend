cat > system-design.md << 'EOF'
# Personal Expense Management System - System Design

## 🧠 Overview
This system is designed to track income, expenses, budgets, and analytics using a scalable microservices architecture with a React frontend.

---

## 🧩 High-Level Architecture

                        ┌──────────────────────────────┐
                        │        React Frontend        │
                        │ (TypeScript + React Query)   │
                        └──────────────┬───────────────┘
                                       │ HTTPS (REST/GraphQL)
                                       ▼
                        ┌──────────────────────────────┐
                        │        API Gateway           │
                        │  (Routing, Auth, Rate Limit) │
                        └──────────────┬───────────────┘
                                       │
        ┌──────────────────────────────┼──────────────────────────────┐
        │                              │                              │
        ▼                              ▼                              ▼

┌─────────────────────┐   ┌─────────────────────┐   ┌─────────────────────┐
│   User Service      │   │ Transaction Service │   │   Budget Service    │
│---------------------│   │---------------------│   │---------------------│
│ - Auth (JWT)        │   │ - Income            │   │ - Set budgets       │
│ - Profile           │   │ - Expenses          │   │ - Track limits      │
│ - Preferences       │   │ - Categories        │   │ - Alerts trigger    │
└─────────┬───────────┘   │ - Recurring txns    │   └─────────┬───────────┘
          │               └─────────┬───────────┘             │
          │                         │                         │
          ▼                         ▼                         ▼

                 ┌──────────────────────────────────────┐
                 │          Event Bus (Async)           │
                 │     (Kafka / RabbitMQ later)         │
                 └──────────────┬───────────────────────┘
                                │
        ┌───────────────────────┼────────────────────────┐
        │                       │                        │
        ▼                       ▼                        ▼

┌─────────────────────┐  ┌─────────────────────┐  ┌─────────────────────┐
│ Analytics Service   │  │ Notification Service│  │   File Service      │
│---------------------│  │---------------------│  │---------------------│
│ - Reports           │  │ - Email alerts      │  │ - Receipt uploads   │
│ - Trends            │  │ - Budget warnings   │  │ - Storage (S3)      │
│ - Aggregations      │  │ - Reminders         │  │                     │
└─────────┬───────────┘  └─────────┬───────────┘  └─────────┬───────────┘
          │                        │                        │
          ▼                        ▼                        ▼

        ┌────────────────────────────────────────────────────────────┐
        │                     Data Layer                             │
        │------------------------------------------------------------│
        │  PostgreSQL (Primary DB)                                   │
        │   - Users                                                  │
        │   - Transactions                                           │
        │   - Budgets                                                │
        │   - Categories                                             │
        │                                                            │
        │  Redis (Optional Cache)                                    │
        │   - Session cache                                          │
        │   - Analytics cache                                        │
        │                                                            │
        │  Object Storage (S3/MinIO)                                 │
        │   - Receipts / files                                       │
        └────────────────────────────────────────────────────────────┘

---

## 🔄 Key Flows

### Add Expense Flow
User → React UI → API Gateway → Transaction Service  
→ Save in DB  
→ Publish Event: "ExpenseCreated"

Event Bus →  
→ Analytics Service updates reports  
→ Budget Service checks limits  
→ Notification Service sends alert  

---

### Dashboard Load Flow
React → API Gateway → Analytics Service  
→ Fetch summaries → Return charts  

---

### Budget Alert Flow
Expense Added → Budget exceeded → Notification sent  

---

## 🚀 Evolution Plan

Phase 1: Modular Monolith  
Phase 2: Split Services  
Phase 3: Add Event Bus  
Phase 4: Full Microservices + Kubernetes  

---

## ✅ Summary

- Scalable architecture  
- Cost-efficient start  
- Microservices-ready design  
- Event-driven future  
EOF