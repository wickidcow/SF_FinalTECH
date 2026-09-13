from pathlib import Path
import re
import yaml

path = Path('src/main/resources/language/en-US.yml')
data = yaml.safe_load(path.read_text(encoding='utf-8'))
items = data.get('items', {})
placeholder = re.compile(r'\{\d+\}')
usable = []
missing = []
explicit = []
for item_id, node in items.items():
    if not isinstance(node, dict) or 'name' not in node:
        continue
    lore = node.get('lore') or []
    if lore:
        explicit.append(item_id)
        continue
    picked = []
    info = node.get('info') or {}
    if isinstance(info, dict):
        def sort_key(k):
            try: return int(k)
            except Exception: return 9999
        for key in sorted(info, key=sort_key):
            section = info[key]
            if not isinstance(section, dict):
                continue
            title = str(section.get('name', '')).lower()
            if not any(word in title for word in ('usage', 'mechanism', 'function')):
                continue
            for line in section.get('lore') or []:
                if line and not placeholder.search(str(line)):
                    picked.append(str(line))
                if len(picked) >= 2:
                    break
            if picked:
                break
    if picked:
        usable.append((item_id, picked))
    else:
        missing.append(item_id)

print(f'Item entries with names: {len(explicit)+len(usable)+len(missing)}')
print(f'Already explicit lore: {len(explicit)}')
print(f'Can derive useful purpose from Usage/Mechanism: {len(usable)}')
print(f'Need explicit fallback purpose: {len(missing)}')
print('--- MISSING PURPOSE ---')
for item_id in missing:
    print(item_id)
