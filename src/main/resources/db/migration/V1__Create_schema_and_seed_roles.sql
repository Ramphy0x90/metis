-- Create roles table
CREATE TABLE IF NOT EXISTS roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) UNIQUE NOT NULL
);

-- Create tenants table
CREATE TABLE IF NOT EXISTS tenants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    domain VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    surname VARCHAR(255),
    tenant_uuid UUID REFERENCES tenants(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create user_roles junction table
CREATE TABLE IF NOT EXISTS user_roles (
    user_uuid UUID REFERENCES users(id) ON DELETE CASCADE,
    role_uuid UUID REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_uuid, role_uuid)
);

-- Create services table (TenantService entity)
CREATE TABLE IF NOT EXISTS services (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
    price NUMERIC(38,2),
    duration_minutes INTEGER NOT NULL,
    tenant_uuid UUID REFERENCES tenants(id)
);

-- Create bookings table
CREATE TABLE IF NOT EXISTS bookings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_email VARCHAR(255),
    client_name VARCHAR(255),
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    status VARCHAR(255) CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED')),
    tenant_uuid UUID REFERENCES tenants(id),
    service_uuid UUID REFERENCES services(id),
    employee_uuid UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert initial roles
INSERT INTO roles (id, name) VALUES 
    (gen_random_uuid(), 'Global Admin'),
    (gen_random_uuid(), 'Admin'),
    (gen_random_uuid(), 'Employee'),
    (gen_random_uuid(), 'User')
ON CONFLICT (name) DO NOTHING;
