import docx
import sys
import os

sys.stdout.reconfigure(encoding='utf-8')

def detailed_image_mapping(doc_path, prefix):
    doc = docx.Document(doc_path)
    print(f"=== DETAILED IMAGE MAPPING FOR {doc_path} ===")
    
    current_h1 = ""
    current_h2 = ""
    current_h3 = ""
    current_h4 = ""
    
    for i, p in enumerate(doc.paragraphs):
        txt = p.text.strip()
        style = p.style.name
        
        if style == 'Heading 1' or style == 'Title':
            current_h1 = txt
        elif style == 'Heading 2':
            current_h2 = txt
        elif style == 'Heading 3':
            current_h3 = txt
        elif style == 'Heading 4':
            current_h4 = txt
            
        blips = p._element.xpath('.//a:blip')
        for b in blips:
            rId = b.attrib.get('{http://schemas.openxmlformats.org/officeDocument/2006/relationships}embed')
            if rId and rId in doc.part.rels:
                rel = doc.part.rels[rId]
                if 'image' in rel.target_ref:
                    img_part = rel.target_part
                    ext = img_part.content_type.split('/')[-1]
                    if ext == 'jpeg': ext = 'jpg'
                    filename = f"scratch/images_{prefix}/p_{i}_{rId}.{ext}"
                    print(f"P[{i}] Img: {filename} | H1: '{current_h1}' | H2: '{current_h2}' | H3: '{current_h3}' | H4: '{current_h4}' | PText: '{txt[:50]}'")

detailed_image_mapping('docs/RDS document.docx', 'rds')
detailed_image_mapping('docs/SDS Document.docx', 'sds')
