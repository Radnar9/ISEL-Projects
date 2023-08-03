/*
 * Database functionalities
 */

/*
 * Creates a new user
 * Returns the user representation
 */
CREATE OR REPLACE PROCEDURE create_user(uname TEXT, uemail TEXT, upassword TEXT, user_rep OUT JSON)
AS
$$
DECLARE
    user_id UUID; user_name TEXT; user_email TEXT;
BEGIN
    INSERT INTO "USER"(name, email, password) VALUES (uname, uemail, upassword)
    RETURNING id, name, email INTO user_id, user_name, user_email;

    IF (user_id IS NOT NULL) THEN
        user_rep = json_build_object('id', user_id, 'name', user_name, 'email', user_email);
    END IF;
END$$ LANGUAGE plpgsql;

/*
 * Deletes a user and all his projects
 * Returns the deleted user representation
 */
CREATE OR REPLACE PROCEDURE delete_user(user_rep OUT JSON, uid UUID DEFAULT NULL, uemail TEXT DEFAULT NULL)
AS
$$
DECLARE
    user_id UUID; user_name TEXT; user_email TEXT;
BEGIN
    IF (uid IS NULL AND uemail IS NULL) THEN
        RAISE EXCEPTION 'Both parameters cannot be null! Please insert one of the parameters.';
    END IF;

    DELETE FROM "USER" WHERE (id = uid OR email = uemail) RETURNING id, name, email INTO user_id, user_name, user_email;
    IF (user_id IS NOT NULL) THEN
        user_rep = json_build_object('id', user_id, 'name', user_name, 'email', user_email);
    END IF;
END$$ LANGUAGE plpgsql;

/*
 * Verifies a user credentials
 * Returns the user representation
 */
CREATE OR REPLACE PROCEDURE validate_user_credentials(user_rep OUT JSON, uemail TEXT, upw TEXT)
AS
$$
DECLARE
    u_id UUID; user_name TEXT; user_email TEXT; session_token UUID;
BEGIN

    RAISE INFO '%', uemail;
    SELECT id, name, email INTO u_id, user_name, user_email FROM "USER"
    WHERE email = uemail AND password = upw;
    RAISE INFO '%', u_id;
    IF (u_id IS NOT NULL) THEN
        SELECT token INTO session_token FROM session WHERE user_id = u_id;
        IF (session_token IS NULL) THEN
            INSERT INTO session (user_id) VALUES (u_id) RETURNING token INTO session_token;
        END IF;
        user_rep = json_build_object('name', user_name, 'email', user_email, 'token', session_token);
    END IF;
END$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION get_user_info(user_token uuid) RETURNS JSON
    LANGUAGE plpgsql
AS
$$
DECLARE
    user_email TEXT; user_name TEXT; user_id UUID;
BEGIN
    DELETE FROM SESSION WHERE (timestamp < now()::date - 7);

    SELECT id, name, email INTO user_id, user_name, user_email  FROM "USER" u
    INNER JOIN SESSION s ON (u.id = s.user_id) WHERE s.token = user_token;
    RAISE INFO '%', user_email;
    RAISE INFO '%', user_id;
    RAISE INFO '%', user_name;
    IF (user_id IS NOT NULL) THEN
        return json_build_object('id', user_id, 'name', user_name, 'email', user_email);
    ELSE
        return null;
    END IF;
END
$$LANGUAGE plpgsql;

do
$$
DECLARE
    ss JSON;
BEGIN
    SELECT get_user_info('0af643cb-4609-4249-b7d2-c00e6f9aaa25') into ss;
    raise info '%', ss;
end$$;

/*
 * Updates information about a user
 * Returns the updated user representation
 */
CREATE OR REPLACE PROCEDURE update_user(user_rep OUT JSON, uid UUID, uname TEXT DEFAULT NULL, uemail TEXT DEFAULT NULL)
AS
$$
DECLARE
    user_id UUID; user_name TEXT; user_email TEXT;
BEGIN
    CASE
        WHEN (uname IS NULL AND uemail IS NULL) THEN
            RAISE EXCEPTION 'Both parameters cannot be null! Please insert one of the parameters to update.';
        WHEN (uname IS NOT NULL AND uemail IS NOT NULL) THEN
            UPDATE "USER"
            SET name  = uname,
                email = uemail
            WHERE id = uid
            RETURNING id, name, email INTO user_id, user_name, user_email;
        WHEN (uname IS NULL AND uemail IS NOT NULL) THEN
            UPDATE "USER"
            SET email = uemail
            WHERE id = uid
            RETURNING id, name, email INTO user_id, user_name, user_email;
        WHEN (uname IS NOT NULL AND uemail IS NULL) THEN
            UPDATE "USER" SET name = uname WHERE id = uid RETURNING id, name, email INTO user_id, user_name, user_email;
    END CASE;

    user_rep = json_build_object('id', user_id, 'name', user_name, 'email', user_email);
END$$ LANGUAGE plpgsql;

create function get_user_info(token_value uuid) returns json
    language plpgsql
as
$$
DECLARE
    user_email TEXT; user_name TEXT; user_id UUID;
BEGIN
    SELECT id, name, email INTO user_id, user_name, user_email  FROM "USER" u
    INNER JOIN SESSION s ON (u.id = s.user_id) WHERE s.token = token_value;

    IF (user_id IS NOT NULL) THEN
        return json_build_object('id', user_id, 'name', user_name, 'email', user_email);
    END IF;
END
$$;

/*
 * Creates a new project
 * Returns the project representation
 */
CREATE OR REPLACE PROCEDURE create_project(
    pname TEXT,
    pdescription TEXT,
    user_id UUID,
    labels TEXT[],
    states TEXT[],
    initial_state TEXT,
    transitions TEXT[],
    project_rep OUT JSON
)
AS
$$
DECLARE
    prev_id    INT;
    current_id INT;
    project_id INT;
    x1         JSON; x2 JSON;
    text_var1  TEXT; text_var2 TEXT; text_var3 TEXT;
BEGIN
    SELECT last_value INTO prev_id FROM project_id_seq;

    INSERT INTO PROJECT(name, description, "user") VALUES (pname, pdescription, user_id) RETURNING id INTO project_id;
    IF (NOT FOUND OR project_id IS NULL) THEN
        RETURN;
    END IF;

    -- TODO: Verify if labels, states and transitions return a null representation meaning the the addition failed
    CALL set_project_labels(project_id, user_id, labels, x1);
    CALL set_project_states(project_id, states, initial_state, x2);
    CALL set_state_transitions(project_id, transitions);

    project_rep = project_representation(project_id);

EXCEPTION
    WHEN unique_violation THEN
        SELECT last_value INTO current_id FROM project_id_seq;
        IF (prev_id < current_id) THEN
            PERFORM setval('project_id_seq', current_id - 1);
        END IF;
        RAISE unique_violation USING MESSAGE = 'The project name inserted already exists.';
    WHEN OTHERS THEN
        SELECT last_value INTO current_id FROM project_id_seq;
        IF (prev_id < current_id) THEN
            PERFORM setval('project_id_seq', current_id - 1);
        END IF;

        GET STACKED DIAGNOSTICS text_var1 = MESSAGE_TEXT,
            text_var2 = PG_EXCEPTION_DETAIL,
            text_var3 = PG_EXCEPTION_HINT;
        RAISE EXCEPTION '% % %', text_var1, text_var2, text_var3 USING ERRCODE = SQLSTATE;
END$$ LANGUAGE plpgsql;

/*
 * Sets the allowed project labels
 * Returns the project labels id and name
 */
CREATE OR REPLACE PROCEDURE set_project_labels(project_id INT, uid UUID, labels TEXT[], project_labels OUT JSON)
AS
$$
DECLARE
    lab TEXT;
    pname TEXT;
    pdescription TEXT;
BEGIN
    SELECT name, description INTO pname, pdescription FROM PROJECT WHERE id = project_id AND "user" = uid;
    IF (pname IS NULL) THEN
        RETURN;
    END IF;
    FOREACH lab IN ARRAY labels
        LOOP
            IF (EXISTS(SELECT name FROM LABEL WHERE name = lab AND project = project_id)) THEN
                CONTINUE;
            END IF;
            INSERT INTO LABEL(name, project) VALUES (lab, project_id);
            IF (NOT FOUND) THEN
                RAISE EXCEPTION 'It was not possible to add the label % to the project.', lab;
            END IF;
        END LOOP;
    project_labels = json_build_object(
        'id', project_id, 'name', pname, 'description', pdescription,
        'labels', (
            SELECT json_agg(json_build_object('id', id, 'name', name))
            FROM LABEL
            WHERE project = project_id
        )
    );
END$$ LANGUAGE plpgsql;

/*
 * Sets the allowed project states
 * Returns the project states id and name
 */
CREATE OR REPLACE PROCEDURE set_project_states(project_id INT, states TEXT[], initial_state TEXT,
                                               project_states OUT JSON)
AS
$$
DECLARE
    sta TEXT;
BEGIN
    FOREACH sta IN ARRAY states
        LOOP
            IF (EXISTS(SELECT name FROM STATE WHERE name = sta AND project = project_id)) THEN
                CONTINUE;
            END IF;
            IF (sta = initial_state) THEN
                INSERT INTO STATE(name, initial, project) VALUES (sta, true, project_id);
            ELSE
                INSERT INTO STATE(name, project) VALUES (sta, project_id);
            END IF;
            IF (NOT FOUND) THEN
                RAISE EXCEPTION 'It was not possible to add the state % to the project.', sta;
            END IF;
        END LOOP;
    project_states = json_build_object(
            'id', project_id,
            'states', (
                SELECT json_agg(json_build_object('id', id, 'name', name))
                FROM STATE
                WHERE project = project_id
            ),
            'initialState', (SELECT initial_state FROM STATE WHERE project = project_id AND initial = TRUE)
        );
END$$ LANGUAGE plpgsql;

/*
 * Set the project's states transitions, where the array of transitions received must be a pair of states, where the
 * first value of the pair is the current state and the second is the next state
 */
CREATE OR REPLACE PROCEDURE set_state_transitions(project_id INT, transitions TEXT[])
AS
$$
DECLARE
    arr_len INT = array_length(transitions, 1);
    i       INT = 1;
    tran1   INT;
    tran2   INT;
BEGIN
    IF (arr_len % 2 != 0) THEN
        RAISE EXCEPTION 'The length of the array of transitions must be a pair number!';
    END IF;

    WHILE (i <= arr_len)
        LOOP
            SELECT id INTO tran1 FROM STATE WHERE project = project_id AND name = transitions[i];
            SELECT id INTO tran2 FROM STATE WHERE project = project_id AND name = transitions[i + 1];
            IF (tran1 IS NULL OR tran2 IS NULL) THEN
                RAISE EXCEPTION 'The state(s) of the transition inserted does not exist in the current project!';
            END IF;

            INSERT INTO STATE_TRANSITION(current_state, next_state, project) VALUES (tran1, tran2, project_id);
            IF (NOT FOUND) THEN
                RAISE EXCEPTION 'It was not possible to add the transition (%,%) to the project.', transitions[i], transitions[i + 1];
            END IF;
            i = i + 2;
        END LOOP;
END$$ LANGUAGE plpgsql;

/*
 * Updates the name or the description of the project
 * Returns the number of affected rows
 */
CREATE OR REPLACE PROCEDURE update_project(pid INT, uid UUID, updated_project OUT JSON, pname TEXT DEFAULT NULL,
                                           pdescription TEXT DEFAULT NULL)
AS
$$
DECLARE
    ret_name        TEXT;
    ret_description TEXT;
BEGIN
    SELECT name, description INTO ret_name, ret_description FROM PROJECT WHERE id = pid AND "user" = uid;
    IF (ret_name IS NULL) THEN
        RETURN;
    END IF;
    CASE
        WHEN ((pname = ret_name AND pdescription = ret_description) OR (pdescription IS NULL AND pname = ret_name) OR
              (pname IS NULL AND pdescription = ret_description)) THEN
        -- Does not update when the values are the same
        -- Returns json object with the same values
        WHEN (pname IS NULL AND pdescription IS NULL) THEN
            RAISE EXCEPTION 'Both parameters can''t be null! Please insert one of the parameters in order to update.';
        WHEN (pname IS NOT NULL AND pdescription IS NULL) THEN
            UPDATE PROJECT SET name = pname WHERE id = pid RETURNING name, description INTO ret_name, ret_description;
        WHEN (pname IS NULL AND pdescription IS NOT NULL) THEN
            UPDATE PROJECT SET description = pdescription WHERE id = pid
            RETURNING name, description INTO ret_name, ret_description;
        ELSE
            UPDATE PROJECT SET name = pname, description = pdescription WHERE id = pid
            RETURNING name, description INTO ret_name, ret_description;
    END CASE;
    updated_project = json_build_object('id', pid, 'name', ret_name, 'description', ret_description);
END$$ LANGUAGE plpgsql;

/*
 * Deletes a user's project and his dependents
 * Returns the details of the deleted project in json
 */
CREATE OR REPLACE PROCEDURE delete_project(project_id INT, uid UUID, OUT deleted_project JSON)
AS
$$
DECLARE
    ret_name        TEXT;
    ret_description TEXT;
BEGIN
    DELETE FROM PROJECT WHERE id = project_id AND "user" = uid
    RETURNING name, description INTO ret_name, ret_description;
    IF (FOUND) THEN
        deleted_project = json_build_object('id', project_id, 'name', ret_name, 'description', ret_description);
    END IF;
END$$ LANGUAGE plpgsql;

/*
 * Builds a json with the project representation
 */
CREATE OR REPLACE FUNCTION project_representation(
    project_id INT,
    uid UUID DEFAULT NULL,
    limit_rows INT DEFAULT NULL,
    skip_rows INT DEFAULT NULL
)
RETURNS JSON
AS
$$
DECLARE
    statesTran JSON[];
    issues     JSON[];
    rec        RECORD;
    pname      TEXT;
    pdesc      TEXT;
BEGIN
    IF (uid IS NOT NULL AND NOT EXISTS(SELECT name FROM PROJECT WHERE id = project_id AND "user" = uid)) THEN
        RETURN NULL;
    END IF;

    FOR rec IN
        SELECT current_state, s.name AS current_name, next_state, st.name AS next_name
        FROM STATE_TRANSITION tran
                 INNER JOIN STATE s on s.id = tran.current_state
                 INNER JOIN STATE st on st.id = tran.next_state
        WHERE s.project = project_id ORDER BY current_state
    LOOP
        statesTran = array_append(statesTran, json_build_object('id', rec.current_state, 'name', rec.current_name));
        statesTran = array_append(statesTran, json_build_object('id', rec.next_state, 'name', rec.next_name));
    END LOOP;

    FOR rec IN
        SELECT id
        FROM ISSUE
        WHERE project = project_id LIMIT limit_rows OFFSET skip_rows
    LOOP
        issues = array_append(issues, build_issue_item(project_id, rec.id));
    END LOOP;

    SELECT name, description INTO pname, pdesc FROM PROJECT WHERE id = project_id;
    RETURN json_build_object(
            'id', project_id, 'name', pname, 'description', pdesc,
            'labels', (SELECT json_agg(json_build_object('id', id, 'name', name)) FROM LABEL WHERE project = project_id),
            'states', (SELECT json_agg(json_build_object('id', id, 'name', name)) FROM STATE WHERE project = project_id),
            'initialState', (SELECT json_build_object('id', id, 'name', name) FROM STATE WHERE project = project_id AND initial = TRUE),
            'statesTransitions', statesTran,
            'issues', issues,
            'issuesCollectionSize', (SELECT COUNT(id) FROM ISSUE WHERE project = project_id));
END$$ LANGUAGE plpgsql;

/*
 * Builds a json with the project item representation
 */
CREATE OR REPLACE FUNCTION build_project_item(project_id INT)
RETURNS JSON
AS
$$
DECLARE
    pname          TEXT;
    pdescription   TEXT;
BEGIN
    SELECT name, description INTO pname, pdescription FROM PROJECT WHERE id = project_id;
    RETURN json_build_object('id', project_id, 'name', pname, 'description', pdescription);
END$$ LANGUAGE plpgsql;

/*
 * Builds a json with the project item representation
 */
CREATE OR REPLACE FUNCTION projects_representation(
    uid UUID,
    limit_rows INT,
    skip_rows INT
)
RETURNS JSON
AS
$$
DECLARE
    projects   JSON[];
    rec        RECORD;
BEGIN
    FOR rec IN
        SELECT id
        FROM PROJECT
        WHERE "user" = uid ORDER BY id LIMIT limit_rows OFFSET skip_rows
    LOOP
        projects = array_append(projects, build_project_item(rec.id));
    END LOOP;

    RETURN json_build_object(
        'projects', projects,
        'projectsCollectionSize', (SELECT COUNT(id) FROM PROJECT WHERE "user" = uid)
    );
END$$ LANGUAGE plpgsql;

/*
 * View that builds the project representation
 */
CREATE OR REPLACE VIEW PROJECT_REPRESENTATION
AS
SELECT id, "user" AS user_id, project_representation(id) AS project_rep
FROM PROJECT;

/*
 * View with the project characteristics
 */
CREATE OR REPLACE VIEW PROJECT_ITEM
AS
SELECT id, name, description, "user" AS user_id
FROM PROJECT;

/*
 * View with the user characteristics
 */
CREATE OR REPLACE VIEW USER_ITEM
AS
SELECT id, name, email
FROM "USER";

/*
 * Builds and returns a text array with all the labels names of an issue
 */
-- TODO: Not being used
CREATE OR REPLACE FUNCTION get_issue_labels_names(pid INT, iid INT)
    RETURNS TEXT[]
AS
$$
BEGIN
    RETURN ARRAY(SELECT name FROM LABEL WHERE id IN (SELECT label FROM ISSUE_LABEL il WHERE il.project = pid AND issue = iid));
END$$ LANGUAGE plpgsql;

/*
 * Function to get the project initial state
 */
CREATE OR REPLACE FUNCTION get_initial_state(project_id INT)
RETURNS INT
AS
$$
BEGIN
    RETURN (SELECT id FROM STATE WHERE initial = TRUE AND project = project_id);
END$$ LANGUAGE plpgsql;

/*
 * Creates an issue
 * Returns issue representation
*/
CREATE OR REPLACE PROCEDURE create_issue(
    issue_rep OUT JSON,
    project_id INT,
    user_id UUID,
    issue_name TEXT,
    issue_description TEXT,
    issue_labels INT[]
)
AS
$$
DECLARE
    issue_id INT;
    lab INT;
BEGIN
    IF NOT EXISTS(SELECT id FROM PROJECT WHERE id = project_id AND "user" = user_id) THEN
       RETURN;
    END IF;

    INSERT INTO ISSUE(name, description, state, project)
    VALUES (issue_name, issue_description, get_initial_state(project_id), project_id) RETURNING id INTO issue_id;
    IF (NOT FOUND) THEN
        RETURN;
    END IF;
    IF NOT (issue_labels IS NULL) THEN
            FOREACH lab IN ARRAY issue_labels
            LOOP
                IF NOT EXISTS(SELECT id FROM LABEL WHERE id = lab AND project = project_id) THEN
                    RAISE EXCEPTION 'The issue label inserted does not exist in the project.';
            END IF;
            INSERT INTO ISSUE_LABEL(issue, project, label) VALUES (issue_id, project_id, lab);
            IF (NOT FOUND) THEN
                ROLLBACK;
                RAISE EXCEPTION 'It was not possible to add all the labels to the issue, please try again later.';
            END IF;
        END LOOP;
    END IF;
    issue_rep = issue_representation(project_id, issue_id);
END$$ LANGUAGE plpgsql;

/*
 * Deletes an issue
 * Returns issue item
 */
CREATE OR REPLACE PROCEDURE delete_issue(
    issue_rep OUT JSON,
    project_id INT,
    issue_id INT,
    user_id UUID
)
AS
$$
DECLARE
    return_aux JSON;
BEGIN
    return_aux = build_issue_item(project_id, issue_id);
    DELETE FROM ISSUE WHERE id = issue_id AND project = (SELECT id FROM PROJECT WHERE id = project_id AND "user" = user_id);
    IF (FOUND) THEN
        issue_rep = return_aux;
    END IF;
END$$ LANGUAGE plpgsql;

/*
 * Updates an issue
 * Returns issue item
 */
CREATE OR REPLACE PROCEDURE update_issue(
    issue_rep OUT JSON,
    project_id INT,
    issue_id INT,
    user_id UUID,
    i_name TEXT DEFAULT NULL,
    i_description TEXT DEFAULT NULL,
    i_state INT DEFAULT NULL
)
AS
$$
DECLARE
    ret_name        TEXT;
    ret_description TEXT;
    issue_project   INT;
    iclose_timestamp TIMESTAMP;
BEGIN
    IF EXISTS(SELECT id FROM PROJECT WHERE id = project_id AND "user" = user_id) THEN
        CASE
            WHEN (i_name IS NULL AND i_description IS NULL AND i_state IS NULL) THEN
                RAISE EXCEPTION 'Both parameters can''t be null! Please insert one of the parameters in order to update.';
            WHEN (i_state IS NOT NULL) THEN
                IF EXISTS(SELECT id FROM STATE WHERE id = i_state AND name = 'archived') THEN
                    iclose_timestamp = CURRENT_TIMESTAMP;
                END IF;
                UPDATE ISSUE SET state = i_state, close_timestamp = iclose_timestamp WHERE id = issue_id AND project = project_id
                RETURNING name, description, project INTO ret_name, ret_description, issue_project;
            WHEN (i_name IS NOT NULL AND i_description IS NULL) THEN
                UPDATE ISSUE SET name = i_name WHERE id = issue_id AND project = project_id
                RETURNING name, description, project INTO ret_name, ret_description, issue_project;
            WHEN (i_name IS NULL AND i_description IS NOT NULL) THEN
                UPDATE ISSUE SET description = i_description WHERE id = issue_id AND project = project_id
                RETURNING name, description, project INTO ret_name, ret_description, issue_project;
            ELSE
                UPDATE ISSUE SET name = i_name, description = i_description WHERE id = issue_id AND project = project_id
                RETURNING name, description, project INTO ret_name, ret_description, issue_project;
            END CASE;
        IF (FOUND) THEN
            issue_rep = build_issue_item(issue_project, issue_id);
            RAISE INFO '%', issue_rep;
        END IF;
    END IF;
END$$ LANGUAGE plpgsql;

/*
 * Sets a new issue state after verifying if the desired state is allowed to be set, i.e. if it's in the next state of
 * the current state of the issue in the table PROJECT_STATE_TRAN
 * Returns an issue item
 */
CREATE OR REPLACE PROCEDURE set_issue_state(
    issue_rep OUT JSON,
    project_id INT,
    issue_id INT,
    state_id INT,
    user_id UUID
)
AS
$$
DECLARE
    ret_name        TEXT;
    ret_description TEXT;
    issue_project   INT;
BEGIN
    UPDATE ISSUE
    SET state = state_id
    WHERE (id = (SELECT id FROM ISSUE WHERE project = project_id AND id = issue_id) AND EXISTS(
            SELECT next_state FROM STATE_TRANSITION WHERE next_state = state_id AND state_id IN
            (SELECT id FROM STATE WHERE id = state_id AND project = (SELECT id FROM PROJECT WHERE id = project_id AND "user" = user_id))
        ))
    RETURNING name, description, project INTO ret_name, ret_description, issue_project;
    IF (FOUND) THEN
        issue_rep = build_issue_item(issue_project, issue_id);
    END IF;
END$$ LANGUAGE plpgsql;

/*
 * Builds a json with the issue item representation
 */
CREATE OR REPLACE FUNCTION build_issue_item(project_id INT, issue_id INT)
RETURNS JSON
AS
$$
DECLARE
    issue_name          TEXT;
    issue_description   TEXT;
    issue_state         INT;
BEGIN
    SELECT name, description, state INTO issue_name, issue_description, issue_state
    FROM ISSUE WHERE id = issue_id AND project = project_id;

    RETURN json_build_object(
        'id', issue_id, 'name', issue_name, 'description', issue_description,
        'labels',(SELECT json_agg(json_build_object('id', id, 'name', name)) FROM LABEL WHERE id IN
            (SELECT label FROM ISSUE_LABEL WHERE issue = issue_id)),
        'state', (SELECT json_build_object('id', id, 'name', name) FROM STATE WHERE id = issue_state)
        );
END$$ LANGUAGE plpgsql;

/*
 * Builds a json with the issue representation
 */
CREATE OR REPLACE FUNCTION issue_representation(
    project_id INT,
    issue_id INT,
    uid UUID DEFAULT NULL,
    limit_rows INT DEFAULT NULL,
    skip_rows INT DEFAULT NULL
)
    RETURNS JSON
AS
$$
DECLARE
    comments                 JSON[];
    rec                      RECORD;
    issue_name               TEXT;
    issue_description        TEXT;
    issue_creation_timestamp TIMESTAMP;
    issue_close_timestamp    TIMESTAMP;
    issue_current_state      INT;
BEGIN
     IF (uid IS NOT NULL AND NOT EXISTS(SELECT id FROM ISSUE WHERE project IN (SELECT id FROM PROJECT WHERE id = project_id AND "user" = uid) AND id = issue_id)) THEN
        RETURN NULL;
    END IF;
    FOR rec IN
        SELECT id FROM COMMENT WHERE project = project_id AND issue = issue_id LIMIT limit_rows OFFSET skip_rows
    LOOP
        comments = array_append(comments, comment_representation(project_id, issue_id, rec.id, uid));
    END LOOP;

    SELECT name, description, creation_timestamp, close_timestamp, state
    INTO issue_name, issue_description, issue_creation_timestamp, issue_close_timestamp, issue_current_state
    FROM ISSUE WHERE id = issue_id AND project = project_id;

    RETURN json_build_object(
            'id', issue_id, 'name', issue_name, 'description', issue_description,
            'creationTimestamp', issue_creation_timestamp, 'closeTimestamp', issue_close_timestamp,
            'labels', (SELECT json_agg(json_build_object('id', ID, 'name', name))
             FROM LABEL WHERE id IN (SELECT label FROM ISSUE_LABEL WHERE issue = issue_id)),
            'state', (SELECT json_build_object('id', ID, 'name', name) FROM STATE WHERE ID = issue_current_state),
            'possibleTransitions', (SELECT json_agg(json_build_object('id', ID, 'name', name))
             FROM STATE WHERE ID IN (SELECT NEXT_STATE FROM state_transition WHERE CURRENT_STATE = issue_current_state)),
            'comments', comments,
            'commentsCollectionSize', (SELECT COUNT(id) FROM COMMENT WHERE project = project_id AND issue = issue_id)
        );
END$$ LANGUAGE plpgsql;

/*
 * Builds a json with the issue item representation
 */
CREATE OR REPLACE FUNCTION issues_representation(
    project_id INT,
    uid UUID,
    limit_rows INT,
    skip_rows INT
)
RETURNS JSON
AS
$$
DECLARE
    issues   JSON[];
    rec      RECORD;
BEGIN
    FOR rec IN
        SELECT id FROM ISSUE WHERE project IN (SELECT id FROM PROJECT WHERE id = project_id AND "user" = uid) ORDER BY id
        LIMIT limit_rows OFFSET skip_rows
    LOOP
        issues = array_append(issues, build_issue_item(project_id, rec.id));
    END LOOP;
    RETURN json_build_object('issues', issues, 'issuesCollectionSize',
        (SELECT COUNT(id) FROM ISSUE WHERE project IN (SELECT id FROM PROJECT WHERE id = project_id AND "user" = uid))
    );
END$$ LANGUAGE plpgsql;

/*
 * View that builds the issue representation
 */

CREATE OR REPLACE VIEW ISSUE_REPRESENTATION
AS
SELECT i.id, project, p."user" as i_user, issue_representation(project, i.id) AS issue_rep
FROM ISSUE i INNER JOIN PROJECT p ON i.project = p.id;

/*
 * View with the project characteristics
 */
CREATE OR REPLACE VIEW ISSUE_ITEM AS
SELECT i.id, i.project, p."user" as i_user, build_issue_item(project, i.id) AS issue_item
FROM ISSUE i INNER JOIN PROJECT p ON i.project = p.id;
/*
 * Creates a comment
 * Returns a comment representation
 */
CREATE OR REPLACE PROCEDURE create_comment(
    comment_rep OUT JSON,
    project_id INT,
    issue_id INT,
    user_id UUID,
    issue_comment TEXT
)
AS
$$
DECLARE
    comment_id INT;
BEGIN
    IF EXISTS(SELECT id FROM PROJECT WHERE "user" = user_id) AND
       EXISTS(SELECT id FROM ISSUE WHERE project = project_id) THEN
        INSERT INTO COMMENT(issue, project, comment)
        VALUES (issue_id, project_id, issue_comment)
        RETURNING id INTO comment_id;
        comment_rep = comment_representation(project_id, issue_id, comment_id, user_id);
    END IF;
END$$ LANGUAGE plpgsql;

/*
 * Updates a comment
 * Returns a comment representation
 */
CREATE OR REPLACE PROCEDURE update_comment(
    comment_rep OUT JSON,
    project_id INT,
    issue_id INT,
    comment_id INT,
    user_id UUID,
    new_comment TEXT
)
AS
$$
DECLARE
    ret_issue      INT;
    ret_project    INT;
    ret_comment_id INT;
BEGIN
    UPDATE COMMENT SET comment = new_comment WHERE project = (SELECT id FROM PROJECT WHERE id = project_id AND "user" = user_id)
    AND issue = issue_id AND id = comment_id RETURNING issue, project, id INTO ret_issue, ret_project, ret_comment_id;
    IF (FOUND) THEN
        comment_rep = build_comment_item(ret_project, ret_issue, ret_comment_id);
    END IF;
END$$ LANGUAGE plpgsql;

/*
 * Deletes a comment
 * Returns a comment representation
 */
CREATE OR REPLACE PROCEDURE delete_comment(
    comment_rep OUT JSON,
    project_id INT,
    issue_id INT,
    comment_id INT,
    user_id UUID
)
AS
$$
DECLARE
    json_aux JSON;
BEGIN
    json_aux = build_comment_item(project_id, issue_id, comment_id);
    DELETE FROM COMMENT WHERE project = (SELECT id FROM PROJECT WHERE id = project_id AND "user" = user_id)
    AND issue = issue_id AND id = comment_id;
    IF (FOUND) THEN
        comment_rep = json_aux;
    END IF;
END$$ LANGUAGE plpgsql;


/*
 * Builds a json with the comment representation
 */
CREATE OR REPLACE FUNCTION comment_representation(
    project_id INT,
    issue_id INT,
    comment_id INT,
    user_id UUID
)
    RETURNS JSON
AS
$$
BEGIN
IF (user_id IS NOT NULL AND NOT EXISTS(SELECT id FROM COMMENT WHERE id = comment_id AND issue = issue_id AND
    project IN (SELECT id FROM PROJECT WHERE id = project_id AND "user" = user_id))) THEN
        RETURN NULL;
    END IF;
    RETURN build_comment_item(project_id, issue_id, comment_id);
END$$ LANGUAGE plpgsql;

/*
 * Builds a json with the comment item
 */
CREATE OR REPLACE FUNCTION build_comment_item(project_id INT, issue_id INT, comment_id INT)
    RETURNS JSON
AS
$$
DECLARE
    c_comment         TEXT;
    comment_timestamp TIMESTAMP;
    is_archived       BOOL;
BEGIN
    SELECT comment, timestamp FROM COMMENT WHERE id = comment_id AND issue = issue_id AND project = project_id
    INTO c_comment, comment_timestamp;
    IF (EXISTS (SELECT name FROM ISSUE WHERE project = project_id AND id = issue_id AND state IN (
        SELECT id FROM STATE WHERE project = project_id AND name = 'archived'))) THEN
        is_archived = true;
    END IF;
    RETURN json_build_object('id', comment_id, 'comment', c_comment, 'timestamp', comment_timestamp,
        'isArchived', is_archived);
END$$ LANGUAGE plpgsql;

/*
 * Builds a json with the comment item representation
 */
CREATE OR REPLACE FUNCTION comments_representation(
    project_id INT,
    issue_id INT,
    uid UUID DEFAULT NULL,
    limit_rows INT DEFAULT NULL,
    skip_rows INT DEFAULT NULL
)
RETURNS JSON
AS
$$
DECLARE
    comments   JSON[];
    rec      RECORD;
BEGIN
    FOR rec IN
        SELECT id FROM COMMENT WHERE issue = issue_id AND project IN (SELECT id FROM PROJECT WHERE id = project_id AND "user" = uid) ORDER BY id
        LIMIT limit_rows OFFSET skip_rows
    LOOP
        comments = array_append(comments, build_comment_item(project_id, issue_id, rec.id));
    END LOOP;
    RETURN json_build_object('comments', comments, 'commentsCollectionSize',
        (SELECT COUNT(id) FROM COMMENT WHERE issue = issue_id AND project IN (SELECT id FROM PROJECT WHERE id = project_id AND "user" = uid))
    );
END$$ LANGUAGE plpgsql;
/*
 * View that builds the comment representation
 */
CREATE OR REPLACE VIEW COMMENT_REPRESENTATION
AS
SELECT c.id, project, issue, p."user" as c_user, comment_representation(project, issue, c.id, null) AS comment_rep
FROM COMMENT c INNER JOIN PROJECT p on c.project = p.id;