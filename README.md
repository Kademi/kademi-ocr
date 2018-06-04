# kademi-ocr
Kademi scanning library

## Installing the Python library
Requires Python 3.6 or greater and pip3:

`sudo apt install -y python3 python3-pip`

Install tesseract-ocr:

`sudo apt install -y tesseract-ocr`

Install dependencies for Pillow and Wand (Used by PyOCR):

`sudo apt install -y libtiff5-dev libjpeg8-dev zlib1g-dev libfreetype6-dev liblcms2-dev libwebp-dev libharfbuzz-dev libfribidi-dev libmagickwand-dev`

Install the Python OCR project:

`sudo python3 setup.py install`

or to force update

`sudo python3 setup.py install --force`

You can access the command from a shell in any directory by using:

`kademi-ocr.py --help`
