package com.sofi.tool.duplicatedBugFinder.utils;

import com.sofi.tool.duplicatedBugFinder.data.Issue;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by gli on 10/3/18.
 */
@Data
public class IssueDescriptionDistance implements Comparable<IssueDescriptionDistance>{
    private final Issue issueA;
    private final Issue issueB;
    private Integer distance;

    public  IssueDescriptionDistance(Issue a, Issue b) {
        issueA = a;
        issueB = b;
        distance = StringUtils.getLevenshteinDistance(a.getDescription(), b.getDescription());
    }

    @Override
    public int compareTo(IssueDescriptionDistance o) {
        return distance.compareTo(o.distance);
    }


}
