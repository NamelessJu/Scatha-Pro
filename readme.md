# Features

---

## UI overlay in the Crystal Hollows

- pet drop counter
- worm/scatha kills counter
   - kills in current lobby
   - total kills *(requires API key)*
- worm/scatha spawn streak
- time display
   - ingame time
   - time spent in lobby
- coordinates + view axis

## Alerts

- Worm pre-spawn alert
- Worm spawn alert  
   distinguishes between regular worms and scathas
- Scatha pet drop alert    
   shows rarity of dropped pet
- Bedrock wall alert
- Modes  
   modes play different alert sounds *(+ scatha head in overlay changes to show current mode)*
   - Normal *(vanilla Minecraft sounds)*
   - Meme mode
   - Anime mode

## Achievements

Accessible through a button next to the vanilla Minecraft achievements menu button  
  
### Categories:
- Normal
- Secret  
   Description obfuscated if not unlocked
- HIDDEN  
   Completely hidden from the list and achievement counters unless unlocked

## Scatha pet chances command

`/scathachances [magic find] [pet luck] [scatha kills]`  
If no arguments are entered, shows the base chances, otherwise calculates the specific drop chances with looting read from the held weapon.  
When a kills value is entered, calculates the chances for dropping at least 1 pet after x kills

## Settings GUI

Accessible either via a button in the "Options" menu, or using `/scathapro` *(shortcut: `/sp`)*  
  
### All settings:
- API key
- alert volume
- Overlay:
   - enable/disable
   - scale
   - position
- worm pre-spawn alert
- worm alert
- scatha alert
- bedrock wall alert
- scatha pet drop alert
- mode
- chat copy button

## Display rotation angles

Shows the pitch and yaw next to the crosshair

## Chat copy button

*(disabled by default)*  
Adds a clickable icon behind all chat messages that copies their contents into the chat input field