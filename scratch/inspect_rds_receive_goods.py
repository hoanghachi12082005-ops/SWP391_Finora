import docx
import sys

sys.stdout.reconfigure(encoding='utf-8')

doc_rds = docx.Document('docs/RDS document.docx')

print("=== RDS UC-8.3 RECEIVE GOODS (P[185] - P[191]) ===")
for i in range(185, 191):
    p = doc_rds.paragraphs[i]
    print(f"P[{i}] ({p.style.name}): {p.text.strip()}")
