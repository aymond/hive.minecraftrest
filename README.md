# MinecraftREST

A powerful REST API interface for Minecraft servers that provides secure endpoints for server management and player interactions.

## Features

- 🔒 Secure JWT Authentication
- 🚀 RESTful API endpoints for server management
- 👥 Player management (kick, gamemode, messaging)
- 📊 Server information and statistics
- ⚡ Rate limiting protection
- 🔍 Real-time player and world data

## Requirements

- Java 17 or higher
- Spigot/Paper server 1.20.4

## Installation

1. Download the latest release from [GitHub Releases](https://github.com/aymond/hive.minecraftrest/releases)
2. Place the JAR file in your server's `plugins` folder
3. Start/restart your server
4. Configure the plugin in `plugins/MinecraftREST/config.yml`

## Configuration

The plugin creates a `config.yml` file in `plugins/MinecraftREST/` with the following options:

```yaml
# API Configuration
api-port: 4567  # The port on which the HTTP API will run

# Security Configuration
security:
  jwt-secret: "your-secret-key-here"  # Change this to a secure random string
  max-requests-per-minute: 60  # Rate limiting threshold
  admin-credentials:
    username: "admin"  # Change this to a secure username
    password: "change-this-password"  # Change this to a secure password
```

⚠️ **Important**: Change the default security settings before using in production!

## API Endpoints

### Authentication

- `POST /api/auth/login`
  - Request body: `{"username": "admin", "password": "your-password"}`
  - Response: `{"token": "jwt-token"}`

### Server Management

- `GET /api/server/info` - Get server information
- `POST /api/server/command` - Execute console command
- `POST /api/broadcast` - Broadcast message to all players

### Player Management

- `GET /api/players` - List online players
- `POST /api/player/message` - Send message to player
- `POST /api/player/kick` - Kick player
- `POST /api/player/gamemode` - Change player gamemode

All endpoints except `/api/auth/login` require JWT authentication via Bearer token.

## API Usage Example

```bash
# Login and get token
curl -X POST http://localhost:4567/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"your-password"}'

# Use token to get server info
curl http://localhost:4567/api/server/info \
  -H "Authorization: Bearer your-jwt-token"
```

## Building from Source

1. Clone the repository:
```bash
git clone https://github.com/aymond/hive.minecraftrest.git
```

2. Build with Maven:
```bash
mvn clean package
```

3. Find the JAR in `target/minecraft-rest-1.0.0.jar`

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## Security

- Change default JWT secret and admin credentials
- Use HTTPS in production
- Keep your authentication token secure
- Monitor rate limiting logs

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

- GitHub Issues: [Report a bug](https://github.com/aymond/hive.minecraftrest/issues)
- GitHub Discussions: [Ask a question](https://github.com/aymond/hive.minecraftrest/discussions) 