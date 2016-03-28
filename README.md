# Articleate
Build for Capital One's MindSumo challenge. Articleate is an Android application that performs text analysis
and extraction on internet news articles.  

##Dependencies
This application depends on JSoup, Apache OpenNLP, and JGraphT.

##About the application
The following information about articles is provided by
Articleate:

* One sentence summary

* Keywords of the article

* Author information

* Keyword-based definitions, information, and
related articles

###TextRank implementation
Articleate is able to extract key sentences and words in
articles through an NLP algorithm known as TextRank. TextRank is a graph-based
algorithm derived from Larry Page’s and Sergey Brin’s PageRank algorithm,
originally used in determining order of pages in Google searches. 

To perform PageRank, Articleate depends on JGraphT and Apache OpenNLP. JGraphT is used to represent the various graphs used in text extraction, while OpenNLP provides a simple means of sentence separation and text tokenization. The original TextRank paper can be read [here](https://web.eecs.umich.edu/~mihalcea/papers/mihalcea.emnlp04.pdf), and my Java TextRank implementation for this project is available [here](https://github.com/J0Nreynolds/Articleate/blob/master/app/src/main/java/textrank/TextRank.java). In order to optimize the algorithm, stoplists are used to remove common tokens, greatly reducing the number of vertices in keyword extraction and the number of similarities in sentence extraction.

####Benefits:
Using TextRank as a means of article summarization in-app requires no web API calls and runs very quickly.

###Own article extraction methods
Before the TextRank algorithm is run on text, it has to be extracted from webpages first. In order to extract web page text, I used JSoup, an HTML parsing library. After looking at the general HTML structure and patterns on major news websites, I developed a series of methods for extracting author information and article text content without unneeded textual elements.
