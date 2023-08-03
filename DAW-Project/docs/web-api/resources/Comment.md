# Comment
A **Comment** is part of sequence of issue comments, ordered in chronological order. The **Comment API** allows you to create, view and manage all the comments made in an issue of a project owned by the authenticated user. All the **vocabulary** used in the comment representations is described [**here**](#comment-representations-vocabulary).

## Comment API contents
* [**List comments**](#list-comments)
* [**Create a comment**](#create-a-comment)
* [**Get a comment**](#get-a-comment)
* [**Update a comment**](#update-a-comment)
* [**Delete a comment**](#delete-a-comment)

## List comments
List comments of a determined issue of a project owned by the authenticated user.

```http
GET /projects/{projectId}/issues/{issueId}/comments
```

### Parameters
| Name | Type | In | Required | Description |
|:-:|:-:|:-:|:-:|:-:|
| `projectId` | integer | path | yes | Identifier of the project. |
| `issueId` | integer | path | yes | Identifier of the project issue. |
| `accept` | string | header | no | Setting to `application/vnd.daw+json` is recommended. |
| `page` | integer | query | no| Page number of the results to fetch. **Default:** `0` |

### Response
```http
Status: 200 OK 
```

```json
{
    "class": [ "comment", "collection" ],
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
                "comment": "Issue comment",
                "timestamp": "2022-04-08 21:52:47.012620"
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

## Create a comment
Create a new comment in an determined issue.

```http
POST /projects/{projectId}/issues/{issueId}/comments
```

### Parameters:
| Name | Type | In | Required | Description |
|:-:|:-:|:-:|:-:|:-:|
| `projectId` | integer | path | yes | Identifier of the project. |
| `issueId` | integer | path | yes | Identifier of the project issue. |
| `accept` | string | header | no | Setting to `application/vnd.daw+json` is recommended. |
| `content-type` | string | header | yes | Set to `application/json`. |
| `comment` | string | body |  yes | Written remark expressing an opinion or reaction about the issue. |

### Response
```http
Status: 201 Created
Location: /projects/{projectId}/issues/{issueId}/comments
```
```json
{
    "class": [ "comment" ],
    "properties": {
        "id": 1,
        "comment": "Issue comment",
        "timestamp": "2022-04-08 21:52:47.012620"
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
        { "rel": [ "self" ], "href": "/projects/1/issues/1/comments/1" },
        { "rel": [ "comments" ], "href": "/projects/1/issues/1/comments" }
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
```http
Status: 409 Conflict
```

## Get a comment
Get a certain comment of an issue.

```http
GET /projects/{projectId}/issues/{issueId}/comments/{commentId}
```

### Parameters
| Name | Type | In | Required | Description |
|:-:|:-:|:-:|:-:|:-:|
| `projectId` | integer | path | yes | Identifier of the project. |
| `issueId` | integer | path | yes | Identifier of the project issue. |
| `commentId` | integer | path | yes | Identifier of the issue comment. |
| `accept` | string | header | no | Setting to `application/vnd.daw+json` is recommended. |

### Response
```http
Status: 200 OK 
```

```json
{
    "class": [ "comment" ],
    "properties": {
        "id": 1,
        "comment": "Issue comment",
        "timestamp": "2022-04-08 21:52:47.012620"
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

## Update a comment
Update a certain comment of an issue.

```http
PUT /projects/{projectId}/issues/{issueId}/comments/{commentId}
```

### Parameters
| Name | Type | In | Required | Description |
|:-:|:-:|:-:|:-:|:-:|
| `projectId` | integer | path | yes | Identifier of the project. |
| `issueId` | integer | path | yes | Identifier of the project issue. |
| `commentId` | integer | path | yes | Identifier of the issue comment. |
| `accept` | string | header | no | Setting to `application/vnd.daw+json` is recommended. |
| `comment` | string | body |  yes | Edited comment. |

### Response
```http
Status: 200 OK 
```

```json
{
    "class": [ "comment" ],
    "properties": {
        "id": 1,
        "comment": "Comment edited",
        "timestamp": "2022-04-08 21:52:47.012620"
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
        { "rel": [ "self" ], "href": "/projects/1/issues/1/comments/1" }
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
```http
Status: 409 Conflict
```

## Delete a comment
Delete a certain comment of an issue.

```http
DELETE /projects/{projectId}/issues/{issueId}/comments/{commentId}
```

### Parameters
| Name | Type | In | Required | Description |
|:-:|:-:|:-:|:-:|:-:|
| `projectId` | integer | path | yes | Identifier of the project. |
| `issueId` | integer | path | yes | Identifier of the project issue. |
| `commentId` | integer | path | yes | Identifier of the issue comment. |
| `accept` | string | header | no | Setting to `application/vnd.daw+json` is recommended. |

### Response
```http
Status: 200 OK 
```

```json
{
    "class": [ "comment" ],
    "properties": {
        "id": 1,
        "comment": "Comment edited",
        "timestamp": "2022-04-08 21:52:47.012620"
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
        { "rel": [ "self" ], "href": "/projects/1/issues/1/comments/1" }
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
```http
Status: 409 Conflict
```

## Comment representations vocabulary
| Name | Type | Description |
|:-:|:-:|:-:|
| `id` | integer | Identifier of the comment. **Stable** and **unique for each issue of a project, but not globally**. |
| `comment` | string | Written remark expressing an opinion or reaction about the issue. |
| `timestamp` | timestamp | Date and time that the comment was created. |

### Domain specific link relations
| Name | Description |
|:-:|:-:|
| `comments` | Representation of all the comments in a specific issue. |

### Domain specific errors
* `archived-issue`: Happens when it's requested to change an archived issue, namely create, edit or delete. It is thrown with the HTTP status code **409 Conflict**.
```json
{
    "type": "/probs/archived-issue",
    "title": "You can't change an archived issue.",
    "detail": "You can't delete an issue in which the current state is archived.",
    "instance": "/projects/1/issues/1/comments/1"
}
```

The **documentation** for the `media-type`, `classes`, `standard link relations` and `generic errors` used in the representations are described [**here**](../README.md).