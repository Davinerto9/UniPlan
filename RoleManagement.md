# Role Management in UniPlan

UniPlan uses a robust role-based access control (RBAC) system powered by **Spring Security** and stored in **MongoDB**. Roles determine what actions a user can perform and which parts of the application they can access.

## Available Roles

| Role | Description | Key Permissions |
| :--- | :--- | :--- |
| **ROLE_ADMIN** | System administrator with full control. | Create users/organizers, view all reports, manage any event. |
| **ROLE_ORGANIZER** | Event creators (Professors, Student Leaders, Wellbeing staff). | Create/edit/delete their own events, manage attendance for their events. |
| **ROLE_STUDENT** | Students registered in the institutional database. | Register for events, cancel their own registrations, view their history. |
| **ROLE_EMPLOYEE** | University staff/employees. | Similar to students, can register for talks and non-student-specific events. |

## Role Assignment

1.  **Students & Employees:** Roles are automatically assigned during registration based on their institutional record (verified against the PostgreSQL database).
2.  **Organizers:** Must be created by an **Admin**. When an organizer is created, they are assigned `ROLE_ORGANIZER` plus a specific subtype (e.g., `ROLE_ORGANIZER_PROFESSOR`) to store additional metadata.
3.  **Admins:** Typically pre-configured in the database or promoted by existing admins.

## Implementation Details

*   **Authentication:** Managed via `CustomUserDetailsService`, which loads the user's roles from the `Users` collection in MongoDB.
*   **Authorization:**
    *   **URL-level Security:** Defined in `WebConfig.java` using `requestMatchers`.
    *   **Method-level Security:** Enforced using `@PreAuthorize` annotations on Controller methods (e.g., `@PreAuthorize("hasRole('ADMIN')")`).
    *   **UI-level Security:** Thymeleaf templates use `sec:authorize` tags to show/hide navigation links and buttons based on the user's role.

## Institutional Verification
The system ensures that `STUDENT` and `EMPLOYEE` roles are only given to users whose IDs exist in the relational PostgreSQL database, maintaining data integrity and ensuring only valid community members can register for internal events.
