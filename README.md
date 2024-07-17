# Ojima
an ojimization backend for tentative WebOjima

## RESTful API

## Endpoints

### GET /ojimizers

Retrieves the list of Ojimizer models in JSON format.

#### Response
- `200 OK`:
  ```json
  [
    {
      "name": "Ojimizer Model 1",
      "description": "Description of Ojimizer Model 1"
    },
    {
      "name": "Ojimizer Model 2",
      "description": "Description of Ojimizer Model 2"
    }
    ...
  ]
  ```

### POST /reload

Reloads the configuration if the correct admin password is provided.

#### Request
- Headers:
    - `Content-Type: application/json`
- Body:
  ```json
  {
    "password": "your_admin_password"
  }
  ```

#### Response
- `200 OK`:
  ```json
  {
    "status": "success"
  }
  ```
- `401 Unauthorized`:
  ```json
  {
    "status": "failed"
  }
  ```

### POST /ojimize

Processes a video using the specified Ojimizer settings.

#### Request
- Headers:
    - `Content-Type: multipart/form-data`
- Body:
  | Form Item    | Description                   |
  |--------------|-------------------------------|
  | requestId    | Unique ID for the request     |
  | score        | Score data for Ojimization    |
  | mode         | Ojimizer mode to use          |
  | bpm          | Beats per minute for the score|
  | fps          | Frames per second for the video|
  | options      | Additional options (JSON)     |
  | video        | Video file to process         |

#### Response
- `200 OK`: Processed video file and `ojima-status` header with status information
- `400 Bad Request`: `ojima-status` header with error information and no body

#### Example `ojima-status` Header
- Successful response:
  ```json
  {
    "id": "requestId",
    "status": "success",
    "message": "Ojimization completed successfully",
    "success": true
  }
  ```
- Invalid payload or error:
  ```json
  {
    "id": "requestId",
    "status": "failed",
    "message": "Error message",
    "success": false
  }
  ```




