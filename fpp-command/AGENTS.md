# AGENTS.md - fpp-command

Owns bot command execution behavior.

Keep here:
- `/fpp cmd`
- command execution permissions
- command execution config defaults

Core must remain free of extension command behavior. Use `FppApi.runAsBot(...)` instead of reaching into core internals.
