-- ============================================================
-- Gym Management System - Database Schema
-- ============================================================

CREATE DATABASE IF NOT EXISTS gymdemo;
USE gymdemo;

-- Users Table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(15),
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Membership Plans Table
CREATE TABLE membership_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    duration_days INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    INDEX idx_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Trainers Table
CREATE TABLE trainers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    specialization VARCHAR(255),
    experience INT,
    bio TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Members Table
CREATE TABLE members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    gender VARCHAR(20),
    date_of_birth DATE,
    height DECIMAL(5,2),
    weight DECIMAL(5,2),
    bmi DECIMAL(5,2),
    emergency_contact VARCHAR(20),
    medical_conditions TEXT,
    assigned_trainer_id BIGINT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_trainer_id) REFERENCES trainers(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_trainer_id (assigned_trainer_id),
    INDEX idx_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Member Memberships Table
CREATE TABLE member_memberships (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    amount_paid DECIMAL(10,2),
    payment_status VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
    FOREIGN KEY (plan_id) REFERENCES membership_plans(id) ON DELETE CASCADE,
    INDEX idx_member_id (member_id),
    INDEX idx_active (active),
    INDEX idx_end_date (end_date),
    INDEX idx_member_active (member_id, active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Workout Plans Table
CREATE TABLE workout_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    trainer_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    exercises TEXT,
    difficulty VARCHAR(50),
    duration_weeks INT,
    start_date DATE,
    end_date DATE,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    notes TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
    FOREIGN KEY (trainer_id) REFERENCES trainers(id) ON DELETE CASCADE,
    INDEX idx_member_id (member_id),
    INDEX idx_trainer_id (trainer_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Attendance Table
CREATE TABLE attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    attendance_date DATE NOT NULL,
    check_in_time TIME NOT NULL,
    status VARCHAR(50) DEFAULT 'PRESENT',
    notes TEXT,
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
    UNIQUE KEY uk_member_date (member_id, attendance_date),
    INDEX idx_member_id (member_id),
    INDEX idx_attendance_date (attendance_date),
    INDEX idx_member_date (member_id, attendance_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Payments Table
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    membership_id BIGINT,
    amount DECIMAL(10,2) NOT NULL,
    payment_date DATE NOT NULL,
    payment_mode VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(100),
    status VARCHAR(50) DEFAULT 'COMPLETED',
    notes TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
    FOREIGN KEY (membership_id) REFERENCES member_memberships(id) ON DELETE SET NULL,
    INDEX idx_member_id (member_id),
    INDEX idx_payment_date (payment_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- Optimized SQL Queries for Reports
-- ============================================================

-- 1. Active Members
SELECT m.id, u.name, u.email, u.phone, m.gender, m.date_of_birth
FROM members m
JOIN users u ON m.user_id = u.id
WHERE m.active = TRUE;

-- 2. Expired Memberships
SELECT mm.id, u.name AS member_name, mp.name AS plan_name,
       mm.start_date, mm.end_date
FROM member_memberships mm
JOIN members m ON mm.member_id = m.id
JOIN users u ON m.user_id = u.id
JOIN membership_plans mp ON mm.plan_id = mp.id
WHERE mm.active = TRUE AND mm.end_date < CURDATE();

-- 3. Monthly Revenue Report
SELECT MONTH(p.payment_date) AS month,
       YEAR(p.payment_date) AS year,
       SUM(p.amount) AS total_revenue
FROM payments p
WHERE p.status = 'COMPLETED'
  AND p.payment_date >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH)
GROUP BY YEAR(p.payment_date), MONTH(p.payment_date)
ORDER BY YEAR(p.payment_date), MONTH(p.payment_date);

-- 4. Daily Attendance Report
SELECT u.name, u.email, a.check_in_time, a.status
FROM attendance a
JOIN members m ON a.member_id = m.id
JOIN users u ON m.user_id = u.id
WHERE a.attendance_date = CURDATE()
ORDER BY a.check_in_time;

-- 5. Top 10 Most Active Members (by attendance count in last 30 days)
SELECT m.id, u.name, COUNT(a.id) AS attendance_count
FROM attendance a
JOIN members m ON a.member_id = m.id
JOIN users u ON m.user_id = u.id
WHERE a.attendance_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
GROUP BY m.id, u.name
ORDER BY attendance_count DESC
LIMIT 10;

-- 6. Trainer-wise Member Count
SELECT t.id, u.name AS trainer_name,
       COUNT(m.id) AS member_count
FROM trainers t
JOIN users u ON t.user_id = u.id
LEFT JOIN members m ON m.assigned_trainer_id = t.id AND m.active = TRUE
WHERE t.active = TRUE
GROUP BY t.id, u.name
ORDER BY member_count DESC;

-- 7. Membership Renewal Report
SELECT mm.id, u.name AS member_name, mp.name AS plan_name,
       mm.start_date, mm.end_date,
       DATEDIFF(mm.end_date, CURDATE()) AS days_remaining,
       CASE
           WHEN mm.end_date < CURDATE() THEN 'EXPIRED'
           WHEN mm.end_date <= DATE_ADD(CURDATE(), INTERVAL 7 DAY) THEN 'EXPIRING SOON'
           ELSE 'ACTIVE'
       END AS renewal_status
FROM member_memberships mm
JOIN members m ON mm.member_id = m.id
JOIN users u ON m.user_id = u.id
JOIN membership_plans mp ON mm.plan_id = mp.id
WHERE mm.active = TRUE
ORDER BY mm.end_date;

-- 8. Dashboard Statistics
SELECT
    (SELECT COUNT(*) FROM members WHERE active = TRUE) AS total_members,
    (SELECT COUNT(*) FROM member_memberships WHERE active = TRUE AND end_date >= CURDATE()) AS active_memberships,
    (SELECT COUNT(*) FROM member_memberships WHERE active = TRUE AND end_date < CURDATE()) AS expired_memberships,
    (SELECT COUNT(*) FROM trainers WHERE active = TRUE) AS total_trainers,
    (SELECT COUNT(*) FROM attendance WHERE attendance_date = CURDATE()) AS today_attendance,
    (SELECT COALESCE(SUM(amount), 0) FROM payments
     WHERE MONTH(payment_date) = MONTH(CURDATE())
       AND YEAR(payment_date) = YEAR(CURDATE())) AS monthly_revenue;

-- ============================================================
-- Indexing Strategy
-- ============================================================
/*
Indexing Strategy Explanation:

1. Users Table:
   - idx_username: Fast lookup during authentication/login
   - idx_email: Quick email uniqueness check and search
   - idx_role: Efficient filtering by user role

2. Members Table:
   - idx_user_id: Quick join with users table
   - idx_trainer_id: Efficient lookup of members by trainer
   - idx_active: Filter active members for reports

3. Member Memberships:
   - idx_member_id: Quick lookup of member's memberships
   - idx_end_date: Efficient query for expired/expiring memberships
   - idx_member_active: Composite index for checking active membership status

4. Attendance:
   - UNIQUE(member_id, attendance_date): Enforces business rule of one attendance per day
   - idx_attendance_date: Fast daily attendance reports
   - idx_member_date: Composite index for member attendance history

5. Payments:
   - idx_payment_date: Efficient monthly/yearly revenue reports
   - idx_member_id: Quick payment history lookup

The composite indexes are designed to support the most frequent query patterns
while maintaining good write performance. All foreign keys are indexed to
support JOIN operations efficiently.
*/

