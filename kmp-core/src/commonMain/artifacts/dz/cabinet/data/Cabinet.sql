-- SQLDelight schema for Cabinet database
-- Works on both Android and iOS via KMP

-- Dataset identity table
CREATE TABLE dataset_identity (
    id INTEGER NOT NULL PRIMARY KEY,
    datasetId TEXT NOT NULL
);

-- Patient table
CREATE TABLE patient (
    id INTEGER NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    uuid UUID NOT NULL,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL
);

-- Treatment plan table
CREATE TABLE treatment_plan (
    id INTEGER NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'pending',
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL
);

-- Treatment table
CREATE TABLE treatment (
    id INTEGER NOT NULL PRIMARY KEY,
    planId INTEGER NOT NULL,
    name TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'pending',
    rate INTEGER NOT NULL DEFAULT 0,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL,
    FOREIGN KEY (planId) REFERENCES treatment_plan(id)
);

-- Financial agreement table
CREATE TABLE financial_agreement (
    id INTEGER NOT NULL PRIMARY KEY,
    patientId INTEGER NOT NULL,
    rate INTEGER NOT NULL DEFAULT 0,
    fixedAmount INTEGER NOT NULL DEFAULT 0,
    status TEXT NOT NULL DEFAULT 'active',
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL,
    FOREIGN KEY (patientId) REFERENCES patient(id)
);

-- Commission table
CREATE TABLE commission (
    id INTEGER NOT NULL PRIMARY KEY,
    agreementId INTEGER NOT NULL,
    type TEXT NOT NULL,
    amount INTEGER NOT NULL DEFAULT 0,
    rate INTEGER NOT NULL DEFAULT 0,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL,
    FOREIGN KEY (agreementId) REFERENCES financial_agreement(id)
);

-- Cash session table
CREATE TABLE cash_session (
    id INTEGER NOT NULL PRIMARY KEY,
    sessionNumber INTEGER NOT NULL,
    startDate INTEGER NOT NULL,
    endDate INTEGER,
    totalAmount INTEGER NOT NULL DEFAULT 0,
    status TEXT NOT NULL DEFAULT 'open',
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL
);

-- Expense table
CREATE TABLE expense (
    id INTEGER NOT NULL PRIMARY KEY,
    description TEXT NOT NULL,
    amount INTEGER NOT NULL DEFAULT 0,
    paymentStatus TEXT NOT NULL DEFAULT 'pending',
    sessionId INTEGER,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL,
    FOREIGN KEY (sessionId) REFERENCES cash_session(id)
);

-- Document number table
CREATE TABLE document_number (
    id INTEGER NOT NULL PRIMARY KEY,
    uuid UUID NOT NULL,
    year INTEGER NOT NULL,
    counter INTEGER NOT NULL DEFAULT 0,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL
);

-- Indexes for common queries
CREATE INDEX idx_patient_uuid ON patient(uuid);
CREATE INDEX idx_treatment_plan_status ON treatment_plan(status);
CREATE INDEX idx_treatment_planId ON treatment(planId);
CREATE INDEX idx_financial_agreement_patientId ON financial_agreement(patientId);
CREATE INDEX idx_financial_agreement_status ON financial_agreement(status);
CREATE INDEX idx_commission_agreementId ON commission(agreementId);
CREATE INDEX idx_expense_sessionId ON expense(sessionId);
CREATE INDEX idx_document_number_uuid ON document_number(uuid);