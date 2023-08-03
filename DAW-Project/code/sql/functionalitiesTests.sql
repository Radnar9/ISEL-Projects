/*
 * Script to test all the implemented functionalities
 */

/*
 * Auxiliary function to verify the values inside a json object
 * Returns true if the value is equal to the expected and false otherwise
 */
CREATE OR REPLACE FUNCTION assert_json_value(json_val JSON, key TEXT, expected_value TEXT)
RETURNS BOOLEAN
AS
$$
BEGIN
    RETURN (json_val->>key) LIKE expected_value;
END$$LANGUAGE plpgsql;

/*
 * Auxiliary function to verify if a json array as the expected size
 * Returns true if the value is equal to the expected and false otherwise
 */
CREATE OR REPLACE FUNCTION assert_json_array_size(json_val JSON, key TEXT, expected_size INT)
RETURNS BOOLEAN
AS
$$
BEGIN
    RETURN array_length(string_to_array((json_val->>key), '}'), 1) - 1 = expected_size;
END$$LANGUAGE plpgsql;


/*
 * Auxiliary function to verify if the values are not null inside the json object
 * Returns true if is null and false otherwise
 */
CREATE OR REPLACE FUNCTION assert_json_is_not_null(json_val JSON, key TEXT)
RETURNS BOOLEAN
AS
$$
BEGIN
    RETURN (json_val->>key) IS NOT NULL;
END$$LANGUAGE plpgsql;

/*
 * Creation of user
 */
DO
$$
DECLARE
    name TEXT = 'Diogo Afonso';
    email TEXT = 'diogoafonso@gmail.com';
    password TEXT = 'omundosabeque#1906';
    user_rep JSON;

BEGIN
    RAISE INFO '---| User creation test |---';

    CALL create_user(name, email, password, user_rep);

    IF (user_rep->>'id' IS NOT NULL AND
        assert_json_value(user_rep, 'name', name) AND
        assert_json_value(user_rep, 'email', email)) THEN
        RAISE INFO '-> Test succeeded!';
    ELSE
        RAISE INFO '-> Test failed!';
    END IF;
    ROLLBACK;
END$$;

/*
 * Deletion of a user
 */
DO
$$
DECLARE
    id UUID = 'cf128ed3-0d65-42d9-8c96-8ff2e05b3d08';
    name TEXT = 'José Bonifácio';
    email TEXT = 'joca@gmail.com';
    user_rep JSON;
BEGIN
    RAISE INFO '---| User deletion test |---';

    CALL delete_user(user_rep, uemail => email);

    IF (assert_json_value(user_rep, 'id', id::TEXT) AND
        assert_json_value(user_rep, 'name', name) AND
        assert_json_value(user_rep, 'email', email)) THEN
        RAISE INFO '-> Test succeeded!';
    ELSE
        RAISE INFO '-> Test failed! One or more returned values are different than the expected.';
    END IF;
    ROLLBACK;
END$$;

/*
 * Updates the user's information with a new name and email
 */
DO
$$
DECLARE
    id UUID = 'cf128ed3-0d65-42d9-8c96-8ff2e05b3d08';
    name TEXT = 'Fernando Santos';
    email TEXT = 'santosnocatar@gmail.com';
    user_rep JSON;
BEGIN
    RAISE INFO '---| User update name and email test |---';

    CALL update_user(user_rep, id, name, email);

    IF (assert_json_value(user_rep, 'id', id::TEXT) AND
        assert_json_value(user_rep, 'name', name) AND
        assert_json_value(user_rep, 'email', email)) THEN
        RAISE INFO '-> Test succeeded!';
    ELSE
        RAISE INFO '-> Test failed! One or more returned values are different than the expected.';
    END IF;
    ROLLBACK;
END$$;

/*
 * Updates the user's information with only a new name
 */
DO
$$
DECLARE
    id UUID = 'cf128ed3-0d65-42d9-8c96-8ff2e05b3d08';
    name TEXT = 'Fernando Santos';
    email TEXT = 'joca@gmail.com';
    user_rep JSON;
BEGIN
    RAISE INFO '---| User update name test |---';

    CALL update_user(user_rep, id, name);

    IF (assert_json_value(user_rep, 'id', id::TEXT) AND
        assert_json_value(user_rep, 'name', name) AND
        assert_json_value(user_rep, 'email', email)) THEN
        RAISE INFO '-> Test succeeded!';
    ELSE
        RAISE INFO '-> Test failed! One or more returned values are different than the expected.';
    END IF;
    ROLLBACK;
END$$;

/*
 * Updates the user's information with only a new email
 */
DO
$$
DECLARE
    id UUID = 'cf128ed3-0d65-42d9-8c96-8ff2e05b3d08';
    name TEXT = 'José Bonifácio';
    email TEXT = 'jocaodazona@gmail.com';
    user_rep JSON;
BEGIN
    RAISE INFO '---| User update email test |---';

    CALL update_user(user_rep, id, uemail => email);

    IF (assert_json_value(user_rep, 'id', id::TEXT) AND
        assert_json_value(user_rep, 'name', name) AND
        assert_json_value(user_rep, 'email', email)) THEN
        RAISE INFO '-> Test succeeded!';
    ELSE
        RAISE INFO '-> Test failed! One or more returned values are different than the expected.';
    END IF;
    ROLLBACK;
END$$;

/*
 * Updates the user's information with only a new email
 */
DO
$$
DECLARE
    id UUID = 'cf128ed3-0d65-42d9-8c96-8ff2e05b3d08';
    name TEXT = 'José Bonifácio';
    email TEXT = 'joca@gmail.com';
    password TEXT = 'soubuefixe';
    user_rep JSON;
BEGIN
    RAISE INFO '---| User credentials validation test |---';

    CALL validate_user_credentials(user_rep, email, password);

    IF (assert_json_value(user_rep, 'id', id::TEXT) AND
        assert_json_value(user_rep, 'name', name) AND
        assert_json_value(user_rep, 'email', email)) THEN
        RAISE INFO '-> Test succeeded!';
    ELSE
        RAISE INFO '-> Test failed! One or more returned values are different than the expected.';
    END IF;
    ROLLBACK;
END$$;

/*
 * Creates a new project
 */
DO
$$
DECLARE
    user_id UUID = 'cf128ed3-0d65-42d9-8c96-8ff2e05b3d08';
    name TEXT = 'Construção de rede no Estádio da Luz';
    description TEXT = 'Construção de rede para proteger do arremesso de materiais perigosos para o relvado';
    labels TEXT[] = ARRAY['test1', 'test2', 'test3'];
    states TEXT[] = ARRAY['test1', 'test2', 'test3'];
    initial_state TEXT = 'test3';
    states_transitions TEXT[] = ARRAY['test1', 'test2', 'test2', 'test3'];
    project_rep JSON;
BEGIN
    RAISE INFO '---| Project creation test |---';

    CALL create_project(name, description, user_id, labels, states,
        initial_state, states_transitions, project_rep);

    IF (assert_json_is_not_null(project_rep, 'id') AND
        assert_json_value(project_rep, 'name', name) AND
        assert_json_value(project_rep, 'description', description) AND
        assert_json_is_not_null(project_rep, 'labels') AND
        assert_json_is_not_null(project_rep, 'states') AND
        assert_json_is_not_null(project_rep, 'initialState') AND
        assert_json_is_not_null(project_rep, 'statesTransitions') AND
        NOT assert_json_is_not_null(project_rep, 'issues')) THEN
        RAISE INFO '-> Test succeeded!';
    ELSE
        RAISE INFO '-> Test failed! One or more returned values are different than the expected.';
    END IF;
    ROLLBACK;
END$$;

/*
 * Updates the project's information with a new name and description
 */
DO
$$
DECLARE
    user_id UUID = 'cf128ed3-0d65-42d9-8c96-8ff2e05b3d08';
    project_id INT = 1;
    name TEXT = 'CapriMetro Project';
    description TEXT = 'Construção do metro para levar as pessoas à praia da Costa mais facilmente';
    project_rep JSON;
BEGIN
    RAISE INFO '---| Project update with new name and description test |---';

    CALL update_project(project_id, user_id, project_rep, name, description);

    IF (assert_json_is_not_null(project_rep, 'id') AND
        assert_json_value(project_rep, 'name', name) AND
        assert_json_value(project_rep, 'description', description)) THEN
        RAISE INFO '-> Test succeeded!';
    ELSE
        RAISE INFO '-> Test failed! One or more returned values are different than the expected.';
    END IF;
    ROLLBACK;
END$$;

/*
 * Updates the project's information with a the same name and description
 */
DO
$$
DECLARE
    user_id UUID = 'cf128ed3-0d65-42d9-8c96-8ff2e05b3d08';
    project_id INT = 1;
    name TEXT = 'Caparica Metropolitano';
    description TEXT =  'Expansão do metro para a zona da Costa da Caparica';
    project_rep JSON;
BEGIN
    RAISE INFO '---| Project update name and description same values test |---';

    CALL update_project(project_id, user_id, project_rep, name, description);

    IF (assert_json_is_not_null(project_rep, 'id') AND
        assert_json_value(project_rep, 'name', name) AND
        assert_json_value(project_rep, 'description', description)) THEN
        RAISE INFO '-> Test succeeded!';
    ELSE
        RAISE INFO '-> Test failed! One or more returned values are different than the expected.';
    END IF;
    ROLLBACK;
END$$;

/*
 * Deletes a project when it belongs to the user
 */
DO
$$
DECLARE
    user_id UUID = 'cf128ed3-0d65-42d9-8c96-8ff2e05b3d08';
    project_id INT = 1;
    name TEXT = 'Caparica Metropolitano';
    description TEXT = 'Expansão do metro para a zona da Costa da Caparica';
    project_rep JSON;
BEGIN
    RAISE INFO '---| Project deletion test |---';

    CALL delete_project(project_id, user_id, project_rep);

    IF (assert_json_is_not_null(project_rep, 'id') AND
        assert_json_value(project_rep, 'name', name) AND
        assert_json_value(project_rep, 'description', description)) THEN
        RAISE INFO '-> Test succeeded!';
    ELSE
        RAISE INFO '-> Test failed! One or more returned values are different than the expected.';
    END IF;
    ROLLBACK;
END$$;

/*
 * Doesn't delete a project when it doesn't belong to the user
 */
DO
$$
DECLARE
    user_id UUID = 'cf128ed3-0d65-42d9-8c96-8ff2e05b3d08';
    project_id INT = 2;
    project_rep JSON;
BEGIN
    RAISE INFO '---| Project deletion that does not belong to user test |---';

    CALL delete_project(project_id, user_id, project_rep);

    IF (project_rep IS NULL) THEN
        RAISE INFO '-> Test succeeded!';
    ELSE
        RAISE INFO '-> Test failed!';
    END IF;
    ROLLBACK;
END$$;

/*
 * Set project states
 */
DO
$$
DECLARE
    project_id INT = 1;
    states TEXT[] = ARRAY['test1', 'test2', 'test3'];
    initial_state TEXT = 'todo';
    states_size_expected INT = 7;
    project_states JSON;
BEGIN
    RAISE INFO '---| Project set states test |---';

    CALL set_project_states(project_id, states, initial_state, project_states);

    IF (assert_json_array_size(project_states, 'states', states_size_expected)) THEN
        RAISE INFO '-> Test succeeded!';
    ELSE
        RAISE INFO '-> Test failed! Returned value is different than the expected.';
    END IF;
    ROLLBACK;
END$$;

/*
 * Set project labels
 */
DO
$$
DECLARE
    project_id INT = 1;
    user_id UUID = 'cf128ed3-0d65-42d9-8c96-8ff2e05b3d08';
    labels_size_expected INT = 6;
    labels TEXT[] = ARRAY['test1', 'test2', 'test3'];
    project_labels JSON;
BEGIN
    RAISE INFO '---| Project set labels test |---';

    CALL set_project_labels(project_id, user_id, labels, project_labels);

    IF (assert_json_array_size(project_labels, 'labels', labels_size_expected)) THEN
        RAISE INFO '-> Test succeeded!';
    ELSE
        RAISE INFO '-> Test failed! Returned value is different than the expected.';
    END IF;
    ROLLBACK;
END$$;

/*
 * Set project state transitions
 */
DO
$$
DECLARE
    project_id INT = 1;
    transitions TEXT[] = ARRAY['archived', 'closed', 'wip', 'todo'];
    transitions_size_expected INT = 6;
BEGIN
    RAISE INFO '---| Project set state transitions test |---';

    CALL set_state_transitions(project_id, transitions);

    IF ((SELECT COUNT(project) FROM STATE_TRANSITION WHERE project = project_id) = transitions_size_expected) THEN
        RAISE INFO '-> Test succeeded!';
    ELSE
        RAISE INFO '-> Test failed! Returned value is different than the expected.';
    END IF;
    ROLLBACK;
END$$;


/*
 * Project representation view
 */
DO
$$
DECLARE
    project_id INT = 1;
    name TEXT = 'Caparica Metropolitano';
    description TEXT = 'Expansão do metro para a zona da Costa da Caparica';
    project JSON;
BEGIN
    RAISE INFO '---| Project representation view test |---';

    SELECT project_rep FROM PROJECT_REPRESENTATION WHERE id = project_id INTO project;
    IF (assert_json_is_not_null(project, 'id') AND
        assert_json_value(project, 'name', name) AND
        assert_json_value(project, 'description', description) AND
        assert_json_is_not_null(project, 'labels') AND
        assert_json_is_not_null(project, 'states') AND
        assert_json_is_not_null(project, 'initialState') AND
        assert_json_is_not_null(project, 'statesTransitions') AND
        assert_json_is_not_null(project, 'issues')) THEN
        RAISE INFO '-> Test succeeded!';
    ELSE
        RAISE INFO '-> Test failed! Returned value is different than the expected.';
    END IF;
    ROLLBACK;
END$$;

/*
 * Project item view
 */
DO
$$
DECLARE
    project_id INT = 1;
    project_name TEXT = 'Caparica Metropolitano';
    project_description TEXT = 'Expansão do metro para a zona da Costa da Caparica';
    ret_project_id INT;
    ret_project_name TEXT;
    ret_project_description TEXT;
BEGIN
    RAISE INFO '---| Project item view test |---';

    SELECT id, name, description FROM PROJECT_ITEM WHERE id = project_id
        INTO ret_project_id, ret_project_name, ret_project_description;

    IF (ret_project_id = project_id)
           AND (ret_project_name = project_name)
           AND (ret_project_description = project_description) THEN
        RAISE INFO '-> Test succeeded!';
    ELSE
        RAISE INFO '-> Test failed! Returned value is different than the expected.';
    END IF;
    ROLLBACK;
END$$;

/*
 * User item view
 */
DO
$$
DECLARE
    user_id UUID = 'cf128ed3-0d65-42d9-8c96-8ff2e05b3d08';
    user_name TEXT = 'José Bonifácio';
    user_email TEXT = 'joca@gmail.com';
    ret_user_id UUID;
    ret_user_name TEXT;
    ret_user_email TEXT;
BEGIN
    RAISE INFO '---| User item view test |---';

    SELECT id, name, email FROM USER_ITEM WHERE id = user_id
        INTO ret_user_id, ret_user_name, ret_user_email;

    IF (user_id = ret_user_id)
           AND (user_name = ret_user_name)
           AND (user_email = ret_user_email) THEN
        RAISE INFO '-> Test succeeded!';
    ELSE
        RAISE INFO '-> Test failed! Returned value is different than the expected.';
    END IF;
    ROLLBACK;
END$$;

/*
 * Get issue labels names
 */
DO
$$
DECLARE
    project_id INT = 1;
    issue_id INT = 1;
    expected_labels TEXT[] = ARRAY['new-functionality'];
    ret_issue_labels TEXT[];
BEGIN
    RAISE INFO '---| Get issue labels names test |---';

    ret_issue_labels = get_issue_labels_names(project_id, issue_id);
    IF (ret_issue_labels = expected_labels) THEN
        RAISE INFO '-> Test succeeded!';
    ELSE
        RAISE INFO '-> Test failed! Returned value is different than the expected.';
    END IF;
    ROLLBACK;
END$$;

/*
 * Creates a new issue
 */
DO
$$
DECLARE
    project_id INT = 1;
    user_id UUID = 'cf128ed3-0d65-42d9-8c96-8ff2e05b3d08';
    issue_name TEXT = 'Test name';
    issue_description TEXT = 'Test description';
    issue_rep JSON;
BEGIN
    RAISE INFO '---| Issue creation test |---';

    CALL create_issue(issue_rep, project_id, user_id, issue_name, issue_description);
    IF (assert_json_is_not_null(issue_rep, 'id') AND
        assert_json_value(issue_rep, 'name', issue_name) AND
        assert_json_value(issue_rep, 'description', issue_description) AND
        assert_json_is_not_null(issue_rep, 'creationTimestamp') AND
        assert_json_is_not_null(issue_rep, 'state') AND
        assert_json_is_not_null(issue_rep, 'possibleTransitions') AND
        NOT assert_json_is_not_null(issue_rep, 'labels') AND
        NOT assert_json_is_not_null(issue_rep, 'closeTimestamp') AND
        NOT assert_json_is_not_null(issue_rep, 'comments')) THEN
        RAISE INFO '-> Test succeeded!';
    ELSE
        RAISE INFO '-> Test failed! One or more returned values are different than the expected.';
    END IF;
    ROLLBACK;
END$$;

/*
 * Delete an issue
 */
DO
$$
DECLARE
    issue_id INT = 1;
    project_id INT = 1;
    user_id UUID = 'cf128ed3-0d65-42d9-8c96-8ff2e05b3d08';
    issue_name TEXT = 'Construir perímetro de segurança';
    issue_description TEXT = 'Projetar e implementar um perímetro de segurança para a inicialização das obras';
    issue_rep JSON;
BEGIN
    RAISE INFO '---| Issue delete test |---';

    CALL delete_issue(issue_rep, project_id, issue_id, user_id);
    IF ((assert_json_is_not_null(issue_rep, 'id') AND
        assert_json_value(issue_rep, 'name', issue_name) AND
        assert_json_value(issue_rep, 'description', issue_description))) AND
        assert_json_is_not_null(issue_rep, 'labels') AND
        assert_json_is_not_null(issue_rep, 'state') AND
        assert_json_is_not_null(issue_rep, 'possibleTransitions') THEN
        RAISE INFO '-> Test succeeded!';
    ELSE
        RAISE INFO '-> Test failed! Returned value is different than the expected.';
    END IF;
    ROLLBACK;
END$$;

/*
 * Update an issue
 */
DO
$$
DECLARE
    issue_id INT = 1;
    project_id INT = 1;
    user_id UUID = 'cf128ed3-0d65-42d9-8c96-8ff2e05b3d08';
    issue_name TEXT = 'Test name';
    issue_description TEXT = 'Test description';
    issue_rep JSON;
BEGIN
    RAISE INFO '---| Issue update test |---';

    CALL update_issue(issue_rep, project_id, issue_id, user_id, issue_name, issue_description);
    IF (assert_json_is_not_null(issue_rep, 'id') AND
        assert_json_value(issue_rep, 'name', issue_name) AND
        assert_json_value(issue_rep, 'description', issue_description))THEN
        RAISE INFO '-> Test succeeded!';
    ELSE
        RAISE INFO '-> Test failed! Returned value is different than the expected.';
    END IF;
    ROLLBACK;
END$$;

/*
 * Set issue state
 */
DO
$$
DECLARE
    issue_id INT = 1;
    project_id INT = 1;
    user_id UUID = 'cf128ed3-0d65-42d9-8c96-8ff2e05b3d08';
    state_expected TEXT = '{"id" : 4, "name" : "wip"}';
    issue_rep JSON;
BEGIN
    RAISE INFO '---| Issue state update test |---';

    CALL set_issue_state(issue_rep, project_id, issue_id, 4, user_id);
    IF (assert_json_is_not_null(issue_rep, 'id') AND
        assert_json_value(issue_rep, 'state', state_expected)) THEN
        RAISE INFO '-> Test succeeded!';
    ELSE
        RAISE INFO '-> Test failed! Returned value is different than the expected.';
    END IF;
    ROLLBACK;
END$$;

/*
 * Issue representation view
 */
DO
$$
DECLARE
    project_id INT = 1;
    issue_id INT = 1;
    user_id UUID = 'cf128ed3-0d65-42d9-8c96-8ff2e05b3d08';
    rep_issue JSON;
    issue_name TEXT = 'Construir perímetro de segurança';
    issue_description TEXT = 'Projetar e implementar um perímetro de segurança para a inicialização das obras';
BEGIN
    RAISE INFO '---| Issue representation view test |---';

    SELECT issue_rep FROM ISSUE_REPRESENTATION WHERE id = issue_id AND project = project_id AND i_user = user_id
        INTO rep_issue;
    IF (assert_json_is_not_null(rep_issue, 'id') AND
        assert_json_value(rep_issue, 'name', issue_name) AND
        assert_json_value(rep_issue, 'description', issue_description) AND
        assert_json_is_not_null(rep_issue, 'creationTimestamp') AND
        NOT assert_json_is_not_null(rep_issue, 'closeTimestamp') AND
        assert_json_is_not_null(rep_issue, 'labels') AND
        assert_json_is_not_null(rep_issue, 'state') AND
        assert_json_is_not_null(rep_issue, 'possibleTransitions')) THEN
        RAISE INFO '-> Test succeeded!';
    ELSE
        RAISE INFO '-> Test failed! Returned value is different than the expected.';
    END IF;
    ROLLBACK;
END$$;

/*
 * Issue item view
 */
DO
$$
DECLARE
    project_id INT = 1;
    issue_id INT = 1;
    user_id UUID = 'cf128ed3-0d65-42d9-8c96-8ff2e05b3d08';
    issue_name TEXT = 'Construir perímetro de segurança';
    issue_description TEXT = 'Projetar e implementar um perímetro de segurança para a inicialização das obras';
    ret_issue_item JSON;
BEGIN
    RAISE INFO '---| Issue item view test |---';

    SELECT issue_item FROM ISSUE_ITEM WHERE id = issue_id AND project = project_id AND i_user = user_id
        INTO ret_issue_item;
        IF (assert_json_is_not_null(ret_issue_item, 'id') AND
        assert_json_value(ret_issue_item, 'name', issue_name) AND
        assert_json_value(ret_issue_item, 'description', issue_description) AND
        assert_json_is_not_null(ret_issue_item, 'labels') AND
        assert_json_is_not_null(ret_issue_item, 'possibleTransitions') AND
        assert_json_is_not_null(ret_issue_item, 'state')) THEN
        RAISE INFO '-> Test succeeded!';
    ELSE
        RAISE INFO '-> Test failed! Returned value is different than the expected.';
    END IF;
    ROLLBACK;
END$$;

/*
 * Creates a new comment
 */
DO
$$
DECLARE
    project_id INT = 1;
    issue_id INT = 1;
    user_id UUID = 'cf128ed3-0d65-42d9-8c96-8ff2e05b3d08';
    issue_comment TEXT = 'Test comment';
    comment_rep JSON;
BEGIN
    RAISE INFO '---| Comment creation test |---';

    CALL create_comment(comment_rep, project_id, issue_id, user_id, issue_comment);
    IF (assert_json_is_not_null(comment_rep, 'id') AND
        assert_json_value(comment_rep, 'comment', issue_comment) AND
        assert_json_is_not_null(comment_rep, 'timestamp')) THEN
        RAISE INFO '-> Test succeeded!';
    ELSE
        RAISE INFO '-> Test failed! One or more returned values are different than the expected.';
    END IF;
    ROLLBACK;
END$$;

/*
 * Update a comment
 */
DO
$$
DECLARE
    project_id INT = 1;
    issue_id INT = 1;
    comment_id INT = 1;
    user_id UUID = 'cf128ed3-0d65-42d9-8c96-8ff2e05b3d08';
    new_comment TEXT = 'New test comment';
    comment_rep JSON;
BEGIN
    RAISE INFO '---| Comment update test |---';

    CALL update_comment(comment_rep, project_id, issue_id, comment_id, user_id, new_comment);
    IF (assert_json_is_not_null(comment_rep, 'id') AND
        assert_json_value(comment_rep, 'comment', new_comment)) THEN
        RAISE INFO '-> Test succeeded!';
    ELSE
        RAISE INFO '-> Test failed! One or more returned values are different than the expected.';
    END IF;
    ROLLBACK;
END$$;

/*
 * Deletes a comment
 */
DO
$$
DECLARE
    project_id INT = 1;
    issue_id INT = 1;
    comment_id INT = 1;
    user_id UUID = 'cf128ed3-0d65-42d9-8c96-8ff2e05b3d08';
    comment TEXT = 'Investimento muito elevado, deviam pensar melhor';
    timestamp TEXT = '2022-04-08T21:52:47.01262';
    comment_rep JSON;
BEGIN
    RAISE INFO '---| Comment update test |---';

    CALL delete_comment(comment_rep, project_id, issue_id, comment_id, user_id);
    IF (assert_json_is_not_null(comment_rep, 'id') AND
        assert_json_value(comment_rep, 'comment', comment) AND
        assert_json_value(comment_rep, 'timestamp', timestamp)) THEN
        RAISE INFO '-> Test succeeded!';
    ELSE
        RAISE INFO '-> Test failed! One or more returned values are different than the expected.';
    END IF;
    ROLLBACK;
END$$;

/*
 * Deletes a comment, user invalid
 */
DO
$$
DECLARE
    project_id INT = 1;
    issue_id INT = 1;
    comment_id INT = 1;
    user_invalid_id UUID = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';
    comment_rep JSON;
BEGIN
    RAISE INFO '---| Comment update test |---';

    CALL delete_comment(comment_rep, project_id, issue_id, comment_id, user_invalid_id);
    IF (NOT assert_json_is_not_null(comment_rep, 'id')) THEN
        RAISE INFO '-> Test succeeded!';
    ELSE
        RAISE INFO '-> Test failed! One or more returned values are different than the expected.';
    END IF;
    ROLLBACK;
END$$;

/*
 * Comment representation view
 */
DO
$$
DECLARE
    project_id INT = 1;
    issue_id INT = 1;
    comment_id INT = 1;
    user_id UUID = 'cf128ed3-0d65-42d9-8c96-8ff2e05b3d08';
    comment_expected TEXT = 'Investimento muito elevado, deviam pensar melhor';
    timestamp TEXT = '2022-04-08T21:52:47.01262';
    comment JSON;
BEGIN
    RAISE INFO '---| Comment representation view test |---';

    SELECT comment_rep FROM COMMENT_REPRESENTATION
    WHERE id = comment_id AND project = project_id AND issue = issue_id AND c_user = user_id INTO comment;

    IF (assert_json_is_not_null(comment, 'id') AND
        assert_json_value(comment, 'comment', comment_expected) AND
        assert_json_value(comment, 'timestamp', timestamp)) THEN
        RAISE INFO '-> Test succeeded!';
    ELSE
        RAISE INFO '-> Test failed! Returned value is different than the expected.';
    END IF;
    ROLLBACK;
END$$;

/*
 * Comment representation failed, user invalid
 */
DO
$$
DECLARE
    project_id INT = 1;
    issue_id INT = 1;
    comment_id INT = 1;
    user_invalid_id UUID = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';
    comment JSON;
BEGIN
    RAISE INFO '---| Comment representation view test |---';

    SELECT comment_rep FROM COMMENT_REPRESENTATION
    WHERE id = comment_id AND project = project_id AND issue = issue_id AND c_user = user_invalid_id INTO comment;

    IF (NOT assert_json_is_not_null(comment, 'id')) THEN
        RAISE INFO '-> Test succeeded!';
    ELSE
        RAISE INFO '-> Test failed! Returned value is different than the expected.';
    END IF;
    ROLLBACK;
END$$;

