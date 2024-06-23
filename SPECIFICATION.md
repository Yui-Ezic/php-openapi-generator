# Common

Features:
- Version compatibility control between client and server
- Rate limits
- Caching
- Conditional requests

DTO: 
- Distinguish between required and nullable field
- Enums
- Typing
- Hydration from different formats

# Client

Features:
- Host selection
- Host replacement for local development
- Configuration for [server variables](https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.1.0.md#server-variable-object)
- Operation classes
  - Operation Parameters
    - Path
    - Query
    - Headers
    - Cookie
- PSR http client 
- Error handling
  - Connection errors
  - Response timeout
  - DNS error
  - Server response does not match openapi schema
  - Client request does not match openapi schema
  - Client errors from server

# Sever

Features:
- Etag for resources