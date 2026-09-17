-- StaySmart SQLite schema. Executed automatically by DatabaseHandler when the database
-- file is created for the first time. One statement per ';' line ending.

CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL,
    password TEXT NOT NULL,
    fullname TEXT,
    email TEXT,
    contact_number TEXT,
    address TEXT,
    business_name TEXT,
    business_type TEXT,
    profile_image_path TEXT,
    role TEXT DEFAULT 'user',
    created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    ewallet_provider TEXT,
    ewallet_number TEXT,
    qr_image_path TEXT,
    about_me TEXT,
    emergency_contact_name TEXT,
    emergency_contact_phone TEXT,
    emergency_contact_relation TEXT,
    pref_language TEXT DEFAULT 'English',
    pref_timezone TEXT
);

CREATE TABLE IF NOT EXISTS properties (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT,
    location TEXT,
    price REAL,
    location_type TEXT,
    total_floors INTEGER,
    image_path TEXT,
    type TEXT DEFAULT 'Apartment',
    floors TEXT DEFAULT '1',
    landlord_id INTEGER REFERENCES users(id) ON DELETE SET NULL,
    amenities TEXT,
    created_at TEXT DEFAULT (datetime('now', 'localtime'))
);

CREATE TABLE IF NOT EXISTS property_floors (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    property_id INTEGER REFERENCES properties(id) ON DELETE CASCADE,
    floor_number INTEGER,
    room_count INTEGER
);

CREATE TABLE IF NOT EXISTS rooms (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    property_id INTEGER NOT NULL REFERENCES properties(id) ON DELETE CASCADE,
    floor_level INTEGER NOT NULL,
    room_number TEXT NOT NULL,
    status TEXT DEFAULT 'Available',
    price REAL NOT NULL DEFAULT 0,
    image_path TEXT,
    facilities TEXT,
    payment_status TEXT DEFAULT 'Pending'
);

CREATE TABLE IF NOT EXISTS applications (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    room_id INTEGER NOT NULL REFERENCES rooms(id) ON DELETE CASCADE,
    tenant_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    property_id INTEGER NOT NULL REFERENCES properties(id) ON DELETE CASCADE,
    application_type TEXT NOT NULL CHECK (application_type IN ('Booking', 'Tour')),
    message TEXT,
    payment_method TEXT,
    contact_number TEXT,
    status TEXT DEFAULT 'Pending',
    apply_date TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
);

CREATE TABLE IF NOT EXISTS messages (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    application_id INTEGER NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    sender_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    receiver_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    message_text TEXT NOT NULL,
    timestamp TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
);

CREATE TABLE IF NOT EXISTS saved_properties (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    property_id INTEGER NOT NULL REFERENCES properties(id) ON DELETE CASCADE,
    category TEXT NOT NULL CHECK (category IN ('Near Campus', 'Premium Options', 'Budget Picks')),
    saved_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    UNIQUE (tenant_id, property_id)
);

CREATE INDEX IF NOT EXISTS idx_properties_landlord ON properties(landlord_id);
CREATE INDEX IF NOT EXISTS idx_property_floors_property ON property_floors(property_id);
CREATE INDEX IF NOT EXISTS idx_rooms_property ON rooms(property_id);
CREATE INDEX IF NOT EXISTS idx_applications_room ON applications(room_id);
CREATE INDEX IF NOT EXISTS idx_applications_tenant ON applications(tenant_id);
CREATE INDEX IF NOT EXISTS idx_applications_property ON applications(property_id);
CREATE INDEX IF NOT EXISTS idx_messages_application ON messages(application_id);
CREATE INDEX IF NOT EXISTS idx_saved_properties_property ON saved_properties(property_id);
