BEGIN;
    CREATE TABLE "USER"
    (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        name TEXT NOT NULL CONSTRAINT user_name_max_length CHECK ( char_length(name) <= 50 ),
        email TEXT NOT NULL
            CONSTRAINT user_email_not_unique UNIQUE
            CONSTRAINT user_email_valid_format CHECK ( email LIKE '%@%' )
            CONSTRAINT user_email_max_length CHECK ( char_length(email) <= 320 ),
        password TEXT NOT NULL CONSTRAINT user_password_max_length CHECK ( char_length(password) <= 127 )
    );

    CREATE TABLE PROJECT
    (
        id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
        name TEXT NOT NULL
            CONSTRAINT project_name_not_unique UNIQUE
            CONSTRAINT project_name_max_length CHECK ( char_length(name) <= 100 ),
        description TEXT NOT NULL CONSTRAINT project_description_max_length CHECK ( char_length(name) <= 300 ),
        "user" UUID NOT NULL REFERENCES "USER"(id) ON DELETE CASCADE
    );

    CREATE TABLE LABEL
    (
        id INT UNIQUE GENERATED ALWAYS AS IDENTITY,
        name TEXT NOT NULL CONSTRAINT label_name_max_length CHECK ( char_length(name) <= 100 ),
        project INT NOT NULL REFERENCES PROJECT(id) ON DELETE CASCADE,
        PRIMARY KEY (id, project)
    );

    CREATE TABLE STATE
    (
        id INT UNIQUE GENERATED ALWAYS AS IDENTITY,
        name TEXT NOT NULL CONSTRAINT state_name_max_length CHECK ( char_length(name) <= 100 ),
        initial BOOL NOT NULL DEFAULT FALSE,
        project INT NOT NULL REFERENCES PROJECT(id) ON DELETE CASCADE,
        PRIMARY KEY (id, project)
    );

    CREATE TABLE ISSUE
    (
        id INT UNIQUE GENERATED ALWAYS AS IDENTITY,
        name TEXT NOT NULL CONSTRAINT issue_name_max_length CHECK ( char_length(name) <= 100 ),
        description TEXT NOT NULL CONSTRAINT issue_description_max_length CHECK ( char_length(name) <= 300 ),
        creation_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        close_timestamp TIMESTAMP,
        state INT NOT NULL REFERENCES STATE(id) ON DELETE CASCADE,
        project INT NOT NULL REFERENCES PROJECT(id) ON DELETE CASCADE,
        PRIMARY KEY(id, project)
    );

    CREATE TABLE ISSUE_LABEL
    (
        issue INT REFERENCES ISSUE(id) ON DELETE CASCADE,
        project INT REFERENCES PROJECT(id) ON DELETE CASCADE,
        label INT REFERENCES LABEL(id) ON DELETE CASCADE,
        PRIMARY KEY (issue, project, label)
    );

    CREATE TABLE COMMENT
    (
        id INT UNIQUE GENERATED ALWAYS AS IDENTITY,
        issue INT REFERENCES ISSUE(id) ON DELETE CASCADE,
        project INT REFERENCES PROJECT(id) ON DELETE CASCADE,
        comment TEXT NOT NULL CONSTRAINT comment_max_length CHECK ( char_length(comment) <= 200 ),
        timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        PRIMARY KEY (id, issue, project)
    );

    CREATE TABLE STATE_TRANSITION
    (
        current_state INT REFERENCES STATE(id) ON DELETE CASCADE,
        next_state INT REFERENCES STATE(id) ON DELETE CASCADE,
        project INT REFERENCES PROJECT(id) ON DELETE CASCADE,
        PRIMARY KEY (current_state, next_state, project)
    );

    CREATE TABLE SESSION
    (
        user_id UUID NOT NULL,
        token UUID NOT NULL DEFAULT gen_random_uuid(),
        timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        PRIMARY KEY (user_id)
    );

COMMIT;