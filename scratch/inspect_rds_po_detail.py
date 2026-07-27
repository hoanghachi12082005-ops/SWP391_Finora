import docx
import sys

sys.stdout.reconfigure(encoding='utf-8')

doc_rds = docx.Document('docs/RDS document.docx')

print("=== SEARCH FOR PURCHASE / IMPORT IN RDS TEXT ===")
for i, p in enumerate(doc_rds.paragraphs):
    txt = p.text.strip()
    if 'Purchase' in txt or 'purchase' in txt or 'Import' in txt or 'Supplier' in txt or 'Nhập' in txt:
        if len(txt) > 20:
            print(f"P[{i}] ({p.style.name}): {txt[:120]}")
