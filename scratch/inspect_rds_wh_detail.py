import docx
import sys

sys.stdout.reconfigure(encoding='utf-8')

doc_rds = docx.Document('docs/RDS document.docx')

def print_element_text(start, end):
    for i in range(start, end):
        p = doc_rds.paragraphs[i]
        if p.text.strip():
            print(f"P[{i}] ({p.style.name}): {p.text.strip()}")

print_element_text(184, 201)
