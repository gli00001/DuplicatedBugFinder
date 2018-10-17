package com.sofi.tool.duplicatedBugFinder.utils;

import com.sofi.tool.duplicatedBugFinder.data.Issue;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by gli on 10/3/18.
 */
public class IssueDescriptionDistCalculatorTest {

    @Test
    public void testGetNearestNeighbors() {

        Issue bug0 = new Issue("SOFI-0", "null pointer exception");
        Issue bug1 = new Issue("SOFI-1", "null pointer exception 1");
        Issue bug2 = new Issue("SOFI-2", "null pointer exception 11");
        Issue bug3 = new Issue("SOFI-3", "runtime exception");
        Issue bug4 = new Issue("SOFI-4", "IO exception");
        Issue bug5 = new Issue("SOFI-5", "number format exception");


        Issue[] bugs = {
            bug0,
            bug1,
            bug2,
            bug3,
            bug4,
            bug5
        };


        List<Issue> nearestNeighbors = IssueDescriptionDistCalculator.getNearestNeighbors(bug0, bugs);
        Assert.assertEquals(bugs.length - 1, nearestNeighbors.size());
        Assert.assertEquals(bug1, nearestNeighbors.get(0));
        Assert.assertEquals(bug2, nearestNeighbors.get(1));


        nearestNeighbors = IssueDescriptionDistCalculator.getNearestNeighbors(bug1, bug0, bug2, bug3);
        Assert.assertEquals(bug3, nearestNeighbors.get(2));
    }
}
