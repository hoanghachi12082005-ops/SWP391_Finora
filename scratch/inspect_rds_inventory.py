import docx
import sys

sys.stdout.reconfigure(encoding='utf-8')

doc = docx.Document('docs/RDS document.docx')

print("=== ALL SUBSECTIONS & IMAGES UNDER 7. INVENTORY MANAGEMENT IN RDS ===")
h2 = ""
h3 = ""
h4 = ""

for i, p in enumerate(doc.paragraphs):
    if p.style.name == 'Heading 2':
        h2 = p.text.strip()
    elif p.style.name == 'Heading 3':
        h3 = p.text.strip()
    elif p.style.name == 'Heading 4':
        h4 = p.text.strip()
        
    if "7. Inventory Management" in h2 or "Inventory" in h2:
        blips = p._element.xpath('.//a:blip')
        if p.style.name.startswith('Heading') or len(blips) > 0:
            print(f"P[{i}] ({p.style.name}) H2: '{h2}' | H3: '{h3}' | H4: '{h4}' | text: '{p.text[:60]}'")
            for b in blips:
                rId = b.attrib.get('{http://schemas.openxmlformats.org/officeDocument/2006/relationships}embed')
                if rId and rId in doc.part.rels:
                    rel = doc.part.rels[rId]
                    ext = rel.target_part.content_type.split('/')[-1]
                    if ext == 'jpeg': ext = 'jpg'
                    print(f"    --> IMAGE: scratch/images_rds/p_{i}_{rId}.{ext}")
