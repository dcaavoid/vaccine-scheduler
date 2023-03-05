CREATE TABLE Caregivers (
    Username varchar(255),
    Salt BINARY(16),
    Hash BINARY(16),
    PRIMARY KEY (Username)
);

CREATE TABLE Availabilities (
    Time date,
    Username varchar(255) REFERENCES Caregivers,
    PRIMARY KEY (Time, Username)
);

CREATE TABLE Vaccines (
    Name varchar(255),
    Doses int,
    PRIMARY KEY (Name)
);

CREATE TABLE Patients (
    Name varchar(255),
    Salt BINARY(16),
    Hash BINARY(16),
    PRIMARY KEY (Name)
);

CREATE TABLE Appointment (
    id int,
    Time date,
    Caregivers varchar(255) REFERENCES Caregivers,
    Vaccines varchar(255) REFERENCES Vaccines,
    Patients varchar(255) REFERENCES Patients,
    PRIMARY KEY (id)
);
