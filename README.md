# Articleate
Built for Capital One's [MindSumo challenge](https://www.mindsumo.com/contests/565) and competitive entrance to Capital One's [annual SE Summit](https://www.youtube.com/watch?v=c5efHTl40dE). Articleate is an Android application that performs text analysis and extraction on internet news articles.

<p align="center">
  <img  src="https://raw.githubusercontent.com/J0Nreynolds/Articleate/master/Screenshots/Screenshot_2016-03-27-17-31-14.png" width="210" />
  
  <img  src="https://raw.githubusercontent.com/J0Nreynolds/Articleate/master/Screenshots/Screenshot_2016-03-27-17-24-46.png" width="210" />
  
  <img  src="https://raw.githubusercontent.com/J0Nreynolds/Articleate/master/Screenshots/Screenshot_2016-03-27-17-24-53.png" width="210" />
  
  
  <img  src="https://github.com/J0Nreynolds/Articleate/blob/master/Screenshots/Screenshot_2016-03-27-18-23-19.png" width="210" />
</p>
####Table of Contents
* [Articleate](https://github.com/J0Nreynolds/Articleate/blob/master/README.md#articleate)
* [Dependencies](https://github.com/J0Nreynolds/Articleate/blob/master/README.md#dependencies)
* [About the application](https://github.com/J0Nreynolds/Articleate/blob/master/README.md#about-the-application)
 * [TextRank implementation](https://github.com/J0Nreynolds/Articleate/blob/master/README.md#textrank-implementation)
 * [Article extraction methods](https://github.com/J0Nreynolds/Articleate/blob/master/README.md#own-article-extraction-methods)
 * [Cached summaries](https://github.com/J0Nreynolds/Articleate/blob/master/README.md#cached-summaries)
 * [Further research](https://github.com/J0Nreynolds/Articleate/blob/master/README.md#researching-articles-further)
 * [Intent filters for news sources](https://github.com/J0Nreynolds/Articleate/blob/master/README.md#intent-filters-for-popular-news-sources)
* [Results](https://github.com/J0Nreynolds/Articleate/blob/master/README.md#results)

##Dependencies
The application's functionality depends on [JSoup](http://jsoup.org/), [Apache OpenNLP](http://opennlp.apache.org/), and [JGraphT](http://jgrapht.org/).
Additionally, the application depends on [AndroidSlidingUpPanel](https://github.com/umano/AndroidSlidingUpPanel) and [android-flowlayout](https://github.com/ApmeM/android-flowlayout) for Android UI elements. All of these dependencies can be obtained through Maven in Android Studio.

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

To perform TextRank, Articleate depends on JGraphT and Apache OpenNLP. JGraphT is used to represent the various graphs used in text extraction, while OpenNLP provides a simple means of sentence separation and text tokenization. The original TextRank paper can be read [here](https://web.eecs.umich.edu/~mihalcea/papers/mihalcea.emnlp04.pdf), and my Java TextRank implementation for this project is available [here](https://github.com/J0Nreynolds/Articleate/blob/master/app/src/main/java/textrank/TextRank.java). In order to optimize the algorithm, stoplists are used to remove common English-language tokens, greatly reducing the number of vertices in keyword extraction and the number of similarities in sentence extraction.

#####Benefits:
Using TextRank as a means of article summarization in-app requires no web API calls and runs very quickly.

###Own article extraction methods
Before the TextRank algorithm is run on text, it has to be extracted from webpages first. In order to extract web page text, I used JSoup, an HTML parsing library. After looking at the general HTML structure and patterns on major news websites, I developed a series of methods for extracting author information and article text content without unneeded textual elements. 

After extracting the desired text from an article, the text is passed through my TextRank implementation, where keyword and sentence rankings are performed. Then, the top sentence, top 8 keywords, and author information of the article are displayed to the user.

#####Benefits:
Again, all processing is done in-app, and targeting general web-article patterns allows the article extraction methods to work on nearly all articles on the web. See the [Results section](https://github.com/J0Nreynolds/Articleate/blob/master/README.md#results) for a listing of successfully tested news sources.

###Cached summaries
Once summaries are successfully created, they are cached. Articleate will cache your 20 most-recent article summarizations, which are shown on the MainActivity of the app.

#####Benefits:
Users can re-read past summarizations at any time (even if the application was closed), allowing them to continue researching a certain issue or to recall their earlier readings.

###Researching articles further
Touching a keyword gives you multiple options for further research:
* _Google News_
 * Google News will show relevant, recent news articles that focus on the same keyword.
* _Google_
 * Google provides general information (definitions, information, articles, etc.) on the keyword.
* _Wikipedia_
 * Wikipedia provides detailed descriptions, history, and sources related to the keyword.
* _IMDb_
 * IMDb provides information about celebrity or movie related keywords.


###Intent filters for popular news sources
Articleate has incorporated intent filters for the following news sources' websites:

* CNN
* The New York Times
* The Washington Post
* The Huffington Post
* The Guardian
* Reuters

#####Benefits:
In specific, I've examined the URL patterns for each of the above news sources, tailoring the intent filters to those URL patterns. Therefore, the intent filters are only triggered when navigating to an article on the website, not on pages like the homepage, about page, etc.

##Results
Articleate has been tested on the following news sources (allow a checkmark to represent successful summarizations):

- [X] CNN
- [X] The Huffington Post
- [X] The New York Times
- [X] Wired
- [X] IGN
- [X] The Guardian
- [X] The Washington Post
- [X] Reuters
- [ ] Yahoo

**Note:** (JSoup is unable to correctly load Yahoo articles. This may be due to JavaScript loading mechanisms on the pages)

Below are examples of summaries on some of the above news sources. These are summaries from CNN, The Huffington Post, The New York Times, Wired, IGN, and The Washington Post, respectively.

<p display="">
  <img  src="https://raw.githubusercontent.com/J0Nreynolds/Articleate/master/Screenshots/Screenshot_2016-03-27-17-41-54.png" width="210" />

  <img  src="https://raw.githubusercontent.com/J0Nreynolds/Articleate/master/Screenshots/Screenshot_2016-03-27-17-27-53.png" width="210" />

  <img  src="https://raw.githubusercontent.com/J0Nreynolds/Articleate/master/Screenshots/Screenshot_2016-03-27-17-30-57.png" width="210" />

  <img  src="https://raw.githubusercontent.com/J0Nreynolds/Articleate/master/Screenshots/Screenshot_2016-03-27-17-24-53.png" width="210" />

  <img  src="https://raw.githubusercontent.com/J0Nreynolds/Articleate/master/Screenshots/Screenshot_2016-03-27-17-50-06.png" width="210" />

  <img  src="https://raw.githubusercontent.com/J0Nreynolds/Articleate/master/Screenshots/Screenshot_2016-03-27-17-26-35.png" width="210" />
</p>
