import sys
import os
import xml.etree.ElementTree as ET
from bs4 import BeautifulSoup
import json
from output import Output
import re
import argparse
import random

class ParserXML():

    def __init__(self, args):
        self.args = args

    def json2XML(self):
        # Loads Json
        arg_list = []
        with open(self.args.json_file,'r+') as json_file:
            for line in json_file:
                arg_list.append(json.loads(line))

        options = ["NONE","ATTACKS","SUPPORTS"]
        arg_pairs = []
        for arg1 in arg_list:
            for arg2 in arg_list:
                if arg1['id'] != arg2['id']:
                    relation = random.randint(0,2)
                    arg_pairs.append([arg1['id'],arg2['id'],options[relation],[]])
        output = Output('xml')
        xml = output.arg_list_pairs_to_xml(arg_list, arg_pairs, self.args)

        # Saves XML
        with open(self.args.xml_file,'w+') as xml_file:
            xml.write(xml_file)
