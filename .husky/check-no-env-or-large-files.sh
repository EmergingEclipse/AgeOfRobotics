staged=$(git diff --cached --name-only --diff-filter=ACM)

env_files=$(printf '%s\n' "$staged" | grep -E '(^|/)\.env(\..+)?$' | grep -v '\.env\.example$')
if [ -n "$env_files" ]; then
  echo "pre-commit: refusing to commit .env file(s):" >&2
  printf '%s\n' "$env_files" >&2
  echo "Add it to .gitignore instead." >&2
  exit 1
fi

max_bytes=5242880
for f in $staged; do
  [ -f "$f" ] || continue
  size=$(wc -c < "$f")
  if [ "$size" -gt "$max_bytes" ]; then
    echo "pre-commit: $f is $(( size / 1024 / 1024 ))MB, over the 5MB limit for this repo." >&2
    echo "Large binaries don't belong in git history." >&2
    exit 1
  fi
done
