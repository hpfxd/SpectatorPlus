# SpectatorPlus

SpectatorPlus is a Minecraft mod designed to enhance the spectator mode experience by providing additional information
about the player being spectated. With SpectatorPlus, you can gain insights into the spectated player's held item,
health, hotbar contents, food level, and experience points. Additionally, it fixes a few longstanding Minecraft bugs
with spectator mode.

![Screenshot showing two Minecraft instances, one spectating the other](img/demo.png?raw=true)

## Features

- Show the held item and health of the spectated player
- Show the hotbar, health, food, and experience of the spectated player *(Server-side required)*
- Allow continuing to spectate even when the player teleports far away or between worlds *(Server-side required)*
    - This is a fix for [MC-107113](https://bugs.mojang.com/browse/MC-107113)
    - Even works for clients without the mod installed!

In order for all of the mod's features to be enabled, the mod needs to be installed on the server side as well.
This is because Minecraft normally does not send some needed data to clients. Fabric and Paper are supported for the
server-side.

## Permissions

SpectatorPlus has permissions included that you can use to control which players receive extra information about the
player they are spectating. **By default, all players have access to all information and no setup is required.** If you
would like to change this, you can use a permissions plugin/mod such as [LuckPerms](https://luckperms.net/). On Fabric,
SpectatorPlus uses [fabric-permissions-api](https://github.com/lucko/fabric-permissions-api/) to check permissions.

| Permission                      | Description                                                                             |
|---------------------------------|-----------------------------------------------------------------------------------------|
| `spectatorplus.sync.experience` | Allows the player to receive the experience value of the spectated target               |
| `spectatorplus.sync.food`       | Allows the player to receive the food value of the spectated target                     |
| `spectatorplus.sync.hotbar`     | Allows the player to receive the hotbar items and selected slot of the spectated target |
