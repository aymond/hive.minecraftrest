# Hive Minecraft Plugin

A Minecraft server plugin template.

## Requirements

- Java 17 or higher
- Spigot/Paper server 1.20.4

## Building

1. Clone the repository
2. Build the project using Maven:
```bash
mvn clean package
```
3. The compiled JAR will be in the `target` folder

## Installation

1. Stop your Minecraft server
2. Copy the JAR file from `target/hive-1.0-SNAPSHOT.jar` to your server's `plugins` folder
3. Start your server

## Development

This project uses Maven for dependency management and building. The main plugin class is located at `src/main/java/com/minerest/hive/Hive.java`.

### Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── minerest/
│   │           └── hive/
│   │               └── Hive.java
│   └── resources/
│       └── plugin.yml
```

## License

This project is licensed under the MIT License - see the LICENSE file for details. 