#!/usr/bin/env python3

import argparse
import sys
import pyocr

parser = argparse.ArgumentParser(description='PyOCR Based OCR processor')
parser.add_argument(
    '--list-langs', help="List available languages", action='store_true')
parser.add_argument(
    '-f', '--format', help="Output format", choices=['console', 'json'])

args = parser.parse_args()

tools = pyocr.get_available_tools()

if len(tools) == 0:
    if args.format == 'json':
        print('{error: "No OCR Tool found"}')
    else:
        print('No OCR Tool found')
    sys.exit(1)

if args.list_langs:
    print( args )
else:
    parser.print_help()