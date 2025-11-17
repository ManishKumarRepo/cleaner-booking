DROP TABLE IF EXISTS booking;
DROP TABLE IF EXISTS cleaner_professional;
DROP TABLE IF EXISTS vehicle;

CREATE TABLE vehicle (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE cleaner_professional (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    vehicle_id BIGINT,
    FOREIGN KEY (vehicle_id) REFERENCES vehicle(id)
);

CREATE TABLE booking (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cleaner_id BIGINT,
    date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    FOREIGN KEY (cleaner_id) REFERENCES cleaner_professional(id)
);