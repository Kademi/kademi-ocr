#!/usr/bin/env python3

from setuptools import setup

setup(
    name='kademi-ocr-cli',
    version='1.0',
    description='',
    url='https://github.com/Kademi/kademi-ocr',
    license='',
    packages=[
        'ocrlib'
    ],
    install_requires=[
        'wand',
        'pyocr',
        'fleep'
    ],
    scripts=[
        'kademi-ocr.py'
    ],
    python_requires='>=3.6',
    zip_safe=False
)
