CREATE DATABASE IF NOT EXISTS sbsms;
USE sbsms;

CREATE TABLE IF NOT EXISTS residents (
    id_number VARCHAR(50) PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100) DEFAULT '',
    last_name VARCHAR(100) NOT NULL,
    gender VARCHAR(30) NOT NULL,
    age INT NOT NULL,
    address VARCHAR(255) NOT NULL,
    government_id_type VARCHAR(100) DEFAULT '',
    contact_number VARCHAR(30) DEFAULT '',
    disability VARCHAR(100) DEFAULT 'None',
    is_pregnant TINYINT(1) NOT NULL DEFAULT 0,
    original_id_confirmed BOOLEAN DEFAULT FALSE,
    status VARCHAR(30) NOT NULL DEFAULT 'Pending'
);

CREATE TABLE IF NOT EXISTS users (
    id_number VARCHAR(50) PRIMARY KEY,
    full_name VARCHAR(200) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'Resident',
    status VARCHAR(30) NOT NULL DEFAULT 'Pending'
);

CREATE TABLE IF NOT EXISTS services (
    id INT AUTO_INCREMENT PRIMARY KEY,
    service_name VARCHAR(150) UNIQUE NOT NULL,
    category VARCHAR(100) NOT NULL,
    description VARCHAR(255) DEFAULT '',
    fee DECIMAL(10,2) DEFAULT 0,
    status VARCHAR(30) DEFAULT 'Active'
);

CREATE TABLE IF NOT EXISTS service_requests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    resident_id VARCHAR(50) NOT NULL,
    service_id INT NOT NULL,
    request_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(30) DEFAULT 'Pending',
    fee DECIMAL(10,2) DEFAULT 0,
    purpose VARCHAR(255) DEFAULT '',
    FOREIGN KEY (resident_id) REFERENCES residents(id_number) ON DELETE CASCADE,
    FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS appointments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    resident_id VARCHAR(50) NOT NULL,
    service_id INT NOT NULL,
    purpose VARCHAR(255) DEFAULT '',
    appointment_date DATE NOT NULL,
    appointment_time VARCHAR(50) NOT NULL,
    status VARCHAR(30) DEFAULT 'Pending',
    request_id INT NULL,
    FOREIGN KEY (resident_id) REFERENCES residents(id_number) ON DELETE CASCADE,
    FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE RESTRICT,
    FOREIGN KEY (request_id) REFERENCES service_requests(id) ON DELETE SET NULL
);

INSERT IGNORE INTO services(service_name,category,description,fee,status) VALUES
('Barangay Clearance','Clearance','General barangay clearance',150,'Active'),
('Certificate of Indigency','Certificate','Proof of indigency',150,'Active'),
('Business Permit','Permit','Business permit operating within the barangay',150,'Active'),
('Certificate of Residency','Certificate','Proof of residency',150,'Active'),
('Certificate of Good Moral','Certificate','Certificate of good standing',150,'Active'),
('Solo Parent ID','Identification','Solo parent certification',150,'Active'),
('First-Time Jobseeker Certification','Certificate','Certification for first-time jobseekers',150,'Active');
