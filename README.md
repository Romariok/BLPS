# BLPS Project with JAAS Security

This project implements Java Authentication and Authorization Service (JAAS) to provide advanced security features.

## Security Architecture

The security system is built on two key concepts:

1. **Roles** - Represent user types (STUDENT, TEACHER)
2. **Permissions** - Fine-grained access controls for specific actions

### Key Components

- **UserService**: Implements UserDetailsService to load user authorities (roles and permissions)
- **AuthService**: Handles authentication and token generation
- **RolePermission**: Maps roles to permissions in a many-to-many relationship
- **JWTUtil**: Manages token generation and validation with authorities
- **SecurityConfig**: Configures URL-based access control using permissions

### Authorization Process

1. User logs in and receives a JWT containing all roles and permissions
2. JwtAuthenticationFilter extracts authorities from token
3. Security decisions are made based on:
   - URL patterns in SecurityConfig
   - JSR-250 annotations (if used)

### Default Role Permissions

- **STUDENT**:
  - VIEW_COURSE
  - VIEW_TASK
  - SUBMIT_TASK
  - VIEW_CERTIFICATE

- **TEACHER**:
  - All course management permissions
  - All task management permissions
  - Certificate management

## Setup

The DataInitializer class automatically sets up role-permission mappings on application startup.