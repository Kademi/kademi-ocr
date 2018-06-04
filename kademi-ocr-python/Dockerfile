FROM jfloff/alpine-python:3.6-slim

RUN echo "http://dl-cdn.alpinelinux.org/alpine/v${ALPINE_VERSION}/community/" >> /etc/apk/repositories

COPY ocrTest.py /ocrTest.py
COPY test.pdf /test.pdf

RUN /entrypoint.sh \
    -a zlib \
    -a zlib-dev \
    -a jpeg-dev \
    -a freetype-dev \
    -a lcms2-dev \
    -a openjpeg-dev \
    -a tiff-dev \
    -a tk-dev \
    -a tcl-dev \
    -a file \
    -a imagemagick6 \
    -a imagemagick6-dev \
    -a tesseract-ocr \
    -p wand \
    -p pyocr \
    && echo

CMD [ "python", "/ocrTest.py" ]