import docx
import sys
import shutil

sys.stdout.reconfigure(encoding='utf-8')

# Import create_document from generate_doc
import scratch.generate_doc as gd

doc = gd.create_document()
