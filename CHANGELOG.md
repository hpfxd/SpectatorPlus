# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 1.3.0 - 2026-02-07

### Added
- Added support for Minecraft 1.21.11.
- GitHub Actions workflows for automated building and releasing
  - CI build workflow for continuous integration
  - Release workflow for automatic GitHub releases
- Potion effect synchronization, allowing spectators to view the target player's potion effects.
- Container synchronization in spectator mode, displaying the container currently opened by the target player (e.g., chests, furnace, etc.).
- Cursor synchronization in spectator mode, displaying the hotbar slot and item currently selected by the target player (requires server-side installation and the target player to have the mod installed).
- Player inventory synchronization in spectator mode, displaying the target player's full inventory (requires server-side installation).
- Inventory screen state synchronization in spectator mode, reflecting when the target player opens or closes their inventory GUI (requires server-side installation and the target player to have the mod installed).

### Fixed
- Fixed an issue with view rotate calculations in spectator mode that caused incorrect arm positioning.
- Fixed an issue where spectators would not correctly receive hotbar item updates from the target player.

### Changed
- Upgraded the project structure to a multi-module architecture to support building for multiple Minecraft versions.

### Chores
- Merged upstream changes regarding the update from 1.21.10 to 1.21.11.