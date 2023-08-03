# Issue
An **Issue** is a task that needs to be done in the context of a project, such as adding a new functionality, resolve an error, add a test, create a final release. An issue always exists in the context of a project. The **Issue API** allows you to view and manage all the issues of a project owned by the authenticated user. All the **vocabulary** used in the representations is described [**here**](#issue-representations-vocabulary).

## Issue API contents
* [**List issues**](#list-issues)
* [**Create an issue**](#create-an-issue)
* [**Get an issue**](#get-an-issue)
* [**Update an issue**](#update-an-issue)
* [**Delete an issue**](#delete-an-issue)

## List issues
List issues of a project owned by the authenticated user.

```http
GET /projects/{projectId}/issues
```

### Parameters
| Name | Type | In | Required | Description |
|:-:|:-:|:-:|:-:|:-:|
| `projectId` | integer | path | yes | Identifier of the project. |
| `accept` | string | header | no | Setting to `application/vnd.daw+json` is recommended. |
| `page` | integer | query | no | Page number of the results to fetch. **Default:** `0` |

### Response
```http
Status: 200 OK 
```

```json
{
    "class": [ "issue", "collection"],
    "properties": {
        "pageIndex": 0,
        "pageSize": 1,
        "collectionSize": 1
    },
    "entities": [
        {
            "class": [ "issue" ],
            "rel": [ "item" ],
            "properties": {
                "id": 1,
                "name": "Create database procedures",
                "description": "Create database procedures description",
                "labels": [ "test" ],
                "state": "wip"
            },
            "links": [
                { "rel": [ "self" ], "href": "/projects/1/issues/1" }
            ]
        },
        {
            "class": [ "user" ],
            "rel": [ "author" ],
            "properties": {
                "id": "cf128ed3-0d65-42d9-8c96-8ff2e05b3d08",
                "name": "José Bonifácio",
                "email": "joca@gmail.com"
            },
            "links": [
                { "rel": [ "self" ], "href": "/user" }
            ]
        }
    ],
    "actions": [
        {
            "name": "create-issue",
            "title": "Create an issue",
            "method": "POST",
            "href": "/projects/1/issues",
            "type": "application/json",
            "properties": [
                { "name": "name", "type": "string" },
                { "name": "description", "type": "string" },
                { "name": "labels", "type": "array", "itemsType": "number", "required": false }
            ]
        }
    ],
    "links": [
        { "rel": [ "self" ], "href": "/projects/1/issues?page=0" }
    ]
}
```
```http
Status: 400 Bad Request
```
```http
Status: 401 Unauthorized
```
```http
Status: 404 Not Found
```

## Create an issue
Create a new issue.

```http
POST /projects/{projectId}/issues
```
### Parameters:
| Name | Type | In | Required | Description |
|:-:|:-:|:-:|:-:|:-:|
| `projectId` | integer | path | yes | Identifier of the project. |
| `accept` | string | header | no | Setting to `application/vnd.daw+json` is recommended. |
| `content-type` | string | header | yes | Set to `application/json`. |
| `name` | string | body | yes | Name for the issue. |
| `description` | string | body | yes | Description for the issue. |
| `labels` | array of numbers | body | no | Labels for the issue. |

### Response
```http
Status: 201 Created
Location: /projects/{projectId}/issues/{issueId}
```
```json
{
    "class": [ "issue" ],
    "properties": {
        "id": 1,
        "name": "Create database procedures",
        "description": "Create database procedures description",
        "creationTimestamp": "2022-04-08 21:52:47012",
        "labels": [ "new-funcionality" ],
        "state": "todo",
        "possibleTransitions": [
            { "id": 1, "name": "wip" },
            { "id": 2, "name": "closed" } 
        ]
    },
    "entities": [
        {
            "class": [ "user" ],
            "rel": [ "author" ],
            "properties": {
                "id": "cf128ed3-0d65-42d9-8c96-8ff2e05b3d08",
                "name": "José Bonifácio",
                "email": "joca@gmail.com"
            },
            "links": [
                { "rel": [ "self" ], "href": "/user" }
            ]
        }
    ],
    "links": [
        { "rel": [ "self" ], "href": "/projects/1/issues/1" }
    ]
}
```

```http
Status: 400 Bad Request
```
```http
Status: 401 Unauthorized
```
```http
Status: 404 Not Found
```

## Get an issue
Get a certain issue.

```http
GET /projects/{projectId}/issues/{issueId}
```

### Parameters
| Name | Type | In | Required | Description |
|:-:|:-:|:-:|:-:|:-:|
| `projectId` | integer | path | yes | Identifier of the project. |
| `issueId` | integer | path | yes | Identifier of the project issue. |
| `accept` | string | header | no | Setting to `application/vnd.daw+json` is recommended. |
| `content-type` | string | header | yes | Set to `application/json`. |
| `name` | string | body | yes | Name for the issue. |
| `description` | string | body | yes | Description for the issue. |

### Response
```http
Status: 200 OK 
```

```json
{
    "class": [ "issue" ],
    "properties": {
        "id": 1,
        "name": "Create database procedures",
        "description": "Create database procedures description",
        "creationTimestamp": "2022-04-08 21:52:47012",
        "closeTimestamp": "2022-04-12 22:21:31312",
        "labels": [ "new-funcionality" ],
        "state": "closed",
        "possibleTransitions": [
            { "id": 1, "name": "wip" },
            { "id": 2, "name": "archived" } 
        ]
    },
    "entities": [
        {
            "class": [ "comment", "collection" ],
            "rel": [ "issue-comments" ],
            "properties": {
                "pageIndex": 0,
                "pageSize": 1,
                "collectionSize": 1
            },
            "entities": [
                {
                    "class": [ "comment" ],
                    "rel": [ "item" ],
                    "properties": {
                        "id": 1,
                        "comment": "Set the transaction isolation level",
                        "timestamp": "2022-04-09 12:43:22213"
                    },
                    "entities": [
                        {
                            "class": [ "user" ],
                            "rel": [ "author" ],
                            "properties": {
                                "id": "cf128ed3-0d65-42d9-8c96-8ff2e05b3d08",
                                "name": "José Bonifácio",
                                "email": "joca@gmail.com"
                            },
                            "links": [
                                { "rel": [ "self" ], "href": "/user" }
                            ]
                        }
                    ],
                    "actions": [
                        {
                            "name": "delete-comment",
                            "title": "Delete comment",
                            "method": "DELETE",
                            "href": "/projects/1/issues/1/comments/1"
                        },
                        {
                            "name": "edit-comment",
                            "title": "Edit comment",
                            "method": "PUT",
                            "href": "/projects/1/issues/1/comments/1",
                            "type": "application/json",
                            "properties": [
                                { "name": "comment", "type": "string" }
                            ]
                        }
                    ],
                    "links": [
                        { "rel": [ "self" ], "href": "/projects/1/issues/1/comments/1" } 
                    ]
                }
            ],
            "actions": [
                {
                    "name": "create-comment",
                    "title": "Create a comment",
                    "method": "POST",
                    "href": "/projects/1/issues/1/comments",
                    "type": "application/json",
                    "properties": [
                        { "name": "comment", "type": "string" }
                    ]
                }
            ],
            "links": [
                { "rel": [ "self" ], "href": "/projects/1/issues/1/comments?page=0" }
            ]
        },
        {
            "class": [ "user" ],
            "rel": [ "author" ],
            "properties": {
                "id": "cf128ed3-0d65-42d9-8c96-8ff2e05b3d08",
                "name": "José Bonifácio",
                "email": "joca@gmail.com"
            },
            "links": [
                { "rel": [ "self" ], "href": "/user" }
            ]
        }
    ],
    "actions": [
        {
            "name": "delete-issue",
            "title": "Delete issue",
            "method": "DELETE",
            "href": "/projects/1/issues/1"
        },
        {
            "name": "edit-issue",
            "title": "Edit issue",
            "method": "PUT",
            "href": "/projects/1/issues/1",
            "type": "application/json",
            "properties": [
                { "name": "name", "type": "string" },
                { "name": "description", "type": "string" }
                { "name": "state", "type": "number" } 
            ]
        }
    ],
    "links": [
        { "rel": [ "self" ], "href": "/projects/1/issues/1" }
    ]
}
```
```http
Status: 400 Bad Request
```
```http
Status: 401 Unauthorized
```
```http
Status: 404 Not Found
```

## Update an issue
Update a certain issue.

```http
PUT /projects/{projectId}/issues/{issueId}
```

### Parameters
| Name | Type | In | Required | Description |
|:-:|:-:|:-:|:-:|:-:|
| `projectId` | integer | path | yes | Identifier of the project. |
| `issueId` | integer | path | yes | Identifier of the project issue. |
| `accept` | string | header | no | Setting to `application/vnd.daw+json` is recommended. |
| `content-type` | string | header | yes | Set to `application/json`. |
| `name` | string | body | no | New name for the issue. |
| `description` | string | body | no | New description for the issue. |
| `state` | integer | body | no | New state for the issue. |

**Notice:** At least one of the body parameters **should** be inserted.

### Response
```http
Status: 200 OK 
```

```json
{
    "class": [ "issue" ],
    "properties": {
        "id": 1,
        "name": "Create database procedures",
        "description": "Create database procedures description edited",
        "labels": [ "test" ],
        "state": "archived"
    },
    "entities": [
        {
            "class": [ "user" ],
            "rel": [ "author" ],
            "properties": {
                "id": "cf128ed3-0d65-42d9-8c96-8ff2e05b3d08",
                "name": "José Bonifácio",
                "email": "joca@gmail.com"
            },
            "links": [
                { "rel": [ "self" ], "href": "/user" }
            ]
        }
    ],
    "links": [
        { "rel": [ "self" ], "href": "/projects/1/issues/1" }
    ]
}
```

```http
Status: 400 Bad Request
```
```http
Status: 401 Unauthorized
```
```http
Status: 404 Not Found
```

## Delete an issue
Delete a certain issue.

```http
DELETE /projects/{projectId}/issues/{issueId}
```

### Parameters
| Name | Type | In | Required | Description |
|:-:|:-:|:-:|:-:|:-:|
| `projectId` | integer | path | yes | Identifier of the project. |
| `issueId` | integer | path | yes | Identifier of the porject issue. |
| `accept` | string | header | no | Setting to `application/vnd.daw+json` is recommended. |

### Response
```http
Status: 200 OK 
```

```json
{
    "class": [ "issue" ],
    "properties": {
        "id": 1,
        "name": "Create database procedures",
        "description": "Create database procedures description",
        "labels": [ "test" ],
        "state": "wip"
    },
    "entities": [
        {
            "class": [ "user" ],
            "rel": [ "author" ],
            "properties": {
                "id": "cf128ed3-0d65-42d9-8c96-8ff2e05b3d08",
                "name": "José Bonifácio"
            },
            "links": [
                { "rel": [ "self" ], "href": "/user" }
            ]
        }
    ],
    "links": [
        { "rel": [ "self" ], "href": "/projects/1/issues/1" },
        { "rel": [ "issues" ], "href": "/projects/1/issues" }
    ]
}
```

```http
Status: 400 Bad Request
```
```http
Status: 401 Unauthorized
```
```http
Status: 404 Not Found
```

## Issue representations vocabulary
| Name | Type | Description |
|:-:|:-:|:-:|
| `id` | integer | Identifier of the issue. **Stable** and **unique for each project, but not globally**. |
| `name` | string | Name of the issue. |
| `description` | string | Description of the issue. |
| `state` | string | Current issue state. |
| `possibleTransitions` | array of objects | Possible states transitions based in the current state of the issue. The objects are composed by the identifier and the name of the state that is possible to transition to. |
| `labels` | array of strings | Labels associated with an issue. |
| `creationTimestamp` | string | Timestamp associated to the creation of an issue. |
| `closeTimestamp` | string | Timestamp associated to the closing of an issue. |

### Domain specific link relations
| Name | Description |
|:-:|:-:|
| `issues-project` | Project in which the issues belong. |
| `issue-comments` | Issue in which the comments belong. |
| `issues` | Representation of all the issues in a determined project. |

### Domain specific errors
* `arquived-issue`: Error described in the [Comment documentation](Comment.md#domain-specific-errors). 

The **documentation** for the `media-type`, `classes`, `standard link relations` and `generic errors` used in the representations are described [**here**](../README.md).