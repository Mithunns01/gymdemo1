# Entity-Relationship Diagram

## Tables and Relationships

```
┌─────────────────────────────────────────────────────────────────────┐
│                           USERS                                       │
├─────────────────────────────────────────────────────────────────────┤
│ id (PK)         BIGINT                                              │
│ username         VARCHAR(50) UNIQUE                                  │
│ password         VARCHAR(255)                                        │
│ name             VARCHAR(100)                                        │
│ email            VARCHAR(100) UNIQUE                                 │
│ phone            VARCHAR(15)                                         │
│ role             ENUM(ADMIN,TRAINER,MEMBER)                          │
│ enabled          BOOLEAN                                             │
│ created_at       DATETIME                                            │
│ updated_at       DATETIME                                            │
└──────────┬──────────────────────────────────────────────────────┬────┘
           │                                                      │
           │ 1:1                                                   │ 1:1
           ▼                                                      ▼
┌──────────────────────┐                              ┌──────────────────────┐
│     TRAINERS         │                              │      MEMBERS         │
├──────────────────────┤                              ├──────────────────────┤
│ id (PK)              │                              │ id (PK)              │
│ user_id (FK) ────────┘                              │ user_id (FK) ──────┘
│ specialization       │                              │ gender               │
│ experience           │                              │ date_of_birth        │
│ bio                  │                              │ height               │
│ active               │                              │ weight               │
└──────────┬───────────┘                              │ bmi                  │
           │                                          │ emergency_contact    │
           │ 1:N                                       │ medical_conditions   │
           │                                          │ assigned_trainer_id──┤ FK ────┐
           │                                          │ active               │        │
           ▼                                          └──────┬───────────────┘        │
┌───────────────────────────────┐                           │                          │
│       WORKOUT_PLANS           │                           │                          │
├───────────────────────────────┤                           │ 1:N                       │
│ id (PK)                       │                           │                          │
│ member_id (FK) ───────────────┤──── FK                    │                          │
│ trainer_id (FK) ─────────────┤──── FK                     │                          │
│ title                         │                           ▼                          │
│ description                   │                  ┌───────────────────────────┐       │
│ exercises                     │                  │   MEMBER_MEMBERSHIPS      │       │
│ difficulty                    │                  ├───────────────────────────┤       │
│ duration_weeks                │                  │ id (PK)                   │       │
│ start_date                    │                  │ member_id (FK) ───────────┤── FK ─┘
│ end_date                      │                  │ plan_id (FK)  ───────────┤── FK
│ status                        │                  │ start_date                │
│ notes                         │                  │ end_date                  │
│ created_at                    │                  │ active                    │
│ updated_at                    │                  │ amount_paid               │
└───────────────────────────────┘                  │ payment_status            │
                                                   │ created_at                │
┌───────────────────────────────┐                  └──────┬────────────────────┘
│       MEMBERSHIP_PLANS        │                         │
├───────────────────────────────┤                         │ 1:N
│ id (PK)                       │                         │
│ name                          │                         ▼
│ description                   │                  ┌───────────────────────────┐
│ duration_days                 │                  │       PAYMENTS            │
│ price                         │                  ├───────────────────────────┤
│ active                        │                  │ id (PK)                   │
└───────────────────────────────┘                  │ member_id (FK) ───────────┤── FK
                                                   │ membership_id (FK) ──────┤── FK
                                                   │ amount                    │
                                                   │ payment_date              │
┌───────────────────────────────┐                  │ payment_mode              │
│       ATTENDANCE              │                  │ transaction_id            │
├───────────────────────────────┤                  │ status                    │
│ id (PK)                       │                  │ notes                     │
│ member_id (FK) ───────────────┤── FK             │ created_at                │
│ attendance_date               │                  └───────────────────────────┘
│ check_in_time                 │
│ status                        │
│ notes                         │
└───────────────────────────────┘

## Relationships Summary

1. **User → Trainer**: One-to-One (each user can be one trainer)
2. **User → Member**: One-to-One (each user can be one member)
3. **Trainer → Member**: One-to-Many (a trainer can have many assigned members)
4. **Trainer → WorkoutPlan**: One-to-Many (a trainer creates many workout plans)
5. **Member → WorkoutPlan**: One-to-Many (a member can have many workout plans)
6. **Member → MemberMembership**: One-to-Many (a member can have many membership records)
7. **MembershipPlan → MemberMembership**: One-to-Many (a plan can have many member enrollments)
8. **Member → Attendance**: One-to-Many (a member can have many attendance records)
9. **Member → Payment**: One-to-Many (a member can have many payment records)
10. **MemberMembership → Payment**: One-to-Many (a membership can have many payments)

