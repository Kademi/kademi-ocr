#!/usr/bin/env python3

from setuptools import setup

setup(
    name='kademi-ocr-cli',
    version='1.0',
    description='',
    url='',
    license='',
    packages=[
        'ocrlib'
    ],
    install_requires=[
        'pyocr',
        'fleep'
    ],
    scripts=[
        'kademi-ocr.py'
    ],
    python_requires='>=3.6',
    zip_safe=False
)
