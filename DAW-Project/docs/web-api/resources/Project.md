# Project
A **Project** is a long-running development activity. The **Project API** allows you to create, view and manage all the projects owned by the authenticated user. All the **vocabulary** used in the project representations is described [**here**](#project-representations-vocabulary).

## Project API contents
* [**List projects**](#list-projects)
* [**Create a project**](#create-a-project)
* [**Get a project**](#get-a-project)
* [**Update a project**](#update-a-project)
* [**Delete a project**](#delete-a-project)
* [**Add label to a project**](#add-label-to-a-project)

## List projects
List projects owned by the authenticated user.

```http
GET /projects
```

### Parameters
| Name | Type | In | Required | Description |
|:-:|:-:|:-:|:-:|:-:|
| `accept` | string | header | no | Setting to `application/vnd.daw+json` is recommended. |
| `page` | integer | query | no | Page number of the results to fetch. **Default:** `0` |

### Response
```http
Status: 200 OK 
```

```json
{
    "class": [ "project", "collection" ],
    "properties": {
        "pageIndex": 0,
        "pageSize": 1,
        "collectionSize": 1
    },
    "entities": [
        {
            "class": [ "project" ],
            "rel": [ "item" ],
            "properties": {
                "id": 1,
                "name": "DAW Project",
                "description": "DAW Project description"
            },
            "links": [
                { "rel": [ "self" ], "href": "/projects/1" }
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
            "name": "create-project",
            "title": "Create a project",
            "method": "POST",
            "href": "/projects",
            "type": "application/json",
            "properties": [
                { "name": "name", "type": "string" },
                { "name": "description", "type": "string" },
                { "name": "labels", "type": "array", "itemsType": "string" },
                { "name": "states", "type": "array", "itemsType": "string" },
                { "name": "statesTransitions", "type": "array", "itemsType": "string" },
                { "name": "initialState", "type": "string"}
            ]
        }
    ],
    "links": [
        { "rel": [ "self" ], "href": "/projects?page=0" }
    ]
}
```
```http
Status: 400 Bad Request
```
```http
Status: 401 Unauthorized
```

## Create a project
Create a new project.

```http
POST /projects
```

### Parameters:
| Name | Type | In | Required | Description |
|:-:|:-:|:-:|:-:|:-:|
| `accept` | string | header | no | Setting to `application/vnd.daw+json` is recommended. |
| `content-type` | string | header | yes | Set to `application/json`. |
| `name` | string | body |  yes | **Unique** project name. |
| `description` | string | body |  yes | Project description. |
| `states` | array of strings | body |  yes | Possible states for all of the project's issues. |
| `initialState` | string | body |  yes | Initial state for all of the project's issues. |
| `statesTransitions` | array of strings | body |  yes | **Each pair of entries** in the array should be a state transition. |
| `labels` | array of strings | body |  yes | Possible labels for all of the project's issues. |

### Request body example
```json
{
    "name": "DAW Project",
    "description": "DAW Project description",
    "labels": ["exploration", "new-functionality"],
    "states": ["todo", "wip", "closed", "archived"],
    "initialState": "todo",
    "statesTransitions": ["todo", "wip", "wip", "closed", "closed", "archived"]    
}
```

### Response
```http
Status: 201 Created
Location: /projects/{projectId}
```
```json
{
    "class": [ "project" ],
    "properties": {
        "id": 1,
        "name": "DAW Project",
        "description": "DAW Project description",
        "labels": [
            { "id": 1, "name": "exploration" }, 
            { "id": 2, "name": "new-functionality" }
        ],
        "states": [
            { "id": 1, "name": "todo" },
            { "id": 2, "name": "wip" },
            { "id": 3, "name": "closed" },
            { "id": 4, "name": "archived" }
        ],
        "initialState": { "id": 1, "name": "todo" },
        "statesTransitions": [
            { "id": 1, "name": "todo" },
            { "id": 2, "name": "wip" },
            { "id": 2, "name": "wip" },
            { "id": 3, "name": "closed" },
            { "id": 3, "name": "closed" },
            { "id": 4, "name": "archived" }
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
        { "rel": [ "self" ], "href": "/projects/1" }
    ]
}
```

```http
Status: 400 Bad Request
```
```http
Status: 401 Unauthorized
```

## Get a project
Get a certain project.

```http
GET /projects/{projectId}
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
    "class": [ "project" ],
    "properties": {
        "id": 1,
        "name": "DAW Project",
        "description": "DAW Project description"
    },
    "entities": [
        {
            "class": [ "issue", "collection" ],
            "rel": [ "project-issues" ],
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
                        "name": "Issue1",
                        "labels": ["new-functionality"],
                        "state": "closed"
                    },
                    "links": [
                        { "rel": [ "self" ], "href": "/projects/1/issues/1" }
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
            "name": "delete-project",
            "title": "Delete project",
            "method": "DELETE",
            "href": "/projects/1"
        },
        {
            "name": "edit-project",
            "title": "Edit project",
            "method": "PUT",
            "href": "/projects/1",
            "type": "application/json",
            "properties": [
                { "name": "name", "type": "string" },
                { "name": "description", "type": "string" }
            ]
        }
    ],
    "links": [
        { "rel": [ "self" ], "href": "/projects/1" },
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

## Update a project
Update the characterization of a certain project.

```http
PUT /projects/{projectId}
```

### Parameters
| Name | Type | In | Required | Description |
|:-:|:-:|:-:|:-:|:-:|
| `projectId` | integer | path | yes | Identifier of the project. |
| `accept` | string | header | no | Setting to `application/vnd.daw+json` is recommended. |
| `content-type` | string | header | yes | Set to `application/json`. |
| `name` | string | body | no | New name for the project. |
| `description` | string | body | no | New description for the project. |

 **Notice:** At least one of the body parameters **should** be inserted.

### Response
```http
Status: 200 OK 
```

```json
{
    "class": [ "project" ],
    "properties": {
        "id": 1,
        "name": "DAW Project updated",
        "description": "DAW Project description updated"
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
        { "rel": [ "self" ], "href": "/projects/1" }
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

## Delete a project
Delete a certain project.

```http
DELETE /projects/{projectId}
```

### Parameters
| Name | Type | In | Required | Description |
|:-:|:-:|:-:|:-:|:-:
| `projectId` | integer | path | yes | Identifier of the project. |
| `accept` | string | header | no | Setting to `application/vnd.daw+json` is recommended. |

### Response
```http
Status: 200 OK 
```

```json
{
    "class": [ "project" ],
    "properties": {
        "id": 1,
        "name": "DAW Project",
        "description": "DAW Project description"
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
        { "rel": [ "self" ], "href": "/projects/1" },
        { "rel": [ "projects" ], "href": "/projects" }
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

## Add label to a project
Add a new label to a certain project.

```http
PUT /projects/{projectId}/labels
```

### Parameters
| Name | Type | In | Required | Description |
|:-:|:-:|:-:|:-:|:-:|
| `projectId` | integer | path | yes | Identifier of the project. |
| `accept` | string | header | no | Setting to `application/vnd.daw+json` is recommended. |
| `content-type` | string | header | yes | Set to `application/json`. |
| `labels` | array of strings | body | yes | New labels to add to the project. |

### Response
```http
Status: 200 OK
```

```json
{
    "class": [ "project" ],
    "properties": {
        "id": 1,
        "name": "DAW Project",
        "description": "DAW Project description",
        "labels": [
            { "id": 1, "name": "exploration" }, 
            { "id": 2, "name": "new-functionality" },
            { "id": 3, "name": "new-label" }
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
        { "rel": [ "self" ], "href": "/projects/1" }
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

## Project representations vocabulary
| Name | Type | Description |
|:-:|:-:|:-:|
| `id` | integer | **Unique** and **stable** identifier of the project. |
| `name` | string | **Unique** name of the project. |
| `description` | string | Description of the project. |
| `labels` | array of objects | Possible labels for all the project issues. The objects are composed by two properties: the `id` and the `name` of the label.  |
| `states` | array of objects | Possible states for all the project issues. The objects are composed by two properties: the `id` and the `name` of the state. |
| `initialState` | integer | Initial state for all the project issues. The integer correspond to the `state id`. |
| `statesTransitions` | array of integers | **Each pair of entries** in the array is a state transition, in which the integers correspond to the `states ids`. |

### Domain specific link relations
| Name | Description |
|:-:|:-:|
| `project-issues` | Set of issues that belong to a project. |
| `projects` | Representation of all the projects owned by the authenticated user. |

The **documentation** for the `media-type`, `classes`, `standard link relations` and `generic errors` used in the representations are described [**here**](../README.md).