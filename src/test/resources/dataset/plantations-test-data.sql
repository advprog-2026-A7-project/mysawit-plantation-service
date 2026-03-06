-- dataset for PlantationRepositoryTest

INSERT INTO plantations (id, code, name, location, area, description, owner_id, plant_date, created_at, updated_at) 
VALUES (101, 'TEST-001', 'Test Estate Alpha', 'Sumatra', 1000.5, 'Test description 1', 'owner-auth-999', '2023-01-01 10:00:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO plantations (id, code, name, location, area, description, owner_id, plant_date, created_at, updated_at) 
VALUES (102, 'TEST-002', 'Test Estate Beta', 'Java', 500.0, 'Test description 2', 'owner-auth-999', '2023-06-01 10:00:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO plantations (id, code, name, location, area, description, owner_id, plant_date, created_at, updated_at) 
VALUES (103, 'TEST-003', 'Test Estate Gamma', 'Kalimantan', 2000.0, 'Test description 3', 'owner-auth-888', '2022-01-01 10:00:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
