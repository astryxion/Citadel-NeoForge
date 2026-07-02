# Citadel — Minecraft 26.1 Port (100% AI-Generated)

> ## ⚠️ Full disclosure: every line of this port was written by AI.
> This port of the Citadel library to **Minecraft 26.1.2 / NeoForge 26.1.2.22-beta** was produced
> entirely by **Claude (Anthropic's AI assistant, model: Claude Fable 5)** working autonomously
> inside Claude Code. A human (JakeMalmrose) supervised and tested — but did not write any of
> the code. Judge, review, and use it with that in mind.

## What this is

[Citadel](https://github.com/AlexModGuy/Citadel) is AlexModGuy's animation/model library that
Rats, Alex's Mobs, Ice and Fire, and other mods depend on. This branch builds on
[astryxion23's Citadel-NeoForge](https://github.com/astryxion23/Citadel-NeoForge) work and brings
the library to Minecraft 26.1.2 as a standalone-buildable project, including a `publishing`
block so dependents can consume it from mavenLocal as
`com.github.alexthe666:citadelneoforge:26.1-1.0.1`.

It exists primarily to power the companion
[Rats 26.1 port](https://github.com/JakeMalmrose/Rats) (also 100% AI-generated), whose render
architecture bridges Citadel's entity-typed `AdvancedEntityModel` animation system into 26.1's
render-state/submit pipeline.

## Status

Verified working: builds, boots a dedicated server to "Done" and a client to gameplay under the
Rats 26.1 port, which exercises Citadel's model/animation system across ~60 entity renderers.

## Credits

- **Original library**: [AlexModGuy (Alexthe666)](https://github.com/AlexModGuy/Citadel).
- **NeoForge/26.1 groundwork**: [astryxion23](https://github.com/astryxion23/Citadel-NeoForge).
- **26.1 port**: Claude (Anthropic) — 100% AI-generated, supervised by JakeMalmrose.

License follows the original repository's license. Unofficial; will be taken down on request
from the original author.
