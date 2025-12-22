package com.codeastras.backend.codeastras.security;

public enum ProjectPermission {

    READ_TREE,
    READ_FILE,
    READ_PROJECT,

    CREATE_FILE,
    CREATE_FOLDER,
    UPDATE_FILE,
    DELETE_FILE,

    EXECUTE_CODE,

    START_SESSION,
    STOP_SESSION,

    OWNER_ONLY,
    READ_COLLABORATORS,
}
