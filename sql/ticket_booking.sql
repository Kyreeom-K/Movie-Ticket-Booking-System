-- =====================================================
-- Ticket Booking System - MySQL schema + sample data
-- Requires MySQL 8.0.16+ (older versions ignore CHECK constraints)
-- =====================================================
DROP DATABASE IF EXISTS ticket_booking_db;
CREATE DATABASE ticket_booking_db CHARACTER SET utf8mb4;
USE ticket_booking_db;

-- ---------- USERS (Admin + Customer in one table, role decides) ----------
CREATE TABLE users (
    user_id        INT AUTO_INCREMENT PRIMARY KEY,
    full_name      VARCHAR(100) NOT NULL,
    email          VARCHAR(100) NOT NULL UNIQUE,
    password_hash  CHAR(64)     NOT NULL,          -- SHA-256 hex = 64 chars
    phone          VARCHAR(15),
    role           ENUM('ADMIN','CUSTOMER') NOT NULL DEFAULT 'CUSTOMER',
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------- MOVIES ----------
CREATE TABLE movies (
    movie_id          INT AUTO_INCREMENT PRIMARY KEY,
    title             VARCHAR(150) NOT NULL,
    genre             VARCHAR(50)  NOT NULL,
    language          VARCHAR(30)  NOT NULL,
    duration_minutes  INT          NOT NULL CHECK (duration_minutes > 0),
    release_date      DATE,
    UNIQUE (title, language)
);

-- ---------- THEATRES ----------
CREATE TABLE theatres (
    theatre_id  INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    city        VARCHAR(60)  NOT NULL,
    address     VARCHAR(200),
    UNIQUE (name, city)
);

-- ---------- SHOWS ----------
CREATE TABLE shows (
    show_id        INT AUTO_INCREMENT PRIMARY KEY,
    movie_id       INT           NOT NULL,
    theatre_id     INT           NOT NULL,
    show_date      DATE          NOT NULL,
    show_time      TIME          NOT NULL,
    regular_price  DECIMAL(8,2)  NOT NULL CHECK (regular_price >= 0),
    premium_price  DECIMAL(8,2)  NOT NULL,
    total_seats    INT           NOT NULL CHECK (total_seats BETWEEN 1 AND 260),
    -- a check that compares TWO columns must be a table-level constraint in MySQL
    CONSTRAINT chk_premium_price CHECK (premium_price >= regular_price),
    CONSTRAINT fk_show_movie   FOREIGN KEY (movie_id)   REFERENCES movies(movie_id)     ON DELETE RESTRICT,
    CONSTRAINT fk_show_theatre FOREIGN KEY (theatre_id) REFERENCES theatres(theatre_id) ON DELETE RESTRICT,
    -- a theatre cannot run two shows at the same date and time
    UNIQUE (theatre_id, show_date, show_time)
);

-- ---------- SEATS (one row per seat per show) ----------
CREATE TABLE seats (
    seat_id      INT AUTO_INCREMENT PRIMARY KEY,
    show_id      INT      NOT NULL,
    row_label    CHAR(1)  NOT NULL,                -- A, B, C ...
    seat_number  INT      NOT NULL,                -- 1..10 in each row
    seat_type    ENUM('REGULAR','PREMIUM') NOT NULL DEFAULT 'REGULAR',
    status       ENUM('AVAILABLE','BOOKED') NOT NULL DEFAULT 'AVAILABLE',
    CONSTRAINT fk_seat_show FOREIGN KEY (show_id) REFERENCES shows(show_id) ON DELETE CASCADE,
    UNIQUE (show_id, row_label, seat_number)
);

-- ---------- BOOKINGS ----------
CREATE TABLE bookings (
    booking_id    INT AUTO_INCREMENT PRIMARY KEY,
    user_id       INT NOT NULL,
    show_id       INT NOT NULL,
    booking_time  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status        ENUM('CONFIRMED','CANCELLED') NOT NULL DEFAULT 'CONFIRMED',
    CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT,
    CONSTRAINT fk_booking_show FOREIGN KEY (show_id) REFERENCES shows(show_id) ON DELETE RESTRICT
);

-- ---------- BOOKING_SEATS (junction: which seats belong to which booking) ----------
CREATE TABLE booking_seats (
    booking_id  INT           NOT NULL,
    seat_id     INT           NOT NULL,
    price_paid  DECIMAL(8,2)  NOT NULL CHECK (price_paid >= 0),
    PRIMARY KEY (booking_id, seat_id),
    CONSTRAINT fk_bs_booking FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE,
    CONSTRAINT fk_bs_seat    FOREIGN KEY (seat_id)    REFERENCES seats(seat_id)       ON DELETE RESTRICT
);

-- ---------- PAYMENTS (one per booking) ----------
CREATE TABLE payments (
    payment_id      INT AUTO_INCREMENT PRIMARY KEY,
    booking_id      INT           NOT NULL UNIQUE,
    amount          DECIMAL(10,2) NOT NULL CHECK (amount >= 0),
    payment_mode    ENUM('CARD','UPI','NET_BANKING','CASH') NOT NULL,
    payment_status  ENUM('SUCCESS','FAILED','PENDING','REFUNDED') NOT NULL DEFAULT 'PENDING',
    payment_time    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_booking FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE
);

-- ---------- Helper: generate the seat map for a show ----------
-- 10 seats per row; rows A and B are PREMIUM, the rest REGULAR.
DELIMITER //
CREATE PROCEDURE sp_generate_seats(IN p_show_id INT)
BEGIN
    DECLARE v_total INT;
    DECLARE i INT DEFAULT 0;
    SELECT total_seats INTO v_total FROM shows WHERE show_id = p_show_id;
    WHILE i < v_total DO
        INSERT INTO seats (show_id, row_label, seat_number, seat_type)
        VALUES (p_show_id,
                CHAR(65 + FLOOR(i / 10)),          -- 65 = 'A'
                (i MOD 10) + 1,
                IF(FLOOR(i / 10) < 2, 'PREMIUM', 'REGULAR'));
        SET i = i + 1;
    END WHILE;
END //
DELIMITER ;

-- =====================================================
-- SAMPLE DATA
-- Passwords are hashed with SHA-256 (Java PasswordUtil produces the same hash)
--   admin@movies.com  / admin123
--   rahul@mail.com    / pass123
--   priya@mail.com    / pass123
-- =====================================================
INSERT INTO users (full_name, email, password_hash, phone, role) VALUES
('Super Admin',  'admin@movies.com', SHA2('admin123', 256), '9000000001', 'ADMIN'),
('Rahul Kumar',  'rahul@mail.com',   SHA2('pass123', 256),  '9000000002', 'CUSTOMER'),
('Priya Sharma', 'priya@mail.com',   SHA2('pass123', 256),  '9000000003', 'CUSTOMER');

INSERT INTO movies (title, genre, language, duration_minutes, release_date) VALUES
('Space Odyssey',   'Sci-Fi',  'English', 148, '2026-08-15'),
('Laugh Riot',      'Comedy',  'Hindi',   132, '2026-09-01'),
('The Last Kingdom','Action',  'Tamil',   155, '2026-09-20'),
('Monsoon Melody',  'Romance', 'Hindi',   125, '2026-10-02');

INSERT INTO theatres (name, city, address) VALUES
('PVR Grand',    'Chennai', 'Phoenix Mall, Velachery'),
('INOX Central', 'Chennai', 'Express Avenue, Royapettah'),
('Cinepolis',    'Chennai', 'VR Mall, Anna Nagar');

INSERT INTO shows (movie_id, theatre_id, show_date, show_time, regular_price, premium_price, total_seats) VALUES
(1, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '18:30:00', 200.00, 350.00, 40),
(2, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '21:30:00', 180.00, 300.00, 30),
(3, 2, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '14:00:00', 220.00, 380.00, 50),
(4, 3, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '19:00:00', 150.00, 250.00, 30);

-- Create the seat map for each show
CALL sp_generate_seats(1);
CALL sp_generate_seats(2);
CALL sp_generate_seats(3);
CALL sp_generate_seats(4);

-- One sample booking: Rahul books premium seats A1, A2 for show 1
INSERT INTO bookings (user_id, show_id, status) VALUES (2, 1, 'CONFIRMED');

INSERT INTO booking_seats (booking_id, seat_id, price_paid)
SELECT 1, s.seat_id, sh.premium_price
FROM seats s JOIN shows sh ON sh.show_id = s.show_id
WHERE s.show_id = 1 AND s.row_label = 'A' AND s.seat_number IN (1, 2);

UPDATE seats SET status = 'BOOKED'
WHERE seat_id IN (SELECT seat_id FROM booking_seats WHERE booking_id = 1);

INSERT INTO payments (booking_id, amount, payment_mode, payment_status)
SELECT 1, SUM(price_paid), 'UPI', 'SUCCESS' FROM booking_seats WHERE booking_id = 1;
