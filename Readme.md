# Stickfight

![Static Badge](https://img.shields.io/badge/API%20version-1.21-orange)
![Static Badge](https://img.shields.io/badge/Java%20version-21-red)

Stickfight is a mini-game for Minecraft where each player gets a stick with knockback 5 enchantment.
Glass panes shatter when hit by a player, but are replaced after some time.

## Configuration

A `config.yml` file can be used to configure Stickfight.
If no such file exists on startup, Stickfight creates one which looks like this:

```
kill-counter: true
kill-layer: -20
play-area:
  max:
    x: 10
    y: 10
    z: 10
  min:
    x: -10
    y: -10
    z: -10
  unlimited: true
```

| **Key**                 | Type    | Default Value | Description                                                                 |
|-------------------------|---------|---------------|-----------------------------------------------------------------------------|
| **kill-counter**        | boolean | true          | Whether a kill counter should be created.                                   |
| **kill-layer**          | integer | -20           | The maximum y coordinate where players are killed.                          |
| **play-area.max.x**     | integer | 10            | The maximum x coordinate of the play area if the play area is not unlimited |
| **play-area.max.y**     | integer | 10            | The maximum y coordinate of the play area if the play area is not unlimited |
| **play-area.max.z**     | integer | 10            | The maximum z coordinate of the play area if the play area is not unlimited |
| **play-area.min.x**     | integer | -10           | The minimum x coordinate of the play area if the play area is not unlimited |
| **play-area.min.y**     | integer | -10           | The minimum y coordinate of the play area if the play area is not unlimited |
| **play-area.min.z**     | integer | -10           | The minimum z coordinate of the play area if the play area is not unlimited |
| **play-area.unlimited** | boolean | true          | Whether the play area should be unlimited in size.                          |
