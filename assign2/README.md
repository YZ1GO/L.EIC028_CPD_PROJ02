# NoiaTalk

## Configuration
Before running the chat, ensure all necessary settings are configured in [config.json](config.json):

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

|Username|Password|
|-|-|
|alice|123|
|bob|123|

To add more accounts, modify the [user list](config/users.cfg).

## Commands

### General Commands
| Command | Description |
|---------|-------------|
| `/quit`| Disconnect from the server. |
| `/room list`| Show available chat rooms.|

### Room Commands
| Command | Description |
|---------|-------------|
| `/join <roomname>`| Join an existing room. |
| `/create <roomname> [1]` | Create a new room (use 1 to create an AI-powered room). |


### AI-Powered Rooms
If you're in an AI-powered room, interact with the AI bot using:
```
/ai <message>
```
Example:
```
/ai How are you?
```






