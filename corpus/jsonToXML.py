import sys
import os
import xml.etree.ElementTree as ET
from bs4 import BeautifulSoup
import json
from output import Output
import re
import argparse
import random

if __name__ == '__main__' :
    # Parse arguments
    parser = argparse.ArgumentParser(description = 'No')
    # Optional arguments
    parser.add_argument('--collection', default = 'tweets', type = str, help = 'Collection of the data source (default: tweets)', dest = 'collection_name')
    parser.add_argument('-fs', '--feature_set', default = 'feature_set.txt', type = str, help = 'Feature set to compute probability vectors (default: feature_set.txt)', dest = 'feature_set_file_name')
    parser.add_argument('-if', '--input_format', default = 'json', type = str, choices = ['json'], help = 'Input file format (default: json)', dest = 'input_format')
    parser.add_argument('-i', '--input', default = 'output.json', type = str, help = 'Input', dest = 'input_file_name')
    parser.add_argument('-is', '--input_source', default = 'twitter', type = str, choices = ['twitter', 'reddit'], help = 'Source data for the input file (default: twitter)', dest = 'input_source')
    parser.add_argument('-l', '--lang', default = None, type = str, help = 'Initial language filter (default: None)', dest = 'lang')
    parser.add_argument('-lb', '--log_base', default = 10, type = int, help = 'Logarithmic scale base for weighting (default: 10)', dest = 'log_base')
    parser.add_argument('-o', '--output', default = 'out.xml', type = str, help = 'Output file name (default: out.xml)', dest = 'output_file_name')
    parser.add_argument('-of', '--output_format', default = 'xml', type = str, choices = ['xml'], help = 'Output file format (default: xml)', dest = 'output_format')
    parser.add_argument('--showidsurls', action = 'store_true', default = False, help = 'show the tweets ids URLs of all the filtered tweets', dest = 'showtweetidsurls')
    parser.add_argument('-ws', '--weight_source', default = 'followers_count', type = str, choices = ['followers_count', 'favorite_count', 'retweet_count', 'favret_count', 'fo1fa40re20','score', 'ups', 'downs'], help = 'Source of information to get the weight of the argument (default: followers_count)', dest = 'weight_source')
    args = parser.parse_args()

    arg_list = []
    for line in open(args.input_file_name,'r+'):
        arg_list.append(json.loads(line))

    options = ["NONE","ATTACKS","SUPPORTS"]
    arg_pairs = []
    for arg1 in arg_list:
        for arg2 in arg_list:
            if arg1['id'] != arg2['id']:
                relation = random.randint(0,2)
                arg_pairs.append([arg1['id'],arg2['id'],options[relation],[]])
    print arg_pairs
    o = Output(args)
    xml = o.arg_list_pairs_to_xml(arg_list, arg_pairs, args)
    xml.write(args.output_file_name)
