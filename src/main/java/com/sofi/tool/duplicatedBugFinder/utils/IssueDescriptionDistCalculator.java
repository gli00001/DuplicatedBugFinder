package com.sofi.tool.duplicatedBugFinder.utils;

import com.sofi.tool.duplicatedBugFinder.data.Issue;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class IssueDescriptionDistCalculator {

    public static List<IssueDescriptionDistance> getSortedDistance(Issue bug0, Issue... bugs) {
        Stream<Issue> s = Arrays.stream(bugs);
        List<IssueDescriptionDistance> nearestNeighbors = s.parallel().filter(bug -> StringUtils.isNotBlank(bug.getDescription())).map(
            b -> new IssueDescriptionDistance(bug0, b)
        ).sorted().collect(Collectors.toList());

        return nearestNeighbors;
    }

    public static List<IssueDescriptionDistance> getSortedDistance(Issue bug0, List<Issue> bugs) {
        return getSortedDistance(bug0, bugs.toArray(new Issue[0]));
    }

    public static List<Issue> getNearestNeighbors(Issue bug0, Issue... bugs) {
        if (bug0 == null || bug0.getDescription()==null || bug0.getDescription().isEmpty()) {
            log.error("bug is null or bug's description is empty.");
            return new ArrayList<>();
        }
        List<IssueDescriptionDistance> distances = getSortedDistance(bug0, bugs);

        List<Issue> nearestNeighbors = distances.stream().filter(d -> !d.getIssueB().getKey().equals(d.getIssueA().getKey())).map(d -> d.getIssueB()).collect(Collectors.toList());
        return  nearestNeighbors;
    }
}

