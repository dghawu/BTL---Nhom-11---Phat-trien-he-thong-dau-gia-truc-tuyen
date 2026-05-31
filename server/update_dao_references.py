from pathlib import Path
import re

root = Path('src/main/java/com/example/server/handler')
for path in root.rglob('*.java'):
    if path.name == 'BaseHandler.java':
        continue
    text = path.read_text(encoding='utf-8')
    new = re.sub(r'\buserDAO\b', 'getUserDAO()', text)
    new = re.sub(r'\bitemDAO\b', 'getItemDAO()', new)
    new = re.sub(r'\bauctionDAO\b', 'getAuctionDAO()', new)
    new = re.sub(r'\bbidDAO\b', 'getBidDAO()', new)
    if new != text:
        path.write_text(new, encoding='utf-8')
        print('updated', path)
