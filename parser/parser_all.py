# -*- coding: utf-8 -*-
import argparse
from parser_xml.parser_xml import ParserXML
from parser_vector.parser_vector import ParserVector

if __name__ == '__main__' :

    # Parse arguments
    parser = argparse.ArgumentParser(description = 'No')
    # Files
    parser.add_argument('-j', '--json', default = 'files/conversation.json', type = str, help = 'Json File', dest = 'json_file')
    parser.add_argument('-x', '--xml', default = 'files/conversation.xml', type = str, help = 'Xml File', dest = 'xml_file')
    parser.add_argument('-d', '--dict', default = 'files/fs-politics-200-nsw-50.txt', type = str, help = 'Dictionary', dest = 'dictionary')
    parser.add_argument('-v', '--vector', default = 'files/vector.txt', type = str, help = 'Vector File', dest = 'vector_file')
    parser.add_argument('-p', '--pairs', default='files/pairs.txt', type = str, help = 'Pairs File', dest = 'pairs_file',)
    parser.add_argument('-a', '--attributes', default='files/attributes.txt', type = str, help = 'Pairs File', dest = 'attributes_file',)

    # Second Step
    parser.add_argument('-lb', '--log_base', default = 10, type = int, help = 'Logarithmic scale base for weighting (default: 10)', dest = 'log_base')
    parser.add_argument('-col', '--collection', default = 'tweets', type = str, help = 'Collection of the data source (default: tweets)', dest = 'collection_name')
    parser.add_argument('-siu', '--showidsurls', action = 'store_true', default = False, help = 'show the tweets ids URLs of all the filtered tweets', dest = 'showtweetidsurls')
    parser.add_argument('-ws', '--weight_source', default = 'followers_count', type = str, choices = ['followers_count', 'favorite_count', 'retweet_count', 'favret_count', 'fo1fa40re20','score', 'ups', 'downs'], help = 'Source of information to get the weight of the argument (default: followers_count)', dest = 'weight_source')
    # Third step
    parser.add_argument('-cos', '--cosine', default = 0, type = int, help = 'Boolean compute cosine', dest = 'compute_cosine')
    parser.add_argument("-den", "--dense", dest="dense",help="Dense format", action='store_true', default=False)
    parser.add_argument("-n", "--nlabels", dest="elabels",help="Filter out tweet labels", action='store_false', default=True)
    parser.add_argument("-e", "--elabels", dest="elabels",help="Filter out tweet labels", action='store_true' )

    args = parser.parse_args()

    # First step parse json to xml
    parserXML = ParserXML(args)
    parserXML.json2XML()

    # Second step parse xml to vectors
    parserVector = ParserVector(args)
    parserVector.xml2Vector()

    # Third step
