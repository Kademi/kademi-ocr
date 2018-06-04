from wand.image import Image
from PIL import Image as PI
from pathlib import Path

import fleep
import json
import pyocr
import pyocr.builders
import io
import sys


class OCRProcessor:
    """
    Class for parsing CLI arguments and processing them
    """

    __lang = 'eng'
    __tool_name = 'tesseract'
    __tool = None
    list_langs = False
    convert = False
    output_format = 'raw'
    __output = '-'
    __output_file = None
    input = None

    def msg(self, str_msg):
        print(str(str_msg), file=self.__output_file)

    @property
    def lang(self):
        """Language Code
        """

        return self.__lang

    @lang.setter
    def lang(self, code):
        return

    @property
    def tool(self):
        """Tool Name
        """

        return self.__tool_name

    @tool.setter
    def tool(self, tool_name):
        tools = pyocr.get_available_tools()
        if len(tools) == 0:
            sys.exit(1)
        else:
            for tool in tools:
                if tool.__name__ == 'pyocr.' + tool_name:
                    self.__tool = tool
                    self.__tool_name = tool_name
                    return
        raise AttributeError('Tool "' + tool_name + '" is not available')

    @property
    def output(self):
        return self.__output

    @output.setter
    def output(self, name):
        self.__output = name

        if name == '-':
            self.__output_file = sys.stdout
        else:
            filename = Path(name)
            filename.touch(exist_ok=True)  # will create file, if it exists will do nothing
            self.__output_file = open(name, 'w+')

    def _get_tool(self):
        if self.__tool is None:
            if not self.__tool_name:
                raise AttributeError('Tool name not set, Please set a tool name')
            self.tool = self.__tool_name

            return self.__tool
        else:
            return self.__tool

    def process(self):
        if self.list_langs:
            tool = self._get_tool()

            avail_langs = tool.get_available_languages()

            if self.output_format == 'json':
                self.msg(json.dumps({'langs': avail_langs}))
            else:
                self.msg('Available Languages:')
                self.msg(', '.join(avail_langs))

            return True
        elif self.convert:
            if Path(self.input).is_file():
                with open(self.input, "rb") as file:
                    info = fleep.get(file.read(128))

                is_raster_image = info.type_matches('raster-image')
                is_pdf = info.extension_matches('pdf')

                if not is_raster_image and not is_pdf:
                    if len(info.mime) > 0:
                        type = str(info.mime[0])
                    else:
                        type = 'Unknown'

                    raise AttributeError('Unsupported input file type: ' + type)

                tool = self._get_tool()
                FINAL_TEXT = []
                if is_raster_image:
                    txt = tool.image_to_string(
                        PI.open(self.input),
                        lang=self.lang,
                        builder=pyocr.builders.LineBoxBuilder()
                    )
                    FINAL_TEXT.append(txt)
                elif is_pdf:
                    self.msg('TODO')

                RESULT_OBJ = []

                for a in FINAL_TEXT:
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

                self.msg(json.dumps({'result': RESULT_OBJ}))

                return True
            else:
                raise AttributeError('Input file does not exist: ' + str(self.input))

        return False
