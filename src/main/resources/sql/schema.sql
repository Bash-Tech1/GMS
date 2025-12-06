-- =====================================================
-- COMPLETE GYM MANAGEMENT SYSTEM SETUP
-- This script will:
-- 1. Drop existing database (if any)
-- 2. Create new database
-- 3. Create all tables with proper structure
-- 4. Insert sample data for testing
-- =====================================================

-- Drop and recreate database
DROP DATABASE IF EXISTS gym_management_system;
CREATE DATABASE gym_management_system;
USE gym_management_system;

-- Disable foreign key checks temporarily
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- TABLE CREATION
-- =====================================================

CREATE TABLE plans (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    duration INT NOT NULL,
    benefits TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_plan_price CHECK (price > 0),
    CONSTRAINT chk_plan_duration CHECK (duration > 0)
);

CREATE INDEX idx_plan_active ON plans(is_active);

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role ENUM('admin', 'trainer', 'member') DEFAULT 'member',
    plan_id INT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL DEFAULT NULL,
    CONSTRAINT fk_user_plan FOREIGN KEY (plan_id) REFERENCES plans(id) ON DELETE SET NULL
);

CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_plan ON users(plan_id);
CREATE INDEX idx_user_role ON users(role);
CREATE INDEX idx_user_active ON users(is_active);

CREATE TABLE equipment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    status ENUM('active', 'maintenance', 'retired') DEFAULT 'active',
    purchase_date DATE,
    last_maintenance_date DATE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_equipment_status ON equipment(status);
CREATE INDEX idx_equipment_active ON equipment(is_active);

CREATE TABLE trainer_equipment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    trainer_id INT NOT NULL,
    equipment_id INT NOT NULL,
    responsibility_level ENUM('primary', 'secondary', 'shared') DEFAULT 'primary',
    assigned_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_trainer_equipment_user FOREIGN KEY (trainer_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_trainer_equipment_equip FOREIGN KEY (equipment_id) REFERENCES equipment(id) ON DELETE CASCADE,
    UNIQUE KEY unique_trainer_equipment (trainer_id, equipment_id)
);

CREATE INDEX idx_trainer_equipment_trainer ON trainer_equipment(trainer_id);
CREATE INDEX idx_trainer_equipment_equip ON trainer_equipment(equipment_id);
CREATE INDEX idx_trainer_equipment_level ON trainer_equipment(responsibility_level);

CREATE TABLE workout_plans (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    trainer_id INT NOT NULL,
    description TEXT,
    duration INT NOT NULL,
    difficulty ENUM('beginner', 'intermediate', 'advanced') DEFAULT 'beginner',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_workout_trainer FOREIGN KEY (trainer_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT chk_workout_duration CHECK (duration > 0)
);

CREATE INDEX idx_workout_trainer ON workout_plans(trainer_id);
CREATE INDEX idx_workout_active ON workout_plans(is_active);

CREATE TABLE member_workout_plans (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    workout_plan_id INT NOT NULL,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL DEFAULT NULL,
    status ENUM('active', 'completed', 'paused', 'cancelled') DEFAULT 'active',
    CONSTRAINT fk_member_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_member_workout FOREIGN KEY (workout_plan_id) REFERENCES workout_plans(id) ON DELETE CASCADE,
    UNIQUE KEY unique_member_workout (user_id, workout_plan_id)
);

CREATE INDEX idx_member_user ON member_workout_plans(user_id);
CREATE INDEX idx_member_workout ON member_workout_plans(workout_plan_id);
CREATE INDEX idx_member_status ON member_workout_plans(status);

CREATE TABLE workout_plan_equipment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    workout_plan_id INT NOT NULL,
    equipment_id INT NOT NULL,
    set_order INT NOT NULL,
    sets INT,
    reps INT,
    duration_minutes INT,
    CONSTRAINT fk_workout_plan FOREIGN KEY (workout_plan_id) REFERENCES workout_plans(id) ON DELETE CASCADE,
    CONSTRAINT fk_plan_equipment FOREIGN KEY (equipment_id) REFERENCES equipment(id) ON DELETE CASCADE,
    UNIQUE KEY unique_workout_equipment (workout_plan_id, equipment_id),
    CONSTRAINT chk_set_order CHECK (set_order > 0),
    CONSTRAINT chk_sets CHECK (sets IS NULL OR sets > 0),
    CONSTRAINT chk_reps CHECK (reps IS NULL OR reps > 0),
    CONSTRAINT chk_duration CHECK (duration_minutes IS NULL OR duration_minutes > 0)
);

CREATE INDEX idx_workout_equipment ON workout_plan_equipment(workout_plan_id);
CREATE INDEX idx_equipment_workout ON workout_plan_equipment(equipment_id);

CREATE TABLE class_sessions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    trainer_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    capacity INT NOT NULL,
    current_enrollment INT DEFAULT 0,
    status ENUM('scheduled', 'ongoing', 'completed', 'cancelled') DEFAULT 'scheduled',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_class_trainer FOREIGN KEY (trainer_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT chk_capacity CHECK (capacity > 0),
    CONSTRAINT chk_enrollment CHECK (current_enrollment >= 0),
    CONSTRAINT chk_enrollment_capacity CHECK (current_enrollment <= capacity),
    CONSTRAINT chk_class_time CHECK (end_time > start_time)
);

CREATE INDEX idx_class_trainer ON class_sessions(trainer_id);
CREATE INDEX idx_class_status ON class_sessions(status);
CREATE INDEX idx_class_time ON class_sessions(start_time);
CREATE INDEX idx_class_active ON class_sessions(is_active);

CREATE TABLE class_session_equipment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    class_session_id INT NOT NULL,
    equipment_id INT NOT NULL,
    quantity_needed INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_class_session FOREIGN KEY (class_session_id) REFERENCES class_sessions(id) ON DELETE CASCADE,
    CONSTRAINT fk_session_equipment FOREIGN KEY (equipment_id) REFERENCES equipment(id) ON DELETE CASCADE,
    UNIQUE KEY unique_class_equipment (class_session_id, equipment_id),
    CONSTRAINT chk_quantity CHECK (quantity_needed > 0)
);

CREATE INDEX idx_class_equipment ON class_session_equipment(class_session_id);
CREATE INDEX idx_equipment_class ON class_session_equipment(equipment_id);

CREATE TABLE bookings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    class_session_id INT NOT NULL,
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('confirmed', 'cancelled', 'attended', 'no_show') DEFAULT 'confirmed',
    cancelled_reason TEXT,
    cancelled_at TIMESTAMP NULL DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_class FOREIGN KEY (class_session_id) REFERENCES class_sessions(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_class_booking (user_id, class_session_id)
);

CREATE INDEX idx_booking_user ON bookings(user_id);
CREATE INDEX idx_booking_class ON bookings(class_session_id);
CREATE INDEX idx_booking_status ON bookings(status);
CREATE INDEX idx_booking_date ON bookings(booking_date);

CREATE TABLE diet_charts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    trainer_id INT NOT NULL,
    recommendations TEXT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_diet_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_diet_trainer FOREIGN KEY (trainer_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE INDEX idx_diet_user ON diet_charts(user_id);
CREATE INDEX idx_diet_trainer ON diet_charts(trainer_id);
CREATE INDEX idx_diet_active ON diet_charts(is_active);

CREATE TABLE payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    plan_id INT,
    amount DECIMAL(10, 2) NOT NULL,
    payment_method ENUM('credit_card', 'debit_card', 'paypal', 'cash', 'bank_transfer') NOT NULL,
    status ENUM('pending', 'completed', 'failed', 'refunded') DEFAULT 'pending',
    transaction_reference VARCHAR(255),
    paid_at TIMESTAMP NULL DEFAULT NULL,
    refunded_at TIMESTAMP NULL DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_payment_plan FOREIGN KEY (plan_id) REFERENCES plans(id) ON DELETE SET NULL,
    CONSTRAINT chk_payment_amount CHECK (amount > 0)
);

CREATE INDEX idx_payment_user ON payments(user_id);
CREATE INDEX idx_payment_plan ON payments(plan_id);
CREATE INDEX idx_payment_status ON payments(status);
CREATE INDEX idx_payment_date ON payments(created_at);

CREATE TABLE attendance (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    class_session_id INT NOT NULL,
    booking_id INT,
    check_in_time DATETIME NOT NULL,
    status ENUM('present', 'absent', 'late') DEFAULT 'present',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_attendance_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_class FOREIGN KEY (class_session_id) REFERENCES class_sessions(id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE SET NULL
);

CREATE INDEX idx_attendance_user ON attendance(user_id);
CREATE INDEX idx_attendance_class ON attendance(class_session_id);
CREATE INDEX idx_attendance_booking ON attendance(booking_id);
CREATE INDEX idx_attendance_status ON attendance(status);
CREATE INDEX idx_attendance_date ON attendance(check_in_time);

-- =====================================================
-- SAMPLE DATA INSERTION
-- =====================================================

-- =====================================================
-- 1. INSERT PLANS
-- =====================================================
INSERT INTO plans (name, description, price, duration, benefits, is_active) VALUES
('Basic Monthly', 'Access to gym facilities during off-peak hours', 29.99, 30, 'Gym access, Locker room, Free WiFi', TRUE),
('Standard Monthly', 'Full access to gym facilities anytime', 49.99, 30, 'Gym access, Locker room, Free WiFi, Group classes', TRUE),
('Premium Monthly', 'Full access plus personal training sessions', 89.99, 30, 'All Standard benefits, 4 PT sessions/month, Diet consultation', TRUE),
('Annual Basic', 'Basic plan paid annually with discount', 299.99, 365, 'All Basic benefits for a year, 2 months free', TRUE),
('Annual Premium', 'Premium plan paid annually with discount', 899.99, 365, 'All Premium benefits for a year, 2 months free', TRUE),
('Student Monthly', 'Discounted plan for students', 24.99, 30, 'Gym access during off-peak, Student lounge access', TRUE);

-- =====================================================
-- 2. INSERT USERS
-- WARNING: PLAIN TEXT PASSWORDS - FOR TESTING ONLY!
-- =====================================================

-- Admin users
INSERT INTO users (email, password, username, full_name, phone, role, plan_id, is_active) VALUES
('admin@gym.com', 'admin123', 'admin', 'John Administrator', '+1234567890', 'admin', NULL, TRUE),
('manager@gym.com', 'manager123', 'manager', 'Sarah Manager', '+1234567891', 'admin', NULL, TRUE);

-- Trainer users
INSERT INTO users (email, password, username, full_name, phone, role, plan_id, is_active) VALUES
('trainer1@gym.com', 'trainer123', 'mike_trainer', 'Mike Johnson', '+1234567892', 'trainer', NULL, TRUE),
('trainer2@gym.com', 'trainer123', 'lisa_trainer', 'Lisa Williams', '+1234567893', 'trainer', NULL, TRUE),
('trainer3@gym.com', 'trainer123', 'david_trainer', 'David Brown', '+1234567894', 'trainer', NULL, TRUE);

-- Member users
INSERT INTO users (email, password, username, full_name, phone, role, plan_id, is_active) VALUES
('member1@email.com', 'member123', 'john_doe', 'John Doe', '+1234567895', 'member', 1, TRUE),
('member2@email.com', 'member123', 'jane_smith', 'Jane Smith', '+1234567896', 'member', 2, TRUE),
('member3@email.com', 'member123', 'bob_wilson', 'Bob Wilson', '+1234567897', 'member', 3, TRUE),
('member4@email.com', 'member123', 'alice_jones', 'Alice Jones', '+1234567898', 'member', 2, TRUE),
('member5@email.com', 'member123', 'charlie_davis', 'Charlie Davis', '+1234567899', 'member', 1, TRUE),
('member6@email.com', 'member123', 'emma_taylor', 'Emma Taylor', '+1234567810', 'member', 3, TRUE),
('member7@email.com', 'member123', 'frank_miller', 'Frank Miller', '+1234567811', 'member', 5, TRUE),
('member8@email.com', 'member123', 'grace_anderson', 'Grace Anderson', '+1234567812', 'member', 6, TRUE);

-- =====================================================
-- 3. INSERT EQUIPMENT
-- =====================================================
INSERT INTO equipment (name, description, status, purchase_date, last_maintenance_date, is_active) VALUES
('Treadmill-01', 'Commercial grade treadmill with heart rate monitor', 'active', '2023-01-15', '2024-11-01', TRUE),
('Treadmill-02', 'Commercial grade treadmill with heart rate monitor', 'active', '2023-01-15', '2024-11-01', TRUE),
('Elliptical-01', 'Cross trainer with adjustable resistance', 'active', '2023-02-20', '2024-10-15', TRUE),
('Stationary Bike-01', 'Spin bike with digital display', 'active', '2023-02-20', '2024-10-15', TRUE),
('Bench Press', 'Olympic bench press station', 'active', '2023-03-10', '2024-09-20', TRUE),
('Squat Rack', 'Power rack with pull-up bar', 'active', '2023-03-10', '2024-09-20', TRUE),
('Dumbbells Set', 'Complete set 5-50 lbs', 'active', '2023-01-10', '2024-08-10', TRUE),
('Cable Machine', 'Multi-function cable crossover', 'active', '2023-04-05', '2024-10-01', TRUE),
('Rowing Machine', 'Water resistance rowing machine', 'active', '2023-05-12', '2024-11-05', TRUE),
('Leg Press', 'Plate-loaded leg press machine', 'active', '2023-03-15', '2024-09-15', TRUE),
('Smith Machine', 'Smith machine with safety stops', 'maintenance', '2023-02-01', '2024-11-20', TRUE),
('Yoga Mats', 'Premium yoga mats (set of 20)', 'active', '2023-06-01', NULL, TRUE),
('Kettlebell Set', 'Kettlebells 10-50 lbs', 'active', '2023-01-20', NULL, TRUE),
('Battle Ropes', 'Heavy duty training ropes', 'active', '2023-07-10', NULL, TRUE);

-- =====================================================
-- 4. INSERT TRAINER_EQUIPMENT
-- =====================================================
INSERT INTO trainer_equipment (trainer_id, equipment_id, responsibility_level, is_active) VALUES
-- Mike (trainer_id: 3)
(3, 1, 'primary', TRUE),
(3, 2, 'primary', TRUE),
(3, 3, 'primary', TRUE),
(3, 4, 'secondary', TRUE),
-- Lisa (trainer_id: 4)
(4, 5, 'primary', TRUE),
(4, 6, 'primary', TRUE),
(4, 7, 'primary', TRUE),
(4, 10, 'primary', TRUE),
-- David (trainer_id: 5)
(5, 8, 'primary', TRUE),
(5, 12, 'primary', TRUE),
(5, 13, 'primary', TRUE),
(5, 14, 'primary', TRUE);

-- =====================================================
-- 5. INSERT WORKOUT_PLANS
-- =====================================================
INSERT INTO workout_plans (name, trainer_id, description, duration, difficulty, is_active) VALUES
('Beginner Full Body', 3, 'Complete body workout for beginners, 3 times per week', 30, 'beginner', TRUE),
('Advanced Strength Training', 4, 'Intensive strength program focusing on compound movements', 60, 'advanced', TRUE),
('Cardio Burn', 3, 'High-intensity cardio workout for fat loss', 45, 'intermediate', TRUE),
('Functional Fitness', 5, 'Functional movements for everyday strength', 45, 'intermediate', TRUE),
('Power Building', 4, 'Combination of strength and muscle building', 60, 'advanced', TRUE),
('Yoga & Flexibility', 5, 'Stretching and yoga-based workout', 60, 'beginner', TRUE);

-- =====================================================
-- 6. INSERT MEMBER_WORKOUT_PLANS
-- =====================================================
INSERT INTO member_workout_plans (user_id, workout_plan_id, started_at, status) VALUES
(6, 1, '2024-11-01 10:00:00', 'active'),
(7, 3, '2024-11-05 09:00:00', 'active'),
(8, 2, '2024-11-10 14:00:00', 'active'),
(9, 4, '2024-11-12 11:00:00', 'active'),
(10, 1, '2024-11-15 08:00:00', 'active'),
(11, 5, '2024-11-18 15:00:00', 'active');

-- =====================================================
-- 7. INSERT WORKOUT_PLAN_EQUIPMENT
-- =====================================================
INSERT INTO workout_plan_equipment (workout_plan_id, equipment_id, set_order, sets, reps, duration_minutes) VALUES
-- Beginner Full Body (workout_plan_id: 1)
(1, 1, 1, NULL, NULL, 10),
(1, 7, 2, 3, 12, NULL),
(1, 5, 3, 3, 10, NULL),
(1, 10, 4, 3, 12, NULL),
-- Advanced Strength (workout_plan_id: 2)
(2, 6, 1, 5, 5, NULL),
(2, 5, 2, 5, 5, NULL),
(2, 10, 3, 4, 8, NULL),
(2, 8, 4, 4, 10, NULL),
-- Cardio Burn (workout_plan_id: 3)
(3, 1, 1, NULL, NULL, 15),
(3, 3, 2, NULL, NULL, 15),
(3, 9, 3, NULL, NULL, 10);

-- =====================================================
-- 8. INSERT CLASS_SESSIONS
-- =====================================================
INSERT INTO class_sessions (trainer_id, title, description, start_time, end_time, capacity, current_enrollment, status, is_active) VALUES
-- Future classes
(3, 'Morning Cardio Blast', 'High-energy cardio session', '2024-12-10 07:00:00', '2024-12-10 08:00:00', 15, 8, 'scheduled', TRUE),
(4, 'Strength & Power', 'Build strength with compound movements', '2024-12-10 18:00:00', '2024-12-10 19:30:00', 12, 10, 'scheduled', TRUE),
(5, 'Yoga Flow', 'Relaxing yoga session for all levels', '2024-12-11 09:00:00', '2024-12-11 10:00:00', 20, 15, 'scheduled', TRUE),
(3, 'HIIT Training', 'High-intensity interval training', '2024-12-11 17:00:00', '2024-12-11 18:00:00', 15, 12, 'scheduled', TRUE),
(4, 'Olympic Lifting', 'Learn proper olympic lifting techniques', '2024-12-12 19:00:00', '2024-12-12 20:30:00', 10, 7, 'scheduled', TRUE),
-- Past classes
(3, 'Spin Class', 'Indoor cycling workout', '2024-11-25 06:00:00', '2024-11-25 07:00:00', 20, 18, 'completed', TRUE),
(5, 'Core Conditioning', 'Abs and core strengthening', '2024-11-26 12:00:00', '2024-11-26 13:00:00', 15, 12, 'completed', TRUE);

-- =====================================================
-- 9. INSERT CLASS_SESSION_EQUIPMENT
-- =====================================================
INSERT INTO class_session_equipment (class_session_id, equipment_id, quantity_needed) VALUES
(1, 1, 5),
(1, 3, 5),
(2, 5, 3),
(2, 6, 2),
(2, 7, 1),
(3, 12, 20),
(4, 14, 5),
(4, 13, 10),
(5, 6, 2),
(5, 5, 2);

-- =====================================================
-- 10. INSERT BOOKINGS
-- =====================================================
INSERT INTO bookings (user_id, class_session_id, booking_date, status) VALUES
(6, 1, '2024-12-01 10:00:00', 'confirmed'),
(7, 1, '2024-12-01 11:30:00', 'confirmed'),
(8, 1, '2024-12-02 09:00:00', 'confirmed'),
(8, 2, '2024-12-02 10:00:00', 'confirmed'),
(9, 2, '2024-12-02 14:00:00', 'confirmed'),
(10, 2, '2024-12-03 08:00:00', 'confirmed'),
(7, 3, '2024-12-02 12:00:00', 'confirmed'),
(11, 3, '2024-12-03 10:00:00', 'confirmed'),
(13, 3, '2024-12-04 09:00:00', 'confirmed'),
(6, 6, '2024-11-20 08:00:00', 'attended'),
(7, 6, '2024-11-20 09:00:00', 'attended'),
(8, 7, '2024-11-21 10:00:00', 'attended'),
(9, 7, '2024-11-21 11:00:00', 'no_show');

-- =====================================================
-- 11. INSERT DIET_CHARTS
-- =====================================================
INSERT INTO diet_charts (user_id, trainer_id, recommendations, is_active) VALUES
(6, 3, 'High protein diet: 2000 cal/day. Breakfast: Oats with fruits. Lunch: Chicken breast with brown rice. Dinner: Fish with vegetables. Snacks: Nuts and protein shakes.', TRUE),
(7, 4, 'Weight loss diet: 1500 cal/day. Focus on lean proteins, lots of vegetables, minimal carbs. Avoid processed foods. Drink 3L water daily.', TRUE),
(8, 4, 'Muscle building diet: 2800 cal/day. High protein (200g/day), complex carbs, healthy fats. 6 meals per day. Post-workout shake essential.', TRUE),
(11, 5, 'Balanced diet: 2200 cal/day. 40% carbs, 30% protein, 30% fats. Focus on whole foods, meal prep on Sundays. Include variety of fruits and vegetables.', TRUE);

-- =====================================================
-- 12. INSERT PAYMENTS
-- =====================================================
INSERT INTO payments (user_id, plan_id, amount, payment_method, status, transaction_reference, paid_at) VALUES
(6, 1, 29.99, 'credit_card', 'completed', 'TXN-2024-001', '2024-11-01 10:30:00'),
(7, 2, 49.99, 'debit_card', 'completed', 'TXN-2024-002', '2024-11-02 14:15:00'),
(8, 3, 89.99, 'credit_card', 'completed', 'TXN-2024-003', '2024-11-03 09:45:00'),
(9, 2, 49.99, 'paypal', 'completed', 'TXN-2024-004', '2024-11-05 16:20:00'),
(10, 1, 29.99, 'cash', 'completed', 'TXN-2024-005', '2024-11-08 11:00:00'),
(11, 3, 89.99, 'bank_transfer', 'completed', 'TXN-2024-006', '2024-11-10 13:30:00'),
(12, 5, 899.99, 'credit_card', 'completed', 'TXN-2024-007', '2024-11-12 10:00:00'),
(13, 6, 24.99, 'debit_card', 'completed', 'TXN-2024-008', '2024-11-15 15:45:00'),
(6, 1, 29.99, 'credit_card', 'pending', 'TXN-2024-009', NULL);

-- =====================================================
-- 13. INSERT ATTENDANCE
-- =====================================================
INSERT INTO attendance (user_id, class_session_id, booking_id, check_in_time, status, is_active) VALUES
(6, 6, 10, '2024-11-25 05:55:00', 'present', TRUE),
(7, 6, 11, '2024-11-25 06:02:00', 'present', TRUE),
(8, 7, 12, '2024-11-26 11:58:00', 'present', TRUE),
(9, 7, 13, '2024-11-26 12:15:00', 'late', TRUE);

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- VERIFICATION - Show what was created
-- =====================================================
SELECT '✓ Database setup completed successfully!' AS Status;

SELECT 'DATA SUMMARY' AS '===================';

SELECT 'Plans' AS Table_Name, COUNT(*) AS Record_Count FROM plans
UNION ALL
SELECT 'Users', COUNT(*) FROM users
UNION ALL
SELECT 'Equipment', COUNT(*) FROM equipment
UNION ALL
SELECT 'Trainer Equipment', COUNT(*) FROM trainer_equipment
UNION ALL
SELECT 'Workout Plans', COUNT(*) FROM workout_plans
UNION ALL
SELECT 'Member Workout Plans', COUNT(*) FROM member_workout_plans
UNION ALL
SELECT 'Workout Plan Equipment', COUNT(*) FROM workout_plan_equipment
UNION ALL
SELECT 'Class Sessions', COUNT(*) FROM class_sessions
UNION ALL
SELECT 'Class Session Equipment', COUNT(*) FROM class_session_equipment
UNION ALL
SELECT 'Bookings', COUNT(*) FROM bookings
UNION ALL
SELECT 'Diet Charts', COUNT(*) FROM diet_charts
UNION ALL
SELECT 'Payments', COUNT(*) FROM payments
UNION ALL
SELECT 'Attendance', COUNT(*) FROM attendance;

-- =====================================================
-- TEST LOGIN CREDENTIALS
-- =====================================================
SELECT 'LOGIN CREDENTIALS' AS '===================';
SELECT 
    'Admin Login' AS Account_Type,
    'admin@gym.com' AS Email,
    'admin123' AS Password
UNION ALL
SELECT 'Trainer Login', 'trainer1@gym.com', 'trainer123'
UNION ALL
SELECT 'Member Login', 'member1@email.com', 'member123';