
<div align="center">
    <img src="assets/noiatalk-logo.png" width="500">
</div>


## Setup
Before running the chat, ensure all necessary settings are configured in [config.json](config.json). 

Example:

```
{
  "SOCKET_HOST" : "localhost",
  "SOCKET_PORT" : "8008",
  "LLM_URL" : "http://127.0.0.1:1234",
  "MODEL" : "deepseek-r1"
}
```

## Local LLM
To enable AI-powered rooms, it is recommended to run a local LLM server using [LM Studio](https://lmstudio.ai/) to power all the AI features.

## Getting Started
### Server
To start the chat server, run:
```bash
./gradlew server
```

### Client
To connect as a client, run:
```bash
./gradlew client
```

### Authentication
By default, the following accounts are available for login:

| Username | Password |
|----------|----------|
| alice    | 123      |
| bob      | 123      |
| joao     | 123      |
| maria    | 123      |

To add more default accounts, modify the [user list](config/users.cfg).

## Commands

In case of any trouble, use `/help` :)

### Authentication Commands
| Command                           | Description   |
|-----------------------------------|---------------|
| `/login <username> <password>`    | Login user    |
| `/register <username> <password>` | Register user |

### In-Lobby Commands
| Command                  | Description                                             |
|--------------------------|---------------------------------------------------------|
| `/join <roomname>`       | Join an existing room.                                  |
| `/create <roomname> [1]` | Create a new room (use 1 to create an AI-powered room). |
| `/room list`             | List the existing rooms.                                |
| `/logout`                | Disconnect from the server.                             |

### In-Room Commands
| Command                  | Description                                                      |
|--------------------------|------------------------------------------------------------------|
| `/join <roomname>`       | Switch to an existing room.                                      |
| `/create <roomname> [1]` | Create and join a new room (use 1 to create an AI-powered room). |
| `/info`                  | Show current room information                                    |
| `/room list`             | List the existing rooms.                                         |
| `/leave`                 | Back to lobby                                                    |
| `/logout`                | Disconnect from the server.                                      |



### AI-Powered Rooms
If you're in an AI-powered room, interact with the AI bot using:
```
/ai <message>
```
Example:
```
/ai Resume the chat
```


## LAN Setup
Chat Between Machines on the Same Network.

### Server Configuration
Open your [config.json](config.json).  and change the host to allow external connections:

```
{
  "SOCKET_HOST" : "0.0.0.0",
  "SOCKET_PORT" : "8008",
  "LLM_URL" : "http://127.0.0.1:1234",
  "MODEL" : "deepseek-r1"
}
```

- `SOCKET_HOST: "0.0.0.0"` binds the server to all network interfaces, allowing LAN access.

Run these commands on the server's host machine to get its local IP address:
- Linux:
    ```bash
    hostname -I
    ```

- MacOS
    ```bash
    ipconfig getifaddr en0    # for WIFI
    ipconfig getifaddr en1    # for Ethernet
    ```

- Windows
    ```shell
    ipconfig
    ```

### Client Configuration
On any other device on the same Wi-Fi or LAN, update the config.json to use the server’s IP address:

```
{
  "SOCKET_HOST" : "Replace with the server's IP",
  "SOCKET_PORT" : "8008",
  "LLM_URL" : "http://127.0.0.1:1234",
  "MODEL" : "deepseek-r1"
}
```

