# -*- coding: utf-8 -*-
import argparse
from parser_xml.parser_xml import ParserXML
from parser_vector.parser_vector import ParserVector

if __name__ == '__main__' :

    # Parse arguments
    parser = argparse.ArgumentParser(description = 'No')
    # Optional arguments
    parser.add_argument('-j', '--json', default = 'conversation.json', type = str, help = 'Json File', dest = 'json_file')
    parser.add_argument('-x', '--xml', default = 'conversation.xml', type = str, help = 'Xml File', dest = 'xml_file')
    parser.add_argument('-d', '--dict', default = 'fs-politics-200-nsw-50.txt', type = str, help = 'Dictionary', dest = 'dictionary')

    # Ouput class arguments, class used in first step
    parser.add_argument('-lb', '--log_base', default = 10, type = int, help = 'Logarithmic scale base for weighting (default: 10)', dest = 'log_base')
    parser.add_argument('-col', '--collection', default = 'tweets', type = str, help = 'Collection of the data source (default: tweets)', dest = 'collection_name')
    parser.add_argument('-siu', '--showidsurls', action = 'store_true', default = False, help = 'show the tweets ids URLs of all the filtered tweets', dest = 'showtweetidsurls')
    parser.add_argument('-ws', '--weight_source', default = 'followers_count', type = str, choices = ['followers_count', 'favorite_count', 'retweet_count', 'favret_count', 'fo1fa40re20','score', 'ups', 'downs'], help = 'Source of information to get the weight of the argument (default: followers_count)', dest = 'weight_source')
    args = parser.parse_args()

    # First step parse json to xml
    parserXML = ParserXML(args)
    parserXML.json2XML()

    # Second step parse xml to vectors
    parserVector = ParserVector(args)
    parserVector.xml2Vector()

    # Third step
