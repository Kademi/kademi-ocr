#!/usr/bin/env python3

from wand.image import Image
from PIL import Image as PI

import json
import pyocr
import pyocr.builders
import io

for t in pyocr.get_available_tools():
    print(t)

tool = pyocr.get_available_tools()[1]
lang = tool.get_available_languages()[1]

print(tool)
print(lang)

req_image = []
final_text = []

image_pdf = Image(filename="/test.pdf", resolution=300)
image_jpeg = image_pdf.convert('jpeg')

for img in image_jpeg.sequence:
    img_page = PI.open(io.BytesIO(Image(image=img).make_blob('jpeg')))
    if tool.can_detect_orientation():
        try:
            orientation = tool.detect_orientation(
                img_page,
                lang=lang
            )
            print("Orientation: {}".format(orientation))
        except pyocr.PyocrException as exc:
            print("Orientation detection failed: {}".format(exc))

for img in image_jpeg.sequence:
    img_page = Image(image=img)
    req_image.append(img_page.make_blob('jpeg'))

for img in req_image:
    txt = tool.image_to_string(
        PI.open(io.BytesIO(img)),
        lang=lang,
        builder=pyocr.builders.LineBoxBuilder()
    )
    final_text.append(txt)

RESULT_OBJ = []

for a in final_text:
    for b in a:
        line = {
            'content': b.content,
            'position': b.position,
            'wordBoxes': []
        }

        for wb in b.word_boxes:
            line['wordBoxes'].append({
                'content': wb.content,
                'position': wb.position,
                'confidence': wb.confidence
            })

        RESULT_OBJ.append(line)

print(json.dumps({'data': RESULT_OBJ}))
