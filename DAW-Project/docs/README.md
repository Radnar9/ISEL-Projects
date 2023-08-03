# API Documentation
You can use the API to create, view and manage your projects, issues and all of its features.

The base URI for all Web API requests is `http://localhost:8000/v1`.

## Table of contents
* [**API endpoints and documentation**](#api-endpoints-and-documentation)
* [**Media-type**](#media-type)
* [**Classes**](#classes)
* [**Standard link relations**](#standard-link-relations)
* [**Generic Errors**](#generic-errors)
* [**Authentication**](#authentication)

## API endpoints and documentation
* [**Project**](web-api/resources/Project.md)
* [**Issue**](web-api/resources/Issue.md)
* [**Comment**](web-api/resources/Comment.md)

## Media-type
The media-type that we recommend to be used in the representations of each resource is `application/vnd.daw+json`. This media-type is based in the hypermedia specification for representing entities - Siren, in which we did some little changes.

All the properties used in the representations that are in the JSON Siren format, such as, `entities`, `class`, `links`, `actions`, etc, are described in their documentation [here](https://github.com/kevinswiber/siren).

Before, in the actions section, when you wanted to do an action that would require a payload, the field types of this payload had to be an [input type specified in HTML5](https://html.spec.whatwg.org/#the-input-element), such as, text, number, hidden, etc. Therefore we added to our media-type that when the content-type of the action is `application/json` it has to be followed by the property `properties` which represents an object with all the data that will be in the body request, which types can be `string`, `number`, `array` or `object`. Each property can have an extra element called `required` to inform if the property must be inserted or not, when it is not required its value is set to false, otherwise it is hidden.
```json
{
    "type": "application/json",
    "properties": [
        { "name": "names", "type": "array", "itemsType": "string" }
    ]
}
```
```json
{
    "type": "application/json",
    "properties": [
        { 
            "name": "names", 
            "type": "object", 
            "properties": [ { "name": "firstName", "type": "string" } ] 
        }
    ]
}
```

#### `itemsType`
When the parent property type is an **array**, *itemsType* indicates the type of each value in the array. 

#### `properties`
When the parent property type is an **object**, *properties* represents an array that contains all the properties names and values.

## Classes
The classes, as mentioned in the [Siren documentation](https://github.com/kevinswiber/siren), describe the nature of an entity's content based on the current representation.

| Name | Description |
|:-:|:-:|
| `collection` | The representation contains a collection, such as, a collection of projects, issues or comments. |
| `project` | The representation contains a project. |
| `issue` | The representation contains an issue. |
| `comment` | The representation contains a comment. |
| `user` | The representation contains a user. |

#### `collection`
When the representation contains a collection, it is always included three more properties:
* `collectionSize` - Integer used to represent the total collection size.
* `pageSize` - Integer used to represent the number of items in the page. 
* `pageIndex` - Integer used to represent the page number.

A collection can also have **four** characteristics link relations, namely, `first`, `next`, `prev` and `last`. This link relations are presented in the next section [Standard link relations](#standard-link-relations). But notice that when there are no next and/or previous pages these link relations are not shown.

## Standard link relations
A link relation is a descriptive attribute attached to a hyperlink in order to define the type of the link, or the relationship between the source and destination resources.

In our domain, we mainly use standard link relations, which are described in [IANA's Link Relation Types documentation](https://www.iana.org/assignments/link-relations/link-relations.xhtml), and the ones we use that are not standard are always described in the document where they're used. The standard link relation types used are: `author`, `first`, `item`, `last`, `next`, `prev`, `self` and `search`.

## Generic errors
Each HTTP request can throw a set of errors. The error information format used in the Web API is the one described in the [RFC7807 - Problem Details for HTTP APIs](https://datatracker.ietf.org/doc/html/rfc7807) specification. As such the **content-type** of the errors representations is `application/problem+json`.

Each class can have their domain specific errors but mostly have generic errors, this is, errors that have almost everything in commom with each other, the properties that can differ are the `detail` message, `instance` URI and the `data` or `invalid-params` that caused the error. Below are described all these errors and an example of its usage.

#### `data`
Represents an array of objects, in which each object describe the properties names and values that caused the error. The properties inside the object can vary depending on the class.
```json
{
    "type": "https://example.com/probs/out-of-credit",
    "title": "You do not have enough credit.",
    "detail": "Your current balance is 30, but that costs 50.",
    "instance": "/account/12345/msgs/abc",
    "data": [
        { "balance": 30, "accounts": ["/account/12345", "/account/67890"] }
    ]
}
```

#### `invalid-params`
Only used in the HTTP error Bad Request and it's composed by an array of objects, in which each object represents the error found, describing the `name` of the parameter, the local (`in`) where it was found and the `reason` for this error to occur.
```json
{
   "type": "https://example.net/validation-error",
   "title": "Your request parameters didn't validate.",
   "invalid-params": [ 
       { "name": "age", "reason": "must be a positive integer" },
       { "name": "color", "reason": "must be 'green', 'red' or 'blue'" }
    ]
   }
```

### Bad Request
Thrown in requests that have some error inside the request, such as, an error in the path, headers or body.

#### Type
* `validation-error`

```http
Status: 400 Bad Request
```
```json
{
    "type": "/probs/validation-error",
    "title": "Your request parameters are invalid.",
    "instance": "/projects/abc",
    "invalid-params": [
        { "name": "projectId", "in": "path", "reason": "Must be an integer." }
    ]
}
```

### Unauthorized
Thrown in requests that need an authorization token to be carried out.

#### Type
* `unauthorized`

```http
Status: 401 Unauthorized
```
```json
{
    "type": "/probs/unauthorized",
    "title": "You don't have authorization to make this request.",
    "instance": "/projects/1"
}
```

### Not Found
Thrown in requests that the resource required was not found.

#### Type
* `not-found`

```http
Status: 404 Not Found
```
```json
{
    "type": "/probs/not-found",
    "title": "It was not possible to find the resource.",
    "instance": "/projects/1"
}
```

### Internal Server Error
Thrown when happens an unexpected error in the database.

#### Type
* `internal-server-error`

```http
Status: 500 Internal Server Error
```
```json
{
    "type": "/probs/internal-server-error",
    "title": "An internal server error occurred.",
    "instance": "/projects"
}
```

#### Obervations
In the documentation, where is mencioned that the parameters or the properties of the representations receive an integer, it is **always a non-negative integer**. And whenever is not mencioned that a property **is or is not unique and/or stable** it's because **it is not**.

## Authentication

The project for now uses the [**Basic Authentication Scheme**](https://datatracker.ietf.org/doc/html/rfc7617) which transmits credentials as `user-id/password` pairs, encoded using *Base64*. This scheme is not considered to be a secure method of user authentication and in future we pretend to implement a better authentication method. 