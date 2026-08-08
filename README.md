# Gidget Client

An open-source, client-side Fabric utility and developer inspection framework for Minecraft 26.1.2 (Java 25, Fabric Loader/API, non-remap `fabric-loom`).

Built for use on servers you own or administer — verifying rendering behavior, chunk culling, and lighting logic, and as a reference implementation of a module/settings/GUI framework for Fabric mod development.

This is original code. It targets the real vanilla `net.minecraft.gizmos.Gizmos` debug-draw API, `Block#shouldRenderFace` / `FluidRenderer#shouldRenderFace`, and `LightmapRenderStateExtractor#extract`, rather than reusing another mod's rendering pipeline.

## Modules

- **Block ESP** — highlights whitelisted blocks through walls via `Gizmos.cuboid()`.
- **Xray** — selective face culling for a block whitelist (defaults to ores), with a see-through-water toggle.
- **Entity ESP** — bounding boxes around selected entity types (players/mobs/animals).
- **Fullbright** — forces the lightmap's ambient color to white.

## Usage

### Building
- Clone this repository
- Run `./gradlew build`

### Running a dev client
- Run `./gradlew runClient`

## License

GPL-3.0 (see [LICENSE](LICENSE)).

## Contributing
- The license header must be applied to all Java source code files.
- IDE or system-related files should be added to `.gitignore`, never committed.
- Match the style of existing code in the project; favour readability over compactness.
