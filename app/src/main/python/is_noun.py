import nltk
import sys

nltk.download('averaged_perceptron_tagger')

def get_noun(line):
    words = line.split(" ")

    for word in words:
        ans = nltk.pos_tag(word)
        val = ans[0][1]
        if(val == 'NN' or val == 'NNS' or val == 'NNPS' or val == 'NNP'):
            return word
