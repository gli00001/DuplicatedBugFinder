package com.sofi.tool.duplicatedBugFinder.controller;

import com.sofi.tool.duplicatedBugFinder.data.Issue;
import com.sofi.tool.duplicatedBugFinder.service.JiraService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;


@RestController
public class Controller {
    @Autowired
    private JiraService service;

    @RequestMapping(value = "/issue/{key}/{topK}", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public ResponseEntity<String> find(@PathVariable("key") String key, @PathVariable("topK") int topK) {
        Instant start = Instant.now();
        List<Issue> issues = service.findTopKSimilarIssues(key, topK);
        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);
        final String timeCost = "<h3>Search Time = " + duration.getSeconds() + " seconds</h3>";

        String bug0Url = String.format("<a href=\"https://%s/browse/" + key + "\">" + key + "</a>", service.JIRA_HOST);
        List<String>
            result =
            issues.stream()
                  .map(issue -> String.format("<a href=\"https://%s/browse/" + issue.getKey() + "\">" + issue.getKey() + "</a>", service.JIRA_HOST))
                  .collect(Collectors.toList());
        String resHtml = "<html>";
        resHtml += "<h1>Top 10 bugs most similar to " + bug0Url + ":</h1>";
        resHtml += "<br>" + StringUtils.join(result, "<br> ");
        resHtml += "<br><br><br><br>" + timeCost;
        resHtml += "</html>";
        return new ResponseEntity(resHtml, HttpStatus.OK);
    }

    @RequestMapping(value = "/issue/{key}", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public ResponseEntity<String> find(@PathVariable("key") String key) {
        return find(key, 10);
    }
}
