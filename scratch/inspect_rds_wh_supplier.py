import docx
import sys

sys.stdout.reconfigure(encoding='utf-8')

doc_rds = docx.Document('docs/RDS document.docx')

print("=== RDS WAREHOUSE & SUPPLIER OPERATIONS (P[184] - P[240]) ===")
for i in range(184, 240):
    p = doc_rds.paragraphs[i]
    if p.text.strip():
        print(f"P[{i}] ({p.style.name}): {p.text.strip()}")
