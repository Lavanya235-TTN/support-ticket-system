#!/usr/bin/env bash
# Run integration tests (Testcontainers). Uses `sg docker` when the current shell
# has not refreshed group membership after `usermod -aG docker`.
set -euo pipefail
cd "$(dirname "$0")"

run_verify() {
  mvn verify "$@"
}

if docker info >/dev/null 2>&1; then
  run_verify "$@"
elif getent group docker | grep -qE "[,:]${USER}([,]|$)"; then
  echo "Docker socket not usable in this shell; running with docker group via sg …" >&2
  exec sg docker -c "cd $(pwd) && mvn verify $*"
else
  echo "Add your user to the docker group, then log out and back in (or restart Cursor):" >&2
  echo "  sudo usermod -aG docker \"\$USER\"" >&2
  exit 1
fi
