package com.sofi.tool.duplicatedBugFinder.service;
import com.sofi.tool.duplicatedBugFinder.data.Issue;
import com.sofi.tool.duplicatedBugFinder.utils.IssueDescriptionDistCalculator;
import com.sofi.tool.duplicatedBugFinder.utils.IssueDescriptionDistance;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
public class JiraService {

    public final String JIRA_HOST = System.getProperty("jiraHost");
    private final String authHeaderValue = System.getProperty("authHeaderValue");
    private final String PROJECT = System.getProperty("project", "SOFI");

    private final String DEFAULT_SINGLE_ISSUE_URL_PREFIX = "https://" + JIRA_HOST + "/rest/api/2/issue/";
    private final String DEFAULT_ISSUES_SEARCH_URL_PREFIX = "https://" + JIRA_HOST + "/rest/api/2/search?";

    private final int DEFAULT_PAGE_SIZE = 100; //Seems like JIRA does not allow this value to be greater than 100
    private final String BUGS_SEARCH_PARAMS = String.format("jql=project = \"%s\" and type = \"Bug\"", PROJECT);
    private final String COMPONENT_PREFIX = " and component in (";
    private final String COMPONENT_SEPERATOR = ",";
    private final String COMPONENT_POSTFIX = ")";



    private final int pageSize = Integer.getInteger("pageSize", DEFAULT_PAGE_SIZE);
    private final int MAX_LATEST_ISSUES_COUNT = Integer.getInteger("searchRange", 8000);
    private final String singleIssueUrl = System.getProperty("singleIssueUrl", DEFAULT_SINGLE_ISSUE_URL_PREFIX);
    public final int maxIssueCount = Integer.parseInt(System.getProperty("maxIssueCount", Integer.toString(MAX_LATEST_ISSUES_COUNT)));

    private JsonNode query(String uri) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", authHeaderValue);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

        return restTemplate.exchange(uri, HttpMethod.GET, entity, JsonNode.class).getBody();
    }

    private List<Issue> loadTopKIssuesFromJiraForSearchParams(String searchParams, Issue target, int topK) {
        int issueCount = findIssueCount(searchParams);
        return IntStream.range(0, issueCount / getPageSize() + 1 ).parallel()
                        .mapToObj(index -> loadIssues(index, searchParams))
                        .map(issues->IssueDescriptionDistCalculator.getSortedDistance(target, issues))
                        .flatMap(List::stream)
                        .filter(d -> !d.getIssueB().getKey().equals(d.getIssueA().getKey()))
                        .sorted()
                        .limit(topK)
                        .map(IssueDescriptionDistance::getIssueB)
                        .collect(Collectors.toList());
    }

    private Issue jsonToIssue(JsonNode issueNode) {
        String key = issueNode.get("key").textValue();
        String desc = issueNode.get("fields").get("description").textValue();
        String summary = (issueNode.get("fields").get("summary") != null) ? issueNode.get("fields").get("summary").textValue() : null;
        final JsonNode componentNodes = issueNode.get("fields").get("components");
        List<String> components = new ArrayList<>();
        if (componentNodes != null && componentNodes.isArray()) {
            for (final JsonNode componentNode : componentNodes) {
                String componentName = componentNode.get("name").textValue();
                components.add(componentName);
            }
        }

        return new Issue(key, desc, summary, components);
    }


    public Issue findAnIssue(String key) {
        String uri = singleIssueUrl+key;
        JsonNode node = query(uri);

        if (node == null) {
            log.error("Unknown ticket "+key);
            return null;
        } else if (!node.get("fields").get("issuetype").get("name").asText().equalsIgnoreCase("Bug")){
            log.warn("This ticket "+key+" is not a bug.");
            //return null;
        }

        Issue issue =  jsonToIssue(node);
        return issue;
    }

    public List<Issue> findTopKSimilarIssues(String targetKey, int topK) {
        return loadTopKBugIssuesWithSameComponentFromJira(targetKey, topK);
    }

    private List<Issue> loadIssues(int pageIndex, String searchParams) {
        log.info("startAt="+pageIndex*getPageSize());
        JsonNode node = query(DEFAULT_ISSUES_SEARCH_URL_PREFIX + "&startAt=" + pageIndex * getPageSize() + "&maxResults=" + getPageSize() + "&" + searchParams);
        JsonNode issueNodes = node.get("issues");
        List<Issue> issues = new ArrayList<>();
        if (issueNodes.isArray()) {
            for (final JsonNode issueNode : issueNodes) {
                issues.add(jsonToIssue(issueNode));
            }
        }

        return issues;
    }

    public int findIssueCount(String searchParams) {
        JsonNode node = query(DEFAULT_ISSUES_SEARCH_URL_PREFIX + "maxResults=1&" + searchParams);
        int count = node.get("total").asInt();
        return (maxIssueCount > 0? Math.min(maxIssueCount, count):count);
    }

    private int getPageSize() {
        if (pageSize>DEFAULT_PAGE_SIZE) {
            log.info("Page size cannot be larger than 100. Reset page size to 100.");
            return DEFAULT_PAGE_SIZE;
        } else  {
          return pageSize;
        }
    }

    /**
     * Returns a list of tickets that are bugs and have same component as the jira ticket
     *
     * @param jiraTicket - Either in the form or a number of sofi-number of an existing ticket in JIRA
     * @return A list of Issues where the issuetype.name = Bug and there the field.components list matches the jiraTickets components
     * or no components filter if the jiraTicket does not have a component or null if it is an invalid ticket.
     */
    public List<Issue> loadTopKBugIssuesWithSameComponentFromJira(String jiraTicket) {
        return loadTopKBugIssuesWithSameComponentFromJira(jiraTicket, 10);
    }


    public List<Issue> loadTopKBugIssuesWithSameComponentFromJira(String jiraTicket, int topK) {
        String formattedJiraTicket = formatJiraTicket(jiraTicket);
        if (formattedJiraTicket == null) {
            return Collections.emptyList();
        }

        Issue target = findAnIssue(formattedJiraTicket);
        List<String> components = target.getComponents();

        if (components == null) {
            return Collections.emptyList(); 
        }

        String urlForComponents = getSearchQueryWithComponents(components);
        log.info("Search Params For ticket "+formattedJiraTicket+" = '"+urlForComponents+"'");

        return loadTopKIssuesFromJiraForSearchParams(urlForComponents, target, topK);
    }

    private String getSearchQueryWithComponents(List<String> components) {
        String urlForBugAndComponent = BUGS_SEARCH_PARAMS;
        if (components.size() > 0) {
            urlForBugAndComponent = urlForBugAndComponent + COMPONENT_PREFIX;
            for (int i = 0; i < components.size(); i++) {
                String componentName = components.get(i);
                urlForBugAndComponent = urlForBugAndComponent + componentName;
                if ((i+1) < components.size()){
                    urlForBugAndComponent = urlForBugAndComponent + COMPONENT_SEPERATOR;
                }
            }
            urlForBugAndComponent = urlForBugAndComponent + COMPONENT_POSTFIX;
        }
        return urlForBugAndComponent;
    }


    /**
     *
     * @param jiraTicket - Expected to be either XXXX or sofi-XXXX or SOFI-XXXX
     * @return null if invalid, otherwise format to be SOFI-XXXXX
     */
    private String formatJiraTicket(String jiraTicket) {
        if (StringUtils.isBlank(jiraTicket)) {
            log.info("Ticket cannot be null. Please put in the format SOFI-XXXX or just XXXX where XXXX is a number.");
            return null;
        }

        if (!jiraTicket.startsWith("SOFI-")) {
            jiraTicket = "SOFI-"+jiraTicket;
        }
        //validate the stuff after 'SOFI-' is a number
        String numberPartOfTicket = jiraTicket.substring(5);
        try {
            Integer.parseInt(numberPartOfTicket);
        } catch (NumberFormatException nfe) {
            log.info("Invalid ticket number. Please put in the format SOFI-XXXX or just XXXX where XXXX is a number.");
            return null;
        }
        return jiraTicket;
    }

}
