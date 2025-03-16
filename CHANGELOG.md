# Changelog

All notable changes to MinecraftREST will be documented in this file.

## [1.0.1] - 2024-03-20

### Fixed
- Fixed an error that occurs because trying to kick a player from an asynchronous thread (the HTTP request thread) while Bukkit/Spigot requires player operations to be performed on the main server thread.
- Fixed similar thread safety issues with the gamemode change endpoint.
- Both operations now properly run on the main server thread using BukkitRunnable.

### Technical Details
- Modified `/api/player/kick` endpoint to use Bukkit's scheduler
- Modified `/api/player/gamemode` endpoint to use Bukkit's scheduler
- Improved error handling for async operations
- Follows the same pattern as the command execution endpoint for thread safety

### Developer Notes
- If you're using these endpoints in your code, no changes are required on your end
- The API interface and responses remain the same
- Operations may take a few milliseconds longer due to thread scheduling

## [1.0.0] - 2024-03-20

### Initial Release
- Complete RESTful API implementation
- Secure JWT authentication system
- Rate limiting protection
- Player management endpoints
- Server control endpoints
- Comprehensive API documentation 