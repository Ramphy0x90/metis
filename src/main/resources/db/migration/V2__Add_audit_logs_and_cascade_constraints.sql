-- Add audit_logs table
CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    operation VARCHAR(50) NOT NULL CHECK (operation IN ('CREATE', 'UPDATE', 'DELETE')),
    entity_type VARCHAR(255) NOT NULL,
    entity_id UUID,
    old_values TEXT,
    new_values TEXT,
    performed_by VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    tenant_id VARCHAR(255),
    description TEXT
);

-- Add cascade constraints to existing tables
-- Note: These ALTER TABLE statements will only add constraints if they don't already exist

-- Add cascade delete to users table
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'users_tenant_uuid_fkey' 
        AND table_name = 'users'
    ) THEN
        ALTER TABLE users ADD CONSTRAINT users_tenant_uuid_fkey 
        FOREIGN KEY (tenant_uuid) REFERENCES tenants(id) ON DELETE CASCADE;
    END IF;
END $$;

-- Add cascade delete to services table
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'services_tenant_uuid_fkey' 
        AND table_name = 'services'
    ) THEN
        ALTER TABLE services ADD CONSTRAINT services_tenant_uuid_fkey 
        FOREIGN KEY (tenant_uuid) REFERENCES tenants(id) ON DELETE CASCADE;
    END IF;
END $$;

-- Add cascade delete to bookings table
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'bookings_tenant_uuid_fkey' 
        AND table_name = 'bookings'
    ) THEN
        ALTER TABLE bookings ADD CONSTRAINT bookings_tenant_uuid_fkey 
        FOREIGN KEY (tenant_uuid) REFERENCES tenants(id) ON DELETE CASCADE;
    END IF;
END $$;

-- Add cascade delete to bookings service reference
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'bookings_service_uuid_fkey' 
        AND table_name = 'bookings'
    ) THEN
        ALTER TABLE bookings ADD CONSTRAINT bookings_service_uuid_fkey 
        FOREIGN KEY (service_uuid) REFERENCES services(id) ON DELETE CASCADE;
    END IF;
END $$;

-- Add SET NULL to bookings employee reference
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'bookings_employee_uuid_fkey' 
        AND table_name = 'bookings'
    ) THEN
        ALTER TABLE bookings ADD CONSTRAINT bookings_employee_uuid_fkey 
        FOREIGN KEY (employee_uuid) REFERENCES users(id) ON DELETE SET NULL;
    END IF;
END $$;
