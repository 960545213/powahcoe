# 0.0.6

- Preserve the client auto-eject flag when receiving energy-only updates.
- Synchronize the checkbox from the server when opening the GUI and while it is open.
- Include full block-entity state in initial chunk synchronization.
- Ignore non-left-clicks on auto-eject and avoid redundant state changes.
- Existing world NBT remains compatible; no inventory layout or recipe changes.

## In-game regression checklist

1. Enable auto-eject, charge the orb through at least 20 successful energy transfers, close and reopen its GUI: still checked.
2. Disable auto-eject, close/reopen and continue charging: still unchecked.
3. Save/reload the world and unload/reload the chunk: both ON and OFF persist.
4. Open the same orb with two clients: both checkboxes reflect server changes.
5. With auto-eject ON, place a receiving inventory beside the orb and verify output transfers; OFF must stop transfers.

Build/static verification does not substitute for these in-game checks.
