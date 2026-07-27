import docx
import sys

sys.stdout.reconfigure(encoding='utf-8')

def map_doc_content(doc_path):
    doc = docx.Document(doc_path)
    print(f"================ MAP OF {doc_path} ================")
    
    current_h1 = ""
    current_h2 = ""
    current_h3 = ""
    
    for i, p in enumerate(doc.paragraphs):
        txt = p.text.strip()
        style = p.style.name
        
        # Check if paragraph has images
        blips = p._element.xpath('.//a:blip')
        has_img = len(blips) > 0
        
        if style == 'Heading 1' or style == 'Title':
            current_h1 = txt
            print(f"\n[H1] p{i}: {txt}")
        elif style == 'Heading 2':
            current_h2 = txt
            print(f"  [H2] p{i}: {txt}")
        elif style == 'Heading 3':
            current_h3 = txt
            print(f"    [H3] p{i}: {txt}")
        elif style == 'Heading 4':
            print(f"      [H4] p{i}: {txt}")
        elif has_img:
            print(f"        -> [IMAGE IN P{i}] under H1: '{current_h1}' | H2: '{current_h2}' | H3: '{current_h3}' | text: '{txt[:40]}'")

map_doc_content('docs/RDS document.docx')
map_doc_content('docs/SDS Document.docx')
