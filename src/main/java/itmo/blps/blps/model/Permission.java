package itmo.blps.blps.model;

public enum Permission {
    // Course permissions
    VIEW_COURSE,
    CREATE_COURSE,
    EDIT_COURSE,
    DELETE_COURSE,

    // Task permissions
    VIEW_TASK,
    CREATE_TASK,
    EDIT_TASK,
    DELETE_TASK,
    SUBMIT_TASK,
    GRADE_TASK,

    // Certificate permissions
    VIEW_CERTIFICATE,
    ISSUE_CERTIFICATE,
    VERIFY_CERTIFICATE,

    // User management
    VIEW_USERS,
    EDIT_USERS,
    DELETE_USERS
}