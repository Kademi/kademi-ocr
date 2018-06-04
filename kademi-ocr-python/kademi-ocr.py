#!/usr/bin/env python3

import json
import sys
import argparse

from ocrlib.ocr import OCRProcessor

parser = argparse.ArgumentParser(prog='kademi-ocr',
                                 description='Convert images to text, returning the results as json',
                                 usage=argparse.SUPPRESS)
parser.add_argument('--list-langs',
    dest='list_langs',
    action='store_true',
    help='List available languages for the current OCR tool'
)

parser.add_argument('--convert',
    dest='convert',
    action='store_true',
    help='Process the input file and convert to text'
)

parser.add_argument('--input',
    dest='input',
    type=str,
    help='The path of the input type to be processed',
    required='--convert' in sys.argv
)

parser.add_argument('--output',
    dest='output',
    default='-',
    nargs='?',
    help='Set the output path or use "-" for stdout (default)'
)

parser.add_argument('--format',
    dest='output_format',
    default='raw',
    nargs='?',
    help='Set the output format to use'
)

parser.add_argument('--lang',
    dest='lang',
    default='eng',
    nargs='?',
    help='language code'
)

parser.add_argument('--tool',
    dest='tool',
    default='tesseract',
    nargs='?',
    help='OCR Tool to use for processing'
)

ocr = OCRProcessor()

parser.parse_args(namespace=ocr)

try:
    if not ocr.process():
        parser.print_help()
except Exception as ex:
    if ocr.output_format == 'json':
        ocr.msg(json.dumps({'error': str(ex)}))
    else:
        raise ex