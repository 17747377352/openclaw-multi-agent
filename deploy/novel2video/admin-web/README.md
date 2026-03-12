# Novel2Video Admin Web

Vue 3 admin frontend for the `deploy/novel2video` backend.

## Stack

- Vue 3
- Vue Router
- TypeScript
- Vite

## Development

```bash
npm install
npm run dev
```

Default dev server: `http://localhost:5173`

The Vite dev server proxies `/api` to `http://localhost:8081`.

## Routing

The app uses hash history (`/#/projects`) so it can be served by Spring Boot static resources without extra rewrite rules.

## Build

Deploy at site root:

```bash
npm run build
```

Deploy under a subpath like `/novel-admin/`:

```bash
VITE_PUBLIC_BASE=/novel-admin/ npm run build
```

## Deploy

Build output is generated in `dist/`.

Typical deployment:

```bash
rsync -av --delete dist/ /var/www/novel2video/admin-web/
```

Then use Nginx to serve `/novel-admin/` and proxy `/api/` to the Spring Boot backend.

## Structure

- `src/api`: backend API wrappers
- `src/components`: shared layout and UI components
- `src/stores`: lightweight reactive shared state
- `src/types`: domain and API types
- `src/views`: route-level pages
