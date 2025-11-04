package com.summarizer_backend.dto;



public class SummarizeResponse {
 private final String summary;
 private final int sentenceCount;
 private final int wordCount;

 
 public SummarizeResponse(String summary, int sentenceCount, int wordCount) {
     this.summary = summary;
     this.sentenceCount = sentenceCount;
     this.wordCount = wordCount;
 }
 
 
 public String getSummary() {
     return summary;
 }

 public int getSentenceCount() {
     return sentenceCount;
 }

 public int getWordCount() {
     return wordCount;
 }
}
