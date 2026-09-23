# SCRAPING SPEC — Kids Memory Match

**Status: NOT REQUIRED for v1.**

All game content (card images, sounds, music) is bundled in the app via the asset
pipeline described in DATA_SOURCE.md. There is no web scraping in this project.

If a future version ever needs web-sourced content, these rules apply:
1. Only sources with clear licenses (public domain / CC0 / purchased stock).
2. Record source URL + license in `assets/ATTRIBUTION.md`.
3. Never hotlink — download, verify, bundle.
4. Kid-safe filter: every image reviewed by a human before bundling.

Do not build any scraping infrastructure for v1.
