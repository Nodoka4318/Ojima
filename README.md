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
    },
    ...
  ]
  ```
  
### GET /easings

Retrieves the list of default easing options in JSON format.

#### Response
- `200 OK`:
  ```json
  [
    {
      "name": "EaseInSine",
      "expression": "1 - Math.cos((x * Math.PI) / 2)"
    },
    {
      "name": "EaseOutSine",
      "expression": "Math.sin((x * Math.PI) / 2)"
    },
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
  | options      | (Optional) Additional options (JSON)|
  | video        | Video file to process         |
  
  - Options:
    ```json
    {
      "startFrame": 10,
      "endFrame": 60,
      "easing": "custom",
      "easingExpression": "x == 0 ? 0 : pow(2, 10 * x - 10)"
    }
    ```
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

## Credits

This project was made possible by the following libraries and tools:

- **Ktor**: An asynchronous framework for creating microservices and web applications in Kotlin.
    - [Ktor](https://ktor.io/) by JetBrains.

- **Logback**: A reliable and general-purpose logging framework for Java applications.
    - [Logback](http://logback.qos.ch/) by QOS.ch.

- **JavaCV**: A set of Java wrappers for OpenCV and other computer vision libraries.
    - [JavaCV](https://bytedeco.org/) by Samuel Audet and contributors.

- **Gson**: A Java library that can be used to convert Java Objects into their JSON representation.
    - [Gson](https://github.com/google/gson) by Google.

## Donate

If you find this project useful, consider donating to support development.

- **BTC**: `1J7JJKbrocSw4S74MzoBDQxAwZFerCfHdN`
- **ETH**: `0xa197d2aa2941e4c2258be0a07f551fb58d977c74`